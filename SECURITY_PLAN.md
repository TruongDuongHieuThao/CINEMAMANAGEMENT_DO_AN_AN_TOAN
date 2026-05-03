# Security Implementation Plan — v2
## Đồ án ATBM TMĐT · CSRF Attack + JWT Defense · Burp Suite

---

## Kiến trúc tổng thể

```
┌─────────────────────────────────────────────────────────────┐
│  VICTIM APP  http://localhost:3000 (client) / :5173 (admin) │
│              http://localhost:8080 (Spring Boot API)         │
└─────────────────────────────────────────────────────────────┘
        ↑ tất cả traffic đi qua
┌─────────────────────┐
│  BURP SUITE PROXY   │  listen :8888
│  (intercept/replay) │
└─────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│  ATTACKER SITE  http://localhost:4000  (Docker nginx)       │
│  Phục vụ CSRF PoC HTML, script attack                       │
└─────────────────────────────────────────────────────────────┘
```

**Thay đổi cốt lõi so với plan cũ:**
- JWT lưu trong **httpOnly cookie** (thay vì localStorage)
- Tạo CSRF attack surface tự nhiên (cookie tự gửi cross-site)
- Burp Suite intercept cookie + Generate CSRF PoC trực tiếp
- Mỗi lab có endpoint `/lab-N/vulnerable` và `/lab-N/defended`

---

## Phần 1 — JWT Defense: Cấu hình chống tất cả JWT labs

### 1.1 Thay đổi lưu trữ token: localStorage → httpOnly Cookie

**Trước (hiện tại):**
```
POST /auth/customer/login
← Response body: { "token": "eyJ..." }
Frontend: localStorage.setItem("token", ...)
Requests: Authorization: Bearer eyJ...
```

**Sau:**
```
POST /auth/customer/login
← Set-Cookie: access_token=eyJ...; HttpOnly; SameSite=Lax; Path=/; Max-Age=3600
← Set-Cookie: XSRF-TOKEN=random; SameSite=Lax; Path=/   (NOT httpOnly — JS đọc được)
Requests: Cookie: access_token=eyJ...  (tự động)
          X-XSRF-TOKEN: random        (frontend đọc cookie gửi lên header)
```

**Files cần sửa:**
- `authentication/service/AuthenticationService.java` → trả về `ResponseCookie` thay vì token string
- `authentication/controller/AuthenticationController.java` → `HttpServletResponse` để set cookie
- `configuration/CustomJwtDecoder.java` → đọc JWT từ cookie thay vì Authorization header
- `configuration/SecurityConfig.java` → cấu hình `bearerTokenResolver` đọc từ cookie

### 1.2 Map từng JWT lab → Defense implement

#### Lab 1 · JWT authentication bypass via unverified signature (APPRENTICE)
**Attack:** Server decode JWT nhưng không gọi `verify()` — chỉ trust payload.
**Demo Burp:** Repeater → sửa payload `"sub": "admin-id"` → gửi lên `/lab1/vulnerable` → 200 OK.
**Defense đã có:** `signedJWT.verify(verifier)` trong `AuthenticationService.verifyToken()`.
**Code cần thêm:** Thêm endpoint `/security-lab/jwt/lab1/vulnerable` (naive decode, no verify).

```java
// VulnerableJwtLab1.java
@GetMapping("/security-lab/jwt/lab1/whoami")
public ResponseEntity<?> lab1Vulnerable(@CookieValue("access_token") String token) {
    // CHỈ decode, KHÔNG verify signature
    String[] parts = token.split("\\.");
    String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
    return ResponseEntity.ok(payload); // attacker forge bất kỳ sub/scope nào
}
```

#### Lab 2 · JWT authentication bypass via flawed signature verification (APPRENTICE)
**Attack:** Server accept `alg: none` — bỏ signature phần 3 của JWT.
**Demo Burp:** JWT Editor extension → Algorithm: None → send → `/lab2/vulnerable` → 200 OK.
**Defense:** Explicit check TRƯỚC khi verify.

```java
// AuthenticationService.verifyToken() — THÊM VÀO
private SignedJWT verifyToken(String token, boolean isRefresh) {
    SignedJWT signedJWT = SignedJWT.parse(token);

    // BLOCK: reject bất kỳ alg nào không phải HS512
    JWSAlgorithm alg = signedJWT.getHeader().getAlgorithm();
    if (!JWSAlgorithm.HS512.equals(alg)) {
        log.warn("Rejected JWT with unexpected algorithm: {}", alg);
        throw new AppException(ErrorCode.UNAUTHENTICATED);
    }
    // ... phần còn lại giữ nguyên
}
```

