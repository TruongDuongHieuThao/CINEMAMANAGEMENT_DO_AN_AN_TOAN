package com.theatermgnt.theatermgnt.configuration;

import java.text.ParseException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import com.theatermgnt.theatermgnt.authentication.dto.request.IntrospectRequest;
import com.theatermgnt.theatermgnt.authentication.service.AuthenticationService;

@Component
public class CustomJwtDecoder implements JwtDecoder {
    @Value("${jwt.signerKey}")
    private String signerKey;

    @Autowired
    private AuthenticationService authenticationService;

    private NimbusJwtDecoder nimbusJwtDecoder = null;

    private byte[] signerKeyBytes() {
        try {
            return MessageDigest.getInstance("SHA-512").digest(signerKey.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-512 algorithm not available", e);
        }
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            // BLOCK: reject nếu có jwk, jku, hoặc kid header
            SignedJWT signedJWT = SignedJWT.parse(token);
            var header = signedJWT.getHeader();
            if (header.getJWK() != null || header.getJWKURL() != null) {
                throw new JwtException("JWT header injection detected");
            }
            if (header.getKeyID() != null) {
                throw new JwtException("Unexpected kid header");
            }

            var response = authenticationService.introspect(
                    IntrospectRequest.builder().token(token).build());
            if (!response.isValid()) throw new JwtException("Invalid token");

        } catch (ParseException e) {
            throw new JwtException("Malformed token");
        } catch (JOSEException e) {
            throw new JwtException(e.getMessage());
        }
        if (Objects.isNull(nimbusJwtDecoder)) {
            SecretKeySpec secretKeySpec = new SecretKeySpec(signerKeyBytes(), "HS512");
            nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKeySpec)
                    .macAlgorithm(MacAlgorithm.HS512)
                    .build();
        }

        return nimbusJwtDecoder.decode(token);
    }
}
