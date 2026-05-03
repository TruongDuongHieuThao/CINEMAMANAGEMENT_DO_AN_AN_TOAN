package com.theatermgnt.theatermgnt.security.lab;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nimbusds.jwt.SignedJWT;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Mô phỏng các endpoint thực tế của ứng dụng rạp phim NHƯNG có lỗ hổng JWT (không verify signature).
 * Dùng để demo tấn công có impact thực tế hơn endpoint /whoami.
 *
 * Tất cả endpoint dưới đây đều CÓ LỖ HỔNG (vulnerable) — không verify JWT signature.
 * So sánh với production endpoint thật sẽ bị reject vì CustomJwtDecoder verify đúng cách.
 *
 * Endpoints:
 *   GET    /security-lab/jwt/real/my-profile        — xem profile của "mình" (dựa vào sub trong JWT)
 *   GET    /security-lab/jwt/real/admin/customers    — xem danh sách toàn bộ khách hàng (ROLE_ADMIN)
 *   GET    /security-lab/jwt/real/admin/bookings     — xem toàn bộ booking (ROLE_ADMIN)
 *   PUT    /security-lab/jwt/real/my-profile/email   — đổi email "của mình"
 *   DELETE /security-lab/jwt/real/admin/users/{id}   — xóa user (ROLE_ADMIN)
 */
@RestController
@RequestMapping("/security-lab/jwt/real")
@RequiredArgsConstructor
@Slf4j
public class JwtRealWorldLabController {

    // ── Dữ liệu giả lập (in-memory) ──────────────────────────────────────────

    private static final List<Map<String, Object>> FAKE_CUSTOMERS = List.of(
        Map.of("id", "cust-001", "name", "Nguyễn Văn An",   "email", "an@example.com",     "phone", "0901234567", "points", 150),
        Map.of("id", "cust-002", "name", "Trần Thị Bình",   "email", "binh@example.com",   "phone", "0912345678", "points", 320),
        Map.of("id", "cust-003", "name", "Lê Văn Cường",    "email", "cuong@example.com",  "phone", "0923456789", "points", 80),
        Map.of("id", "cust-004", "name", "Phạm Thị Dung",   "email", "dung@example.com",   "phone", "0934567890", "points", 500),
        Map.of("id", "cust-005", "name", "Hoàng Văn Em",    "email", "em@example.com",     "phone", "0945678901", "points", 210)
    );

    private static final List<Map<String, Object>> FAKE_BOOKINGS = List.of(
        Map.of("id", "bk-001", "customer_id", "cust-001", "movie", "Avengers: Endgame", "seats", 2, "total", 180000, "status", "CONFIRMED"),
        Map.of("id", "bk-002", "customer_id", "cust-002", "movie", "Spider-Man: No Way Home", "seats", 3, "total", 270000, "status", "CONFIRMED"),
        Map.of("id", "bk-003", "customer_id", "cust-001", "movie", "Doctor Strange", "seats", 1, "total", 90000, "status", "CANCELLED"),
        Map.of("id", "bk-004", "customer_id", "cust-003", "movie", "Thor: Love and Thunder", "seats", 4, "total", 360000, "status", "CONFIRMED"),
        Map.of("id", "bk-005", "customer_id", "cust-004", "movie", "Black Panther: Wakanda Forever", "seats", 2, "total", 200000, "status", "PENDING")
    );

    // ── Helper: decode JWT mà KHÔNG verify signature ──────────────────────────