#### Lab 3 · JWT authentication bypass via weak signing key (PRACTITIONER)
**Attack:** Key yếu ("secret", "password") → hashcat crack trong vài giây → forge JWT.
**Demo Burp + hashcat:**
```bash
# Crack key từ token bắt được trong Burp
hashcat -a 0 -m 16500 captured_token.txt /usr/share/wordlists/rockyou.txt

# Sau khi có key → dùng JWT Editor trong Burp để sign lại token mới
```
**Defense:** Key 256-bit random từ env var, không hardcode.

```yaml
# application.yml — sau khi sửa
jwt:
  signerKey: ${JWT_SIGNER_KEY}  # PHẢI có trong env, không có fallback
```

```bash
# Generate key đúng chuẩn
openssl rand -base64 32
# → dùng output này làm JWT_SIGNER_KEY trong .env
```

#### Lab 4 · JWT authentication bypass via jwk header injection (PRACTITIONER)
**Attack:** Header JWT chứa `"jwk": {"kty":"RSA", "n":"...", "e":"AQAB"}` — server dùng key này verify.
**Demo Burp:** JWT Editor → Attack → Embedded JWK → Generate key pair → Send → `/lab4/vulnerable`.
**Defense:** `NimbusJwtDecoder.withSecretKey()` KHÔNG xử lý JWK header — đã block tự nhiên với HMAC.

```java
// CustomJwtDecoder.java — THÊM validation
@Override
public Jwt decode(String token) throws JwtException {
    try {
        SignedJWT signedJWT = SignedJWT.parse(token);

        // BLOCK: reject nếu có jwk hoặc jku header
        JWSHeader header = signedJWT.getHeader();
        if (header.getJWK() != null || header.getJWKURL() != null) {
            log.warn("Rejected JWT with jwk/jku header injection attempt");
            throw new JwtException("JWT header injection detected");
        }
        // BLOCK: reject nếu có kid header (app không dùng kid)
        if (header.getKeyID() != null) {
            log.warn("Rejected JWT with unexpected kid header: {}", header.getKeyID());
            throw new JwtException("Unexpected kid header");
        }
    } catch (ParseException e) {
        throw new JwtException("Malformed token");
    }
    // ... phần decode bình thường
}
```

#### Lab 5 · JWT authentication bypass via jku header injection (PRACTITIONER)
**Attack:** Header `"jku": "https://attacker.com/.well-known/jwks.json"` → server fetch key từ URL attacker.
**Demo Burp:** JWT Editor → Attack → JKU Bypass → trỏ về Burp Collaborator server → Send.
**Defense:** Cùng check `header.getJWKURL() != null` ở lab 4 — đã cover.

#### Lab 6 · JWT authentication bypass via kid header path traversal (PRACTITIONER)
**Attack:** `"kid": "../../dev/null"` hoặc `"kid": "../../../etc/passwd"` → server đọc file làm key.
**Demo Burp:** JWT Editor → sửa kid header → sign với empty string / file content đã biết.
**Defense:** Cùng check `header.getKeyID() != null` ở lab 4 — đã cover.

#### Lab 7 & 8 · Algorithm confusion (EXPERT)
**Attack:** Đổi `alg` từ RS256 sang HS256, sign bằng public key (vì server verify HS256 bằng public key).
**Defense:** App chỉ dùng HS512 (symmetric), không bao giờ dùng RS256 → algorithm confusion không áp dụng.
Explicit check ở lab 2 (`JWSAlgorithm.HS512.equals(alg)`) đã block hoàn toàn.

### 1.3 Tóm tắt: Trạng thái sau khi implement

| Lab | Tên | Trạng thái |
|---|---|---|
| Lab 1 | Unverified signature | ✅ verify() bắt buộc |
| Lab 2 | alg:none bypass | ✅ explicit alg check = HS512 |
| Lab 3 | Weak signing key | ✅ 256-bit key từ env var |
| Lab 4 | JWK injection | ✅ reject jwk header |
| Lab 5 | JKU injection | ✅ reject jku header |
| Lab 6 | KID path traversal | ✅ reject kid header |
| Lab 7 | Algorithm confusion | ✅ HS512 only, không dùng RS/PS |
| Lab 8 | Algorithm confusion no key | ✅ cùng defense trên |

---

## Phần 2 — CSRF Attack: Lab scenarios

