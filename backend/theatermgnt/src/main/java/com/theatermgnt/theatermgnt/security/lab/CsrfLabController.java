package com.theatermgnt.theatermgnt.security.lab;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/security-lab/csrf")
@RequiredArgsConstructor
@Slf4j
public class CsrfLabController {

    private final DemoSessionService demoSessionService;
    private final CsrfTokenStore csrfTokenStore;

    // Lab 1: CSRF vulnerability with no defenses
    // Attack: Form HTML từ attacker site submit POST, không có CSRF token, không SameSite protection.
    @PostMapping("/lab1/update-email")
    public ResponseEntity<?> lab1Vulnerable(
            @CookieValue("DEMO_SESSION") String session,
            @RequestParam String email) {
        // chỉ check session cookie, không có gì khác
        String userId = demoSessionService.getUserId(session);
        if (userId == null) {
            return ResponseEntity.status(401).body("Invalid session");
        }
        // Simulate updating email
        log.info("Lab1: Updated email for user {} to {}", userId, email);
        return ResponseEntity.ok("Email updated to: " + email);
    }

    // Lab 2: CSRF where token validation depends on request method (PRACTITIONER)
    // Attack: Server chỉ validate CSRF token khi POST, không validate khi GET. Đổi method thành GET.
    @RequestMapping(value = "/lab2/update-email",
                    method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<?> lab2(@CookieValue("DEMO_SESSION") String session,
            @RequestParam String email,
            @RequestParam(required = false) String csrfToken,
            HttpServletRequest request) {
        String userId = demoSessionService.getUserId(session);
        if (userId == null) {
            return ResponseEntity.status(401).body("Invalid session");
        }

        if (request.getMethod().equals("POST")) {
            // validate csrfToken... nhưng GET không check
            if (csrfToken == null || !csrfTokenStore.isValid(session, csrfToken)) {
                return ResponseEntity.status(403).body("Invalid CSRF token");
            }
        }
        // Simulate updating email
        log.info("Lab2: Updated email for user {} to {}", userId, email);
        return ResponseEntity.ok("Updated");
    }

    // Lab 3: CSRF where token validation depends on token being present (PRACTITIONER)
    // Attack: Xóa tham số csrfToken khỏi request → server bỏ qua validate.
    @PostMapping("/lab3/update-email")
    public ResponseEntity<?> lab3(@CookieValue("DEMO_SESSION") String session,
            @RequestParam String email,
            @RequestParam(required = false) String csrfToken) {
        String userId = demoSessionService.getUserId(session);
        if (userId == null) {
            return ResponseEntity.status(401).body("Invalid session");
        }

        if (csrfToken != null && !csrfTokenStore.isValid(session, csrfToken)) {
            return ResponseEntity.status(403).body("Invalid CSRF token");
        }
        // ← BUG: nếu csrfToken == null thì không check gì
        // Simulate updating email
        log.info("Lab3: Updated email for user {} to {}", userId, email);
        return ResponseEntity.ok("Updated");
    }

    // Lab 11: CSRF where Referer validation depends on header being present (PRACTITIONER)
    // Attack: Xóa Referer header → server bỏ qua validate.
    @PostMapping("/lab11/update-email")
    public ResponseEntity<?> lab11(@CookieValue("DEMO_SESSION") String session,
            @RequestParam String email,
            @RequestHeader(value = "Referer", required = false) String referer) {
        String userId = demoSessionService.getUserId(session);
        if (userId == null) {
            return ResponseEntity.status(401).body("Invalid session");
        }

        if (referer != null && !referer.startsWith("http://localhost:3000")) {
            return ResponseEntity.status(403).body("Invalid Referer");
        }
        // ← BUG: nếu Referer == null thì không check
        // Simulate updating email
        log.info("Lab11: Updated email for user {} to {}", userId, email);
        return ResponseEntity.ok("Updated");
    }

    // Defense endpoint: Proper CSRF token bắt buộc, tied to session, SameSite=Strict
    @PostMapping("/defended/update-email")
    public ResponseEntity<?> defended(
            @CookieValue("DEMO_SESSION") String session,
            @RequestHeader("X-XSRF-TOKEN") String csrfHeader,
            @CookieValue("XSRF-TOKEN") String csrfCookie,
            @RequestParam String email) {
        String userId = demoSessionService.getUserId(session);
        if (userId == null) {
            return ResponseEntity.status(401).body("Invalid session");
        }

        // Double submit cookie: header phải match cookie (attacker không đọc được cookie)
        if (!csrfHeader.equals(csrfCookie)) {
            return ResponseEntity.status(403).body("CSRF validation failed");
        }
        // Simulate updating email
        log.info("Defended: Updated email for user {} to {}", userId, email);
        return ResponseEntity.ok("Updated");
    }
}