    private Map<String, Object> decodeWithoutVerify(String rawToken) throws Exception {
        String token = URLDecoder.decode(rawToken, StandardCharsets.UTF_8);
        String[] parts = token.split("\\.");
        if (parts.length < 2) throw new IllegalArgumentException("Invalid JWT format");

        // ⚠️ CHỈ decode base64, KHÔNG verify signature — đây là lỗ hổng
        String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));

        // Parse thủ công để lấy sub và scope
        String sub   = extractClaim(payloadJson, "sub");
        String scope = extractClaim(payloadJson, "scope");
        String jti   = extractClaim(payloadJson, "jti");

        return Map.of("sub", sub != null ? sub : "", "scope", scope != null ? scope : "", "jti", jti != null ? jti : "");
    }

    private String extractClaim(String json, String key) {
        // Simple regex-free extraction cho demo
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start == -1) return null;
        start += search.length();
        int end = json.indexOf("\"", start);
        return end == -1 ? null : json.substring(start, end);
    }

    private boolean isAdmin(Map<String, Object> claims) {
        String scope = (String) claims.get("scope");
        return scope != null && (scope.contains("ROLE_ADMIN") || scope.contains("administrator"));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  1. GET /my-profile — xem thông tin cá nhân
    //     Vulnerable: sub trong JWT không được verify → attacker đặt sub=bất kỳ UUID
    // ═══════════════════════════════════════════════════════════════════════════

    @GetMapping("/my-profile")
    public ResponseEntity<?> getMyProfile(
            @CookieValue(value = "access_token", required = false) String rawToken) {

        if (rawToken == null || rawToken.isBlank()) {
            return ResponseEntity.status(401).body(error("Chưa đăng nhập"));
        }
        try {
            Map<String, Object> claims = decodeWithoutVerify(rawToken);
            String sub = (String) claims.get("sub");

            log.warn("[real/my-profile VULNERABLE] Trả profile cho sub={} mà không verify chữ ký!", sub);

            // Mô phỏng: tìm customer theo sub (trong thực tế query DB)
            Map<String, Object> profile = FAKE_CUSTOMERS.stream()
                    .filter(c -> c.get("id").equals(sub))
                    .findFirst()
                    .orElse(Map.of(
                        "id",    sub,
                        "name",  sub.equals("administrator") ? "👑 Administrator Account" : "Unknown User (" + sub + ")",
                        "email", sub.equals("administrator") ? "admin@theater-mgnt.com" : "unknown@example.com",
                        "role",  sub.equals("administrator") ? "SUPER_ADMIN" : "CUSTOMER",
                        "note",  "⚠️ Profile lấy theo sub trong JWT mà không verify chữ ký!"
                    ));

            return ResponseEntity.ok(Map.of(
                "endpoint",      "GET /real/my-profile",
                "vulnerability", "⚠️ JWT signature không được verify — sub bị tin tưởng hoàn toàn",
                "claimed_sub",   sub,
                "claimed_scope", claims.get("scope"),
                "profile",       profile,
                "attack_note",   sub.equals("administrator")
                    ? "🔴 ATTACK SUCCESS: Attacker đang xem profile của administrator!"
                    : "Token hợp lệ — đây là profile thật của bạn"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  2. GET /admin/customers — xem toàn bộ danh sách khách hàng (chỉ ADMIN)
    //     Vulnerable: scope trong JWT không được verify → attacker đặt scope=ROLE_ADMIN
    // ═══════════════════════════════════════════════════════════════════════════

    @GetMapping("/admin/customers")
    public ResponseEntity<?> getAllCustomers(
            @CookieValue(value = "access_token", required = false) String rawToken) {

        if (rawToken == null || rawToken.isBlank()) {
            return ResponseEntity.status(401).body(error("Chưa đăng nhập"));
        }
        try {
            Map<String, Object> claims = decodeWithoutVerify(rawToken);

            // ⚠️ VULNERABILITY: chỉ check scope trong payload, không verify signature
            // → attacker forge scope=ROLE_ADMIN là qua được
            if (!isAdmin(claims)) {
                return ResponseEntity.status(403).body(Map.of(
                    "error",   "Không có quyền truy cập",
                    "scope",   claims.get("scope"),
                    "hint",    "Endpoint này yêu cầu ROLE_ADMIN trong JWT — nhưng JWT không được verify!"
                ));
            }

            log.warn("[real/admin/customers VULNERABLE] Admin data trả về cho sub={} mà không verify chữ ký!",
                    claims.get("sub"));

            return ResponseEntity.ok(Map.of(
                "endpoint",      "GET /real/admin/customers",
                "vulnerability", "⚠️ JWT signature không verify — attacker forge ROLE_ADMIN để xem dữ liệu nhạy cảm",
                "claimed_sub",   claims.get("sub"),
                "claimed_scope", claims.get("scope"),
                "attack_note",   "🔴 ATTACK SUCCESS: Attacker có thể xem toàn bộ thông tin 5 khách hàng!",
                "total",         FAKE_CUSTOMERS.size(),
                "customers",     FAKE_CUSTOMERS
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  3. GET /admin/bookings — xem toàn bộ booking (chỉ ADMIN)
    // ═══════════════════════════════════════════════════════════════════════════

    @GetMapping("/admin/bookings")
    public ResponseEntity<?> getAllBookings(
            @CookieValue(value = "access_token", required = false) String rawToken) {

        if (rawToken == null || rawToken.isBlank()) {
            return ResponseEntity.status(401).body(error("Chưa đăng nhập"));
        }
        try {
            Map<String, Object> claims = decodeWithoutVerify(rawToken);

            if (!isAdmin(claims)) {
                return ResponseEntity.status(403).body(Map.of(
                    "error", "Không có quyền — cần ROLE_ADMIN",
                    "scope", claims.get("scope")
                ));
            }

            log.warn("[real/admin/bookings VULNERABLE] Booking data trả về mà không verify chữ ký!");

            return ResponseEntity.ok(Map.of(
                "endpoint",      "GET /real/admin/bookings",
                "vulnerability", "⚠️ JWT signature không verify — attacker xem được lịch sử booking của tất cả user",
                "attack_note",   "🔴 ATTACK SUCCESS: Toàn bộ 5 booking bị lộ!",
                "total",         FAKE_BOOKINGS.size(),
                "bookings",      FAKE_BOOKINGS
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  4. PUT /my-profile/email — đổi email (dựa vào sub trong JWT)
    //     Vulnerable: attacker đặt sub = UUID của victim → đổi email của người khác
    // ═══════════════════════════════════════════════════════════════════════════

    @PutMapping("/my-profile/email")
    public ResponseEntity<?> updateEmail(
            @CookieValue(value = "access_token", required = false) String rawToken,
            @RequestParam String email) {

        if (rawToken == null || rawToken.isBlank()) {
            return ResponseEntity.status(401).body(error("Chưa đăng nhập"));
        }
        try {
            Map<String, Object> claims = decodeWithoutVerify(rawToken);
            String sub = (String) claims.get("sub");

            log.warn("[real/my-profile/email VULNERABLE] Đổi email của sub={} thành {} mà không verify chữ ký!",
                    sub, email);

            return ResponseEntity.ok(Map.of(
                "endpoint",      "PUT /real/my-profile/email",
                "vulnerability", "⚠️ Attacker đặt sub = UUID của victim → đổi email của người khác",
                "target_user_id", sub,
                "new_email",     email,
                "attack_note",   sub.equals("administrator")
                    ? "🔴 ATTACK SUCCESS: Email của administrator bị đổi thành " + email
                    : "🔴 ATTACK SUCCESS: Email của user " + sub + " bị đổi thành " + email,
                "simulated_result", "UPDATE accounts SET email = '" + email + "' WHERE id = '" + sub + "'"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  5. DELETE /admin/users/{id} — xóa user (chỉ ADMIN)
    //     Vulnerable: forge ROLE_ADMIN → xóa bất kỳ user nào
    // ═══════════════════════════════════════════════════════════════════════════

    @DeleteMapping("/admin/users/{id}")
    public ResponseEntity<?> deleteUser(
            @CookieValue(value = "access_token", required = false) String rawToken,
            @PathVariable String id) {

        if (rawToken == null || rawToken.isBlank()) {
            return ResponseEntity.status(401).body(error("Chưa đăng nhập"));
        }
        try {
            Map<String, Object> claims = decodeWithoutVerify(rawToken);

            if (!isAdmin(claims)) {
                return ResponseEntity.status(403).body(Map.of(
                    "error", "Không có quyền — cần ROLE_ADMIN",
                    "scope", claims.get("scope")
                ));
            }

            log.warn("[real/admin/users VULNERABLE] Xóa user id={} mà không verify chữ ký!", id);

            return ResponseEntity.ok(Map.of(
                "endpoint",      "DELETE /real/admin/users/" + id,
                "vulnerability", "⚠️ Forge ROLE_ADMIN → xóa bất kỳ user nào trong hệ thống",
                "deleted_user_id", id,
                "attack_note",   "🔴 ATTACK SUCCESS: User " + id + " đã bị xóa bởi attacker!",
                "simulated_result", "DELETE FROM accounts WHERE id = '" + id + "'"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  6. So sánh: endpoint thật của production sẽ reject forged token
    // ═══════════════════════════════════════════════════════════════════════════

    @GetMapping("/compare")
    public ResponseEntity<?> compare() {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("title", "So sánh Vulnerable Lab vs Production Endpoint");
        result.put("attack_token", "forged_token_lab1 (sub=administrator, scope=ROLE_ADMIN, chữ ký sai)");

        result.put("vulnerable_lab_endpoints", Map.of(
            "GET /security-lab/jwt/real/my-profile",       "✅ ATTACK SUCCESS — trả profile của administrator",
            "GET /security-lab/jwt/real/admin/customers",  "✅ ATTACK SUCCESS — lộ toàn bộ danh sách khách hàng",
            "GET /security-lab/jwt/real/admin/bookings",   "✅ ATTACK SUCCESS — lộ toàn bộ booking",
            "PUT /security-lab/jwt/real/my-profile/email", "✅ ATTACK SUCCESS — đổi email của bất kỳ user",
            "DELETE /security-lab/jwt/real/admin/users/x", "✅ ATTACK SUCCESS — xóa user"
        ));

        result.put("production_endpoints_with_defense", Map.of(
            "GET /customers/myInfo",   "⛔ 401 — CustomJwtDecoder verify signature → reject forged token",
            "GET /bookings/{id}",      "⛔ 401 — CustomJwtDecoder verify signature → reject forged token",
            "GET /staffs",             "⛔ 401 — CustomJwtDecoder verify signature → reject forged token"
        ));

        result.put("defense_code", Map.of(
            "file",   "CustomJwtDecoder.java",
            "method", "decode(String token)",
            "key_check", "signedJWT.verify(new MACVerifier(serverKey)) → nếu false → throw JwtException"
        ));

        return ResponseEntity.ok(result);
    }

    private Map<String, Object> error(String msg) {
        return Map.of("error", msg);
    }
}