### 2.1 Điều kiện Burp Suite CSRF PoC hoạt động

**Vấn đề SameSite cookie trên Chrome hiện đại:**
- Chrome default `SameSite=Lax` → block cross-site POST form
- Cần set cookie với `SameSite=None` cho demo attack hoạt động

**Giải pháp cho localhost:**
```bash
# Chạy Chrome với flag tắt SameSite enforcement (chỉ cho demo/localhost)
# macOS:
open -a "Google Chrome" --args --disable-features=SameSiteByDefaultCookies

# Hoặc dùng Firefox (không enforce SameSite mặc định như Chrome)
```

**Trong code — cookie vulnerable labs:**
```java
ResponseCookie.from("DEMO_SESSION", sessionToken)
    .httpOnly(true)
    .sameSite("None")   // ← cho phép cross-site (demo attack)
    .secure(false)      // ← localhost HTTP
    .path("/")
    .build()
```

**Trong code — cookie defended (sau khi bật defense):**
```java
ResponseCookie.from("access_token", jwtToken)
    .httpOnly(true)
    .sameSite("Strict")  // ← block cross-site hoàn toàn
    .secure(false)
    .path("/")
    .build()
```

### 2.2 Map từng CSRF lab → Implementation

#### Lab 1 · CSRF vulnerability with no defenses ← IMPLEMENT (Attack chính)
**Attack:** Form HTML từ attacker site submit POST, không có CSRF token, không SameSite protection.
**Demo:** Burp → HTTP History → right-click `/security-lab/csrf/lab1/update-email` → Generate CSRF PoC → copy sang `localhost:4000`.

```java
// Lab1: Không có bất kỳ protection nào
@PostMapping("/security-lab/csrf/lab1/update-email")
public ResponseEntity<?> lab1Vulnerable(
        @CookieValue("DEMO_SESSION") String session,
        @RequestParam String email) {
    // chỉ check session cookie, không có gì khác
    String userId = sessionStore.get(session);
    customerService.updateEmail(userId, email);
    return ResponseEntity.ok("Email updated to: " + email);
}
```

```html
<!-- attacker/lab1.html (served at localhost:4000) -->
<html><body onload="document.forms[0].submit()">
  <form action="http://localhost:8080/api/theater-mgnt/security-lab/csrf/lab1/update-email"
        method="POST">
    <input type="hidden" name="email" value="hacked@evil.com"/>
  </form>
</body></html>
```

#### Lab 2 · CSRF where token validation depends on request method (PRACTITIONER)
**Attack:** Server chỉ validate CSRF token khi POST, không validate khi GET. Đổi method thành GET.
**Demo Burp:** Intercept POST request → Change Request Method (POST→GET) → Forward → thành công.

```java
// Lab2: CSRF token check chỉ cho POST, không check GET
@RequestMapping(value = "/security-lab/csrf/lab2/update-email",
                method = {RequestMethod.POST, RequestMethod.GET})
public ResponseEntity<?> lab2(@CookieValue("DEMO_SESSION") String session,
        @RequestParam String email,
        @RequestParam(required = false) String csrfToken,
        HttpServletRequest request) {
    if (request.getMethod().equals("POST")) {
        // validate csrfToken... nhưng GET không check
        if (csrfToken == null || !csrfStore.isValid(session, csrfToken))
            return ResponseEntity.status(403).body("Invalid CSRF token");
    }
    customerService.updateEmail(sessionStore.get(session), email);
    return ResponseEntity.ok("Updated");
}
```

#### Lab 3 · CSRF where token validation depends on token being present (PRACTITIONER)
**Attack:** Xóa tham số `csrfToken` khỏi request → server bỏ qua validate.
**Demo Burp:** Intercept → xóa dòng `csrfToken=xxx` trong body → Forward → thành công.

```java
// Lab3: Nếu không có token thì skip validation
@PostMapping("/security-lab/csrf/lab3/update-email")
public ResponseEntity<?> lab3(@CookieValue("DEMO_SESSION") String session,
        @RequestParam String email,
        @RequestParam(required = false) String csrfToken) {
    if (csrfToken != null && !csrfStore.isValid(session, csrfToken))
        return ResponseEntity.status(403).body("Invalid CSRF token");
    // ← BUG: nếu csrfToken == null thì không check gì
    customerService.updateEmail(sessionStore.get(session), email);
    return ResponseEntity.ok("Updated");
}
```

