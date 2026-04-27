package com.theatermgnt.theatermgnt.authentication.service;

import java.security.SecureRandom;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import com.theatermgnt.theatermgnt.account.entity.Account;
import com.theatermgnt.theatermgnt.account.repository.AccountRepository;
import com.theatermgnt.theatermgnt.authentication.dto.request.*;
import com.theatermgnt.theatermgnt.authentication.dto.response.AuthenticationResponse;
import com.theatermgnt.theatermgnt.authentication.dto.response.IntrospectResponse;
import com.theatermgnt.theatermgnt.authentication.entity.InvalidatedToken;
import com.theatermgnt.theatermgnt.authentication.entity.OtpToken;
import com.theatermgnt.theatermgnt.authentication.enums.AccountType;
import com.theatermgnt.theatermgnt.authentication.event.PasswordResetEvent;
import com.theatermgnt.theatermgnt.authentication.repository.InvalidatedTokenRepository;
import com.theatermgnt.theatermgnt.authentication.repository.OtpTokenRepository;
import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationService {
    AccountRepository accountRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;
    ApplicationEventPublisher eventPublisher;
    OtpTokenRepository otpTokenRepository;
    TokenService tokenService;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    @NonFinal
    @Value("${otp.valid-duration}")
    protected long OTP_VALID_DURATION;

    /// INTROSPECT
    public IntrospectResponse introspect(IntrospectRequest request) throws ParseException, JOSEException {
        var token = request.getToken();
        boolean isValid = true;

        // SIGNED JWT = Verify token
        try {
            verifyToken(token, false);
        } catch (AppException e) {
            log.error("Token verification failed: {}", e.getMessage());
            isValid = false;
        }
        return IntrospectResponse.builder().valid(isValid).build();
    }

    /// AUTHENTICATE WITH ACCOUNT TYPE VALIDATION
    public AuthenticationResponse authenticate(AuthenticationRequest request, AccountType requiredAccountType) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

        var account = accountRepository
                .findByUsernameOrEmail(request.getLoginIdentifier(), request.getLoginIdentifier())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        boolean authenticated = passwordEncoder.matches(request.getPassword(), account.getPassword());
        if (!authenticated) throw new AppException(ErrorCode.UNAUTHENTICATED);

        // Validate account type
        if (account.getAccountType() != requiredAccountType) {
            log.warn(
                    "Account type mismatch: {} tried to login with {} account type",
                    account.getUsername(),
                    requiredAccountType);
            throw new AppException(ErrorCode.WRONG_ACCOUNT_TYPE);
        }

        var token = tokenService.generateToken(account);
        return AuthenticationResponse.builder().authenticated(true).token(token).build();
    }

    /// LOGOUT METHOD
    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        try {
            var signToken = verifyToken(request.getToken(), true);
            String jti = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken =
                    InvalidatedToken.builder().id(jti).expiryTime(expiryTime).build();
            invalidatedTokenRepository.save(invalidatedToken);
        } catch (AppException e) {
            log.info("Token already expired: {}", e.getMessage());
        }
    }

    /// REFRESH TOKEN
    public AuthenticationResponse refreshToken(RefreshTokenRequest refreshToken) throws ParseException, JOSEException {
        // Step 1: Check validation of the token.
        var signedJWT = verifyToken(refreshToken.getToken(), true);

        // Step 2: Invalidate old token
        var jti = signedJWT.getJWTClaimsSet().getJWTID();
        var expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        InvalidatedToken invalidatedToken =
                InvalidatedToken.builder().id(jti).expiryTime(expiryTime).build();
        invalidatedTokenRepository.save(invalidatedToken);

        // Step 3: Generate new token based on current user
        var username = signedJWT.getJWTClaimsSet().getSubject();
        var account = accountRepository
                .findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        var token = tokenService.generateToken(account);
        return AuthenticationResponse.builder().token(token).authenticated(true).build();
    }

    /// FORGOT PASSWORD
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {

        var account =
                accountRepository.findByUsernameOrEmail(request.getLoginIdentifier(), request.getLoginIdentifier());

        // Does not throw exception if not found
        if (account.isEmpty()) {
            log.info("Password reset requested for non-existing account: {}", request.getLoginIdentifier());
            return; // Exit silently to prevent user enumeration
        }

        Account acc = account.get();

        // Delete old OTP and flush immediately to avoid constraint violation
        otpTokenRepository.findByAccount(acc).ifPresent(oldToken -> {
            otpTokenRepository.delete(oldToken);
            otpTokenRepository.flush(); // Force immediate delete
        });

        // Generate otp code
        String otpCode = generateOtpCode();
        Instant expiryTime = Instant.now().plus(OTP_VALID_DURATION, ChronoUnit.MINUTES);

        // Save OTP into database
        OtpToken newOtpToken = OtpToken.builder()
                .account(acc)
                .code(otpCode)
                .expiryTime(expiryTime)
                .build();
        otpTokenRepository.save(newOtpToken);
        otpTokenRepository.flush(); // Force immediate save

        // Publish event to send email
        try {
            eventPublisher.publishEvent(new PasswordResetEvent(account.get(), otpCode));
        } catch (Exception e) {
            // Log error but do not throw to avoid user enumeration
            log.error("Error publishing password reset event: {}", e.getMessage(), e);
        }
    }

    /// RESET PASSWORD
    public void resetPassword(ResetPasswordRequest request) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

        var account = accountRepository
                .findByUsernameOrEmail(request.getLoginIdentifier(), request.getLoginIdentifier())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Get saved OTP from database
        OtpToken savedOtpToken = otpTokenRepository
                .findByAccount(account)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        // Validate OTP code and expiry
        if (savedOtpToken.getExpiryTime().isBefore(Instant.now())) {
            otpTokenRepository.delete(savedOtpToken);
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }

        if (!savedOtpToken.getCode().equals(request.getOtpCode())) {
            throw new AppException(ErrorCode.INVALID_OTP);
        }
        // OTP is valid - proceed to reset password
        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(account);

        // Delete used OTP
        otpTokenRepository.delete(savedOtpToken);
        log.info("Password has been reset for user: {}", account.getEmail());
    }

    /// VERIFY TOKEN
    private SignedJWT verifyToken(String token, boolean isRefreshToken) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = (isRefreshToken)
                ? new Date(signedJWT
                        .getJWTClaimsSet()
                        .getIssueTime()
                        .toInstant()
                        .plus(REFRESHABLE_DURATION, ChronoUnit.MILLIS)
                        .toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        if (!(verified && expiryTime.after(new Date()))) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // Case:User has logged out -> token is invalidated
        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return signedJWT;
    }

    /// GENERATE OTP CODE
    private String generateOtpCode() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
