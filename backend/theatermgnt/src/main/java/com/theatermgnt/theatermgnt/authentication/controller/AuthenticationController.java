package com.theatermgnt.theatermgnt.authentication.controller;

import java.security.SecureRandom;
import java.text.ParseException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.nimbusds.jose.JOSEException;
import com.theatermgnt.theatermgnt.authentication.dto.request.AuthenticationRequest;
import com.theatermgnt.theatermgnt.authentication.dto.request.IntrospectRequest;
import com.theatermgnt.theatermgnt.authentication.dto.request.LogoutRequest;
import com.theatermgnt.theatermgnt.authentication.dto.request.RefreshTokenRequest;
import com.theatermgnt.theatermgnt.authentication.dto.response.AuthenticationResponse;
import com.theatermgnt.theatermgnt.authentication.dto.response.IntrospectResponse;
import com.theatermgnt.theatermgnt.authentication.enums.AccountType;
import com.theatermgnt.theatermgnt.authentication.service.AuthenticationService;
import com.theatermgnt.theatermgnt.common.dto.response.ApiResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.util.StringUtils;

@RequestMapping("/auth")
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationController {
    AuthenticationService authenticationService;

    @PostMapping("/admin/login")
    ApiResponse<AuthenticationResponse> adminLogin(@RequestBody AuthenticationRequest request, HttpServletResponse response) {
        log.info("Admin login attempt for: {}", request.getLoginIdentifier());
        var result = authenticationService.authenticate(request, AccountType.INTERNAL);
        setAuthCookies(response, result.getToken());
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    @PostMapping("/customer/login")
    ApiResponse<AuthenticationResponse> customerLogin(@RequestBody AuthenticationRequest request, HttpServletResponse response) {
        log.info("Customer login attempt for: {}", request.getLoginIdentifier());
        var result = authenticationService.authenticate(request, AccountType.CUSTOMER);
        setAuthCookies(response, result.getToken());
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request)
            throws ParseException, JOSEException {
        if (request == null || !StringUtils.hasText(request.getToken())) {
            return ApiResponse.<IntrospectResponse>builder()
                    .result(IntrospectResponse.builder().valid(false).build())
                    .build();
        }
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder().result(result).build();
    }

    private void setAuthCookies(HttpServletResponse response, String jwtToken) {
        String safeJwtToken = URLEncoder.encode(jwtToken, StandardCharsets.UTF_8);
        // JWT cookie - httpOnly, SameSite=Lax for demo (will change to Strict for defense)
        ResponseCookie jwtCookie = ResponseCookie.from("access_token", safeJwtToken)
                .httpOnly(true)
                .sameSite("None")  // None = cross-site cookie sent → CSRF attack surface (demo)
                .secure(true)     // Chrome accepts Secure cookies on localhost even over HTTP
                .path("/")
                .maxAge(3600)     // 1 hour
                .build();

        // CSRF token cookie - NOT httpOnly (JS can read)
        String csrfToken = generateCsrfToken();
        ResponseCookie csrfCookie = ResponseCookie.from("XSRF-TOKEN", csrfToken)
                .httpOnly(false)  // JS can read this
                .sameSite("Lax")
                .secure(false)
                .path("/")
                .maxAge(3600)
                .build();

        response.addHeader("Set-Cookie", jwtCookie.toString());
        response.addHeader("Set-Cookie", csrfCookie.toString());
    }

    private String generateCsrfToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(
            @RequestBody(required = false) LogoutRequest request,
            @org.springframework.web.bind.annotation.CookieValue(value = "access_token", required = false) String cookieToken,
            HttpServletResponse response) throws ParseException, JOSEException {

        // Blacklist token từ cookie nếu có (ưu tiên cookie)
        String tokenToInvalidate = null;
        if (cookieToken != null && !cookieToken.isBlank()) {
            tokenToInvalidate = java.net.URLDecoder.decode(cookieToken, StandardCharsets.UTF_8);
        } else if (request != null && StringUtils.hasText(request.getToken())) {
            tokenToInvalidate = request.getToken();
        }

        if (tokenToInvalidate != null) {
            authenticationService.logout(LogoutRequest.builder().token(tokenToInvalidate).build());
        }

        // Clear cookies
        clearAuthCookies(response);
        return ApiResponse.<Void>builder().build();
    }

    private void clearAuthCookies(HttpServletResponse response) {
        ResponseCookie clearJwt = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .sameSite("None")
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();

        ResponseCookie clearCsrf = ResponseCookie.from("XSRF-TOKEN", "")
                .httpOnly(false)
                .sameSite("Lax")
                .secure(false)
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader("Set-Cookie", clearJwt.toString());
        response.addHeader("Set-Cookie", clearCsrf.toString());
    }

    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> refreshToken(@RequestBody RefreshTokenRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.refreshToken(request);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }
}