#### Lab 11 · CSRF where Referer validation depends on header being present (PRACTITIONER)
**Attack:** Xóa Referer header → server bỏ qua validate.
**Demo Burp:** Intercept → xóa dòng `Referer: ...` → Forward → thành công.

```java
// Lab11: Chỉ validate Referer khi nó có mặt
@PostMapping("/security-lab/csrf/lab11/update-email")
public ResponseEntity<?> lab11(@CookieValue("DEMO_SESSION") String session,
        @RequestParam String email,
        @RequestHeader(value = "Referer", required = false) String referer) {
    if (referer != null && !referer.startsWith("http://localhost:3000")) {
        return ResponseEntity.status(403).body("Invalid Referer");
    }
    // ← BUG: nếu Referer == null thì không check
    customerService.updateEmail(sessionStore.get(session), email);
    return ResponseEntity.ok("Updated");
}
```

#### Defense endpoint (bật lên để so sánh)
```java
// Proper defense: CSRF token bắt buộc, tied to session, SameSite=Strict
@PostMapping("/security-lab/csrf/defended/update-email")
public ResponseEntity<?> defended(
        @CookieValue("DEMO_SESSION") String session,
        @RequestHeader("X-XSRF-TOKEN") String csrfHeader,
        @CookieValue("XSRF-TOKEN") String csrfCookie,
        @RequestParam String email) {
    // Double submit cookie: header phải match cookie (attacker không đọc được cookie)
    if (!csrfHeader.equals(csrfCookie))
        return ResponseEntity.status(403).body("CSRF validation failed");
    customerService.updateEmail(sessionStore.get(session), email);
    return ResponseEntity.ok("Updated");
}
```

---

## Phần 3 — Burp Suite Setup

### 3.1 Cài đặt

```
Burp Suite Community (free) + JWT Editor extension:
  Burp → Extender → BApp Store → JWT Editor → Install

Proxy listener: 127.0.0.1:8888
Browser (Chrome/Firefox): proxy → 127.0.0.1:8888
```

### 3.2 Workflow demo CSRF Labs

```
1. Login tại localhost:3000/security-demo
   Burp Proxy → thấy Set-Cookie: DEMO_SESSION=...; SameSite=None

2. Thực hiện hành động đổi email trên victim app
   Burp HTTP History → tìm POST /security-lab/csrf/lab1/update-email

3. Right-click request → Engagement tools → Generate CSRF PoC
   → Copy HTML → paste vào attacker/lab1.html (localhost:4000)

4. Mở localhost:4000/lab1.html trong browser (vẫn đang login victim app)
   → Form tự submit → email bị đổi → ATTACK THÀNH CÔNG

5. Làm tương tự với /defended endpoint
   → Burp CSRF PoC không có X-XSRF-TOKEN header → 403 FORBIDDEN
```

### 3.3 Workflow demo JWT Labs

```
Cần: Burp JWT Editor extension

1. Login → cookie access_token=eyJ...
   Burp Proxy → thấy cookie trong request

2. Gửi request tới /security-lab/jwt/lab1/whoami về Repeater

3. Trong Repeater, tab JSON Web Token (do JWT Editor tạo):
   - Sửa payload: "sub" → admin account ID
   - Click "Sign" hoặc để trống signature
   - Send → Lab1 vulnerable → 200 OK với admin data

4. Gửi cùng forged token tới endpoint chính /api/.../customers
   → 401 Unauthorized (defense hoạt động)

5. Lab 2 (alg:none): JWT Editor → Header → alg: none → remove signature → Send
   → /lab2/vulnerable → 200 OK
   → /api endpoint → 401 (alg check block)

6. Lab 4 (JWK injection): JWT Editor → Attack → Embedded JWK → Send
   → /lab4/vulnerable → 200 OK (trust JWK header)
   → /api endpoint → 401 (JWK header rejected)
```

---

## Phần 4 — File structure cần tạo

### Backend (package mới)
```
security/
├── lab/
│   ├── CsrfLabController.java        ← tất cả /security-lab/csrf/lab*/...
│   ├── JwtLabController.java         ← tất cả /security-lab/jwt/lab*/...
│   ├── DemoSessionService.java       ← in-memory Map<sessionToken, customerId>
│   └── CsrfTokenStore.java           ← in-memory Map<sessionToken, csrfToken>
```

### Frontend (trang demo)
```
frontend/client/app/security-demo/
├── page.tsx                          ← landing: login + danh sách labs
├── csrf/
│   └── page.tsx                      ← UI victim: form đổi email các lab
└── jwt/
    └── page.tsx                      ← UI test JWT endpoints
```

