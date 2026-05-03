package com.theatermgnt.theatermgnt.security.lab;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;

import lombok.extern.slf4j.Slf4j;

/**
 * Chứa logic verify JWT cho các lab demo:
 *  - verifySignature        → Lab 1 defense: verify chữ ký HS512
 *  - verifyWithAlgorithmPin → Lab 2 defense: reject alg:none, pin HS512
 *  - verifyRejectingHeaders → Lab 4 defense: reject jwk/jku/kid header injection
 */
@Service
@Slf4j
public class JwtLabService {

    @Value("${jwt.signerKey}")
    private String signerKey;

    private byte[] keyBytes() {
        try {
            return MessageDigest.getInstance("SHA-512")
                    .digest(signerKey.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-512 not available", e);
        }
    }

    private String decodeToken(String rawCookieValue) {
        return URLDecoder.decode(rawCookieValue, StandardCharsets.UTF_8);
    }

    // ── Lab 1 Defense: verify chữ ký (không chỉ decode) ───────────────────────
    public ResponseEntity<?> verifySignature(String rawToken, String labId) {
        if (rawToken == null || rawToken.isBlank()) {
            return ResponseEntity.status(401).body(blocked(labId, "No token in cookie"));
        }
        try {
            String token = decodeToken(rawToken);
            SignedJWT jwt = SignedJWT.parse(token);

            MACVerifier verifier = new MACVerifier(keyBytes());
            boolean signatureOk = jwt.verify(verifier);

            if (!signatureOk) {
                log.warn("[{}] Signature verification FAILED — forged token rejected", labId);
                return ResponseEntity.status(401).body(blocked(labId,
                        "Chữ ký không hợp lệ — token bị giả mạo bị từ chối"));
            }
            if (jwt.getJWTClaimsSet().getExpirationTime().before(new Date())) {
                return ResponseEntity.status(401).body(blocked(labId, "Token đã hết hạn"));
            }

            return ResponseEntity.ok(accepted(labId,
                    "Chữ ký HS512 hợp lệ — server-side key xác thực thành công",
                    jwt.getJWTClaimsSet().getSubject(),
                    jwt.getJWTClaimsSet().getStringClaim("scope")));

        } catch (Exception e) {
            log.warn("[{}] Token parse/verify error: {}", labId, e.getMessage());
            return ResponseEntity.status(401).body(blocked(labId, "Parse error: " + e.getMessage()));
        }
    }

    // ── Lab 2 Defense: pin algorithm = HS512, reject alg:none ─────────────────
    public ResponseEntity<?> verifyWithAlgorithmPin(String rawToken, String labId) {
        if (rawToken == null || rawToken.isBlank()) {
            return ResponseEntity.status(401).body(blocked(labId, "No token in cookie"));
        }
        try {
            String token = decodeToken(rawToken);
            SignedJWT jwt = SignedJWT.parse(token);

            // Defense 1: algorithm pinning
            JWSAlgorithm alg = jwt.getHeader().getAlgorithm();
            if (!JWSAlgorithm.HS512.equals(alg)) {
                log.warn("[{}] Algorithm pinning triggered — got '{}', expected HS512", labId, alg);
                return ResponseEntity.status(401).body(blocked(labId,
                        "Algorithm pinning: nhận được alg='" + alg
                        + "' — chỉ chấp nhận HS512. alg:none bị từ chối."));
            }

            // Defense 2: verify signature
            MACVerifier verifier = new MACVerifier(keyBytes());
            if (!jwt.verify(verifier)) {
                return ResponseEntity.status(401).body(blocked(labId, "Chữ ký không hợp lệ"));
            }

            return ResponseEntity.ok(accepted(labId,
                    "Algorithm pinned HS512 + chữ ký xác thực thành công",
                    jwt.getJWTClaimsSet().getSubject(),
                    jwt.getJWTClaimsSet().getStringClaim("scope")));

        } catch (Exception e) {
            log.warn("[{}] Token parse/verify error: {}", labId, e.getMessage());
            return ResponseEntity.status(401).body(blocked(labId, e.getMessage()));
        }
    }

    // ── Lab 4 Defense: reject jwk / jku / kid header injection ────────────────
    public ResponseEntity<?> verifyRejectingHeaders(String rawToken, String labId) {
        if (rawToken == null || rawToken.isBlank()) {
            return ResponseEntity.status(401).body(blocked(labId, "No token in cookie"));
        }
        try {
            String token = decodeToken(rawToken);
            SignedJWT jwt = SignedJWT.parse(token);
            var header = jwt.getHeader();

            // Defense 1: reject JWK header injection
            JWK embeddedJwk = header.getJWK();
            if (embeddedJwk != null) {
                log.warn("[{}] JWK header injection detected — rejecting token", labId);
                return ResponseEntity.status(401).body(blocked(labId,
                        "JWK header injection phát hiện: server từ chối dùng public key do attacker nhúng vào header"));
            }

            // Defense 2: reject jku (JWK Set URL)
            if (header.getJWKURL() != null) {
                log.warn("[{}] jku header injection detected", labId);
                return ResponseEntity.status(401).body(blocked(labId,
                        "jku header injection phát hiện: từ chối fetch key từ URL của attacker"));
            }

            // Defense 3: reject kid (key ID — có thể bị dùng cho SQL/path injection)
            if (header.getKeyID() != null) {
                log.warn("[{}] Unexpected kid header detected", labId);
                return ResponseEntity.status(401).body(blocked(labId,
                        "kid header không mong đợi — từ chối để tránh key confusion attack"));
            }

            // Defense 4: algorithm pinning
            JWSAlgorithm alg = header.getAlgorithm();
            if (!JWSAlgorithm.HS512.equals(alg)) {
                return ResponseEntity.status(401).body(blocked(labId,
                        "Algorithm pinning: '" + alg + "' không được chấp nhận"));
            }

            // Defense 5: verify signature with server key
            MACVerifier verifier = new MACVerifier(keyBytes());
            if (!jwt.verify(verifier)) {
                return ResponseEntity.status(401).body(blocked(labId, "Chữ ký không hợp lệ"));
            }

            return ResponseEntity.ok(accepted(labId,
                    "Không có header injection + HS512 hợp lệ — tất cả defenses passed",
                    jwt.getJWTClaimsSet().getSubject(),
                    jwt.getJWTClaimsSet().getStringClaim("scope")));

        } catch (Exception e) {
            log.warn("[{}] Token parse/verify error: {}", labId, e.getMessage());
            return ResponseEntity.status(401).body(blocked(labId, e.getMessage()));
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Map<String, Object> blocked(String labId, String reason) {
        return Map.of(
            "lab",    labId,
            "result", "⛔ ATTACK BLOCKED",
            "reason", reason
        );
    }

    private Map<String, Object> accepted(String labId, String defense, String sub, String scope) {
        return Map.of(
            "lab",     labId,
            "result",  "✅ LEGITIMATE TOKEN ACCEPTED",
            "defense", defense,
            "sub",     sub != null ? sub : "",
            "scope",   scope != null ? scope : ""
        );
    }
}