### Docker
```
CINEMAMANAGEMENT_DO_AN_AN_TOAN/
├── docker-compose.yml                ← 5 services: postgres, backend, admin, client, attacker
└── attacker/
    ├── Dockerfile                    ← nginx phục vụ static HTML
    └── html/
        ├── index.html                ← menu các lab
        ├── csrf-lab1.html            ← auto-submit form lab 1
        ├── csrf-lab2.html            ← GET method override
        ├── csrf-lab3.html            ← missing token
        └── csrf-lab11.html           ← no referer
```

### docker-compose.yml (root)
```yaml
version: "3.9"
services:
  postgres:
    image: postgres:17.5
    environment:
      POSTGRES_DB: theaterdb
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: 123456
    ports: ["5432:5432"]
    volumes: [pgdata:/var/lib/postgresql/data]
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 5s
      timeout: 5s
      retries: 5

  backend:
    build: ./backend/theatermgnt
    ports: ["8080:8080"]
    depends_on:
      postgres: { condition: service_healthy }
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/theaterdb
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: 123456
      JWT_SIGNER_KEY: "change-this-to-32-random-bytes-in-production-env"
      APP_CORS_ALLOWED_ORIGINS: "http://localhost:3000,http://localhost:5173,http://localhost:4000"

  frontend-client:
    build: ./frontend/client
    ports: ["3000:3000"]
    environment:
      NEXT_PUBLIC_API_URL: http://localhost:8080

  frontend-admin:
    build: ./frontend/admin
    ports: ["5173:80"]
    environment:
      VITE_API_BASE_URL: http://localhost:8080

  attacker:
    build: ./attacker
    ports: ["4000:80"]

volumes:
  pgdata:
```

---

## Phần 5 — Implementation Order (thứ tự code)

```
Phase 1 — Core JWT cookie migration
  1. SecurityConfig.java          → bearerTokenResolver đọc từ cookie
  2. AuthenticationController.java → set cookie thay vì trả token trong body
  3. CustomJwtDecoder.java         → thêm jwk/jku/kid rejection
  4. AuthenticationService.java    → thêm alg check trong verifyToken()
  5. application.yml               → jwt.signerKey từ ${JWT_SIGNER_KEY}
  6. Frontend client               → bỏ localStorage, thêm X-XSRF-TOKEN header
  7. Frontend admin                → tương tự

Phase 2 — Security lab endpoints
  8. DemoSessionService.java       → in-memory session store
  9. CsrfLabController.java        → lab1, lab2, lab3, lab11, defended
  10. JwtLabController.java        → lab1 (unverified), lab2 (alg:none), lab4 (jwk)

Phase 3 — Attacker site + Frontend demo page
  11. attacker/html/*.html         → CSRF PoC pages
  12. frontend/client/app/security-demo/page.tsx

Phase 4 — Docker
  13. frontend/admin/Dockerfile + nginx.conf
  14. frontend/client/Dockerfile
  15. attacker/Dockerfile
  16. docker-compose.yml (root)
```

---

## Phần 6 — Rubric mapping

| Tiêu chí | Điểm | Implementation |
|---|---|---|
| Giới thiệu vấn đề | 1đ | Slide: CSRF + JWT, tại sao nguy hiểm trong TMĐT |
| Lý thuyết tấn công CSRF | 0.75đ | 4 labs: no defense, method bypass, token absent, referer |
| Demo CSRF attack | 0.75đ | Burp Generate CSRF PoC → localhost:4000 → email bị đổi |
| Lý thuyết phòng thủ CSRF | 0.75đ | Double submit cookie, SameSite=Strict, CSRF token tied to session |
| Demo CSRF defense | 0.75đ | /defended endpoint → 403 Forbidden |
| Lý thuyết tấn công JWT | 0.75đ | 6 labs: unverified, alg:none, weak key, jwk, jku, kid |
| Demo JWT attack | 0.75đ | Burp JWT Editor → forge token → /lab*/vulnerable → 200 OK |
| Lý thuyết phòng thủ JWT | 0.75đ | 8 defenses map từng lab |
| Demo JWT defense | 0.75đ | Forged token → /api endpoint → 401 + log message |
| Database (1đ) | 1đ | 25+ tables với quan hệ |
| Các trang web (1đ) | 1đ | Client + Admin + Security Demo |
| 1 attack + 1 defense (1đ) | 1đ | CSRF lab1 attack + JWT proper defense |
| Docker | 1đ | `docker compose up --build` → tất cả chạy |
