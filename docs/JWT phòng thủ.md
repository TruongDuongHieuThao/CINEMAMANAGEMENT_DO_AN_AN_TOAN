# JWT httpOnly Session — Phòng thủ & Kịch bản Demo

> **Đồ án:** An toàn bảo mật trong thương mại điện tử — UIT  
> **Kỹ thuật:** JWT Session httpOnly + Algorithm Pinning + Header Injection Defense  
> **API Base:** `http://localhost:8080/api/theater-mgnt`

---

## 1. Kiến trúc bảo mật JWT toàn hệ thống

Mọi request đến endpoint được bảo vệ đều đi qua chuỗi xác thực sau:

```
Request (cookie: access_token)
    │
    ▼
BearerTokenResolver          ← đọc JWT từ cookie (skip /security-lab/**)
    │
    ▼
CustomJwtDecoder.decode()
    ├─ [1] SignedJWT.parse()  → nếu lỗi → JwtException("Malformed token")
    ├─ [2] Reject jwk header  → nếu có  → JwtException("JWT header injection detected")
    ├─ [3] Reject jku header  → nếu có  → JwtException("JWT header injection detected")
    ├─ [4] Reject kid header  → nếu có  → JwtException("Unexpected kid header")
    └─ [5] introspect(token)
               │
               ▼
           verifyToken()
               ├─ [6] alg == HS512?  → nếu không → AppException(UNAUTHENTICATED)
               ├─ [7] signature ok?  → nếu không → AppException(UNAUTHENTICATED)
               ├─ [8] expired?       → nếu hết hạn → AppException(UNAUTHENTICATED)
               └─ [9] blacklisted?   → nếu logout  → AppException(UNAUTHENTICATED)
    │
    ▼
NimbusJwtDecoder (pinned HS512) ← decode claims
    │
    ▼
Spring Security AuthenticationFilter
    │
    ▼
Controller → Response 200
```

---

## 2. Bảng defenses toàn hệ thống

| # | Defense | File | Tấn công bị chặn |
|---|---|---|---|
| 1 | httpOnly cookie | `AuthenticationController` | XSS không đọc được token |
| 2 | Reject `jwk` header | `CustomJwtDecoder` | Lab 4: JWK header injection |
| 3 | Reject `jku` header | `CustomJwtDecoder` | JKU header injection |
| 4 | Reject `kid` header | `CustomJwtDecoder` | kid SQL/path injection |
| 5 | Algorithm pin HS512 | `AuthenticationService.verifyToken()` | Lab 2: alg:none |
| 6 | Signature verify | `AuthenticationService.verifyToken()` | Lab 1: forged payload |
| 7 | Expiry check | `AuthenticationService.verifyToken()` | Token replay sau expire |
| 8 | Blacklist (logout) | `invalidated_tokens` table | Token reuse sau logout |
| 9 | SameSite cookie | `AuthenticationController` | CSRF cross-site |

---

## 3. Endpoint thực tế được bảo vệ

| Endpoint | Method | Bảo vệ bởi | Ghi chú |
|---|---|---|---|
| `/customers/myInfo` | GET | JWT (any role) | Profile của user hiện tại |
| `/customers/{id}` | GET | JWT (any role) | Profile by ID |
| `/customers` | GET | JWT (ADMIN) | Danh sách toàn bộ customer |
| `/bookings` | POST | JWT (CUSTOMER) | Tạo booking |
| `/bookings/{id}/summary` | GET | JWT (any role) | Chi tiết booking |
| `/staffs/**` | ANY | JWT (ADMIN) | Quản lý nhân viên |
| `/roles/**` | ANY | JWT (ADMIN) | Quản lý phân quyền |
| `/rooms/**` | ANY | JWT (ADMIN) | Quản lý phòng chiếu |

**Public endpoints** (không cần JWT):
```
POST /auth/customer/login, /auth/admin/login, /auth/logout
GET  /movies/**, /genres/**, /screenings/**, /cinemas/**
```

---

## 4. Chuẩn bị Burp Suite

```
1. Burp Suite Community → Proxy → Proxy settings
   → Add listener: 127.0.0.1:8888

2. Firefox → Settings → Network → Manual Proxy
   → HTTP: 127.0.0.1  Port: 8888

3. Extensions → BApp Store → cài "JWT Editor"
   (sau khi cài, tab "JSON Web Token" xuất hiện trong Request Inspector)

4. Khởi động hệ thống:
   Backend  → mvn spring-boot:run  (port 8080)
   Frontend → npm run dev          (port 3000)
```

---

## 5. Kịch bản demo — Baseline (Token hợp lệ)

**Mục tiêu:** Xác nhận token hợp lệ hoạt động bình thường trên endpoint thực tế.

### Bước 1 — Đăng nhập

Bật Burp Intercept → vào `localhost:3000` → đăng nhập bằng tài khoản customer.

Hoặc dùng Burp Repeater:
```
POST /api/theater-mgnt/auth/customer/login HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "loginIdentifier": "23521600@gm.uit.edu.vn",
  "password": "12345678"
}
```

**Response 200:**
```
Set-Cookie: access_token=eyJhbGciOiJIUzUxMiJ9....; HttpOnly; SameSite=None; Secure
Set-Cookie: XSRF-TOKEN=...; SameSite=Lax
```

### Bước 2 — Gọi endpoint thực tế với token hợp lệ

```
GET /api/theater-mgnt/customers/myInfo HTTP/1.1
Host: localhost:8080
Cookie: access_token=eyJhbGciOiJIUzUxMiJ9....
```

**Response 200:**
```json
{
  "code": 1000,
  "result": {
    "id": "...",
    "fullName": "Nguyễn Văn A",
    "email": "23521600@gm.uit.edu.vn",
    "phone": "0901234567"
  }
}
```

> Token hợp lệ → hệ thống phản hồi bình thường. Ghi nhớ giá trị `access_token` để dùng ở các bước sau.

---

## 6. Kịch bản demo — Tấn công 1: Forged Signature (Lab 1)

**Mục tiêu:** Chứng minh attacker không thể forge payload JWT để mạo danh user/leo quyền trên endpoint thực.

### Nguyên lý tấn công

```
Token gốc:   eyJ{"alg":"HS512"}.eyJ{"sub":"abc123","scope":"ROLE_CUSTOMER"}.VALID_SIG
                                                                                   ↑
Forged:      eyJ{"alg":"HS512"}.eyJ{"sub":"administrator","scope":"ROLE_ADMIN"}.OLD_SIG
                                                                                   ↑
                                        payload thay đổi → chữ ký cũ không còn hợp lệ
```

### Bước 1 — Tạo forged token

```
POST /api/theater-mgnt/security-lab/jwt/forge/lab1 HTTP/1.1
Host: localhost:8080
Cookie: access_token=<token hợp lệ từ login>
Content-Length: 0
```

Lấy giá trị `forged_token_lab1` từ response (sub=administrator, scope=ROLE_ADMIN, chữ ký cũ).

### Bước 2 — Tấn công endpoint thực: GET /customers/myInfo

```
GET /api/theater-mgnt/customers/myInfo HTTP/1.1
Host: localhost:8080
Cookie: access_token=<forged_token_lab1>
```

**Response 401 — Tấn công THẤT BẠI:**
```json
{
  "code": 1006,
  "message": "Unauthenticated"
}
```

### Bước 3 — Tấn công endpoint thực: GET /customers (yêu cầu ADMIN)

```
GET /api/theater-mgnt/customers HTTP/1.1
Host: localhost:8080
Cookie: access_token=<forged_token_lab1>
```

**Response 401 — Tấn công THẤT BẠI:**
```json
{
  "code": 1006,
  "message": "Unauthenticated"
}
```

> Dù token chứa `scope=ROLE_ADMIN`, chữ ký sai → bị chặn từ tầng `CustomJwtDecoder` trước khi vào controller.

### Vì sao bị chặn?

```java
// CustomJwtDecoder.java
authenticationService.introspect(token)  // gọi verifyToken()
   → signedJWT.verify(new MACVerifier(serverKey))
   → false (payload đã thay đổi → signature không khớp)
   → AppException(UNAUTHENTICATED)
   → introspect trả về isValid=false
   → throw new JwtException("Invalid token")
   → Spring Security → 401
```

---

## 7. Kịch bản demo — Tấn công 2: alg:none (Lab 2)

**Mục tiêu:** Chứng minh attacker không thể bypass signature bằng cách đặt `alg:none` trên endpoint thực.

### Nguyên lý tấn công

```
Legitimate: eyJ{"alg":"HS512"}.eyJ{payload}.VALID_SIGNATURE
Forged:     eyJ{"alg":"none"} .eyJ{payload}.          ← phần 3 trống, không cần ký
```

### Bước 1 — Tạo alg:none token (dùng forge tool)

```
POST /api/theater-mgnt/security-lab/jwt/forge/lab1 HTTP/1.1
Host: localhost:8080
Cookie: access_token=<token hợp lệ>
Content-Length: 0
```

Lấy giá trị `forged_token_lab2_none` từ response.

**Hoặc tự tạo bằng JWT Editor trong Burp:**
1. Lấy request có token hợp lệ → tab **JSON Web Token**
2. Trong **Header**: đổi `"alg"` thành `"none"`
3. Trong **Payload**: đổi `"sub"` thành `"administrator"`, `"scope"` thành `"ROLE_ADMIN"`
4. Click **Sign** → chọn **"Don't sign"** → phần signature tự xóa
5. Token mới có format: `header.payload.` (dấu chấm cuối, không có signature)

### Bước 2 — Tấn công endpoint thực: GET /customers/myInfo

```
GET /api/theater-mgnt/customers/myInfo HTTP/1.1
Host: localhost:8080
Cookie: access_token=<forged_token_lab2_none>
```

**Response 401 — Tấn công THẤT BẠI:**
```json
{
  "code": 1006,
  "message": "Unauthenticated"
}
```

### Bước 3 — Tấn công endpoint thực: POST /bookings

```
POST /api/theater-mgnt/bookings HTTP/1.1
Host: localhost:8080
Cookie: access_token=<forged_token_lab2_none>
Content-Type: application/json

{"screeningId": "...", "seatIds": [...]}
```

**Response 401 — Tấn công THẤT BẠI:**
```json
{
  "code": 1006,
  "message": "Unauthenticated"
}
```

### Vì sao bị chặn?

```java
// AuthenticationService.verifyToken()
JWSAlgorithm alg = signedJWT.getHeader().getAlgorithm();
if (!JWSAlgorithm.HS512.equals(alg)) {
    // alg:none → JWSAlgorithm = JWSAlgorithm.parse("none")
    // → không bằng HS512 → reject ngay
    throw new AppException(ErrorCode.UNAUTHENTICATED);
}
```

> Lưu ý: `SignedJWT.parse()` với token alg:none thường ném `ParseException` (PlainJWT không phải SignedJWT) → bị bắt tại `CustomJwtDecoder` → `JwtException("Malformed token")` → 401.

---

## 8. Kịch bản demo — Tấn công 3: JWK Header Injection (Lab 4)

**Mục tiêu:** Chứng minh attacker không thể nhúng RSA public key vào JWT header để bypass xác thực trên endpoint thực.

### Nguyên lý tấn công

```
Attacker:
  1. Tạo RSA-2048 key pair (private key A + public key A)
  2. Tạo JWT:
     Header:  {"alg":"RS256", "jwk": {"kty":"RSA","n":"...","e":"AQAB"}}
                                        ↑ public key A nhúng vào header
     Payload: {"sub":"administrator","scope":"ROLE_ADMIN"}
  3. Ký bằng private key A
  → Server vulnerable: dùng public key trong header để verify → thành công!
  → Server defended:   từ chối ngay khi thấy "jwk" header
```

### Bước 1 — Tạo forged JWK injection token

**Cách A — Dùng forge endpoint:**
```
POST /api/theater-mgnt/security-lab/jwt/forge/lab4 HTTP/1.1
Host: localhost:8080
Content-Length: 0
```
→ Copy `forged_token` từ response.

**Cách B — Dùng JWT Editor trong Burp (đúng như PortSwigger lab):**
1. Vào tab **JWT Editor** (menu trên cùng Burp)
2. Click **New RSA Key** → 2048 bits → **OK**
3. Mở request có token hợp lệ → tab **JSON Web Token**
4. **Payload**: đổi `sub` → `"administrator"`, `scope` → `"ROLE_ADMIN"`
5. **Header**: đổi `alg` → `"RS256"`
6. Cuối trang → click **"Embed JWK"** → chọn key vừa tạo
7. Click **Sign** → chọn RSA key → **OK**

### Bước 2 — Tấn công endpoint thực: GET /customers/myInfo

```
GET /api/theater-mgnt/customers/myInfo HTTP/1.1
Host: localhost:8080
Cookie: access_token=<jwk_injected_token>
```

**Response 401 — Tấn công THẤT BẠI:**
```json
{
  "code": 1006,
  "message": "Unauthenticated"
}
```

### Bước 3 — Tấn công endpoint admin: GET /customers

```
GET /api/theater-mgnt/customers HTTP/1.1
Host: localhost:8080
Cookie: access_token=<jwk_injected_token>
```

**Response 401 — Tấn công THẤT BẠI:**
```json
{
  "code": 1006,
  "message": "Unauthenticated"
}
```

### Vì sao bị chặn?

```java
// CustomJwtDecoder.java — kiểm tra TRƯỚC khi làm bất cứ điều gì
SignedJWT signedJWT = SignedJWT.parse(token);
var header = signedJWT.getHeader();

if (header.getJWK() != null || header.getJWKURL() != null) {
    throw new JwtException("JWT header injection detected");
    // → Spring Security nhận JwtException → 401
}
if (header.getKeyID() != null) {
    throw new JwtException("Unexpected kid header");
}
```

> Defense được thực thi ở **tầng đầu tiên** (CustomJwtDecoder), trước cả introspect và signature verify.

---

## 9. Bảng tổng hợp kết quả demo

| Tấn công | Token | Endpoint thực | HTTP Status | Bị chặn tại |
|---|---|---|---|---|
| Token hợp lệ | HS512, sig đúng | `GET /customers/myInfo` | **200 OK** | — |
| Forged signature | sub=administrator | `GET /customers/myInfo` | **401** | `verifyToken(): sig verify` |
| Forged signature | scope=ROLE_ADMIN | `GET /customers` | **401** | `verifyToken(): sig verify` |
| alg:none | không có signature | `GET /customers/myInfo` | **401** | `verifyToken(): alg pin` hoặc `parse()` |
| alg:none | không có signature | `POST /bookings` | **401** | `verifyToken(): alg pin` hoặc `parse()` |
| JWK injection | RSA public key nhúng | `GET /customers/myInfo` | **401** | `CustomJwtDecoder: header check` |
| JWK injection | RSA public key nhúng | `GET /customers` | **401** | `CustomJwtDecoder: header check` |
| Token hết hạn | exp quá khứ | bất kỳ | **401** | `verifyToken(): expiry check` |
| Token đã logout | jti trong blacklist | bất kỳ | **401** | `verifyToken(): blacklist check` |

---

## 10. Lớp bảo vệ trong code (tóm tắt)

### CustomJwtDecoder.java — Tầng 1
```java
// Reject header injection trước mọi thứ
SignedJWT signedJWT = SignedJWT.parse(token);         // [1] format check
var header = signedJWT.getHeader();
if (header.getJWK() != null || header.getJWKURL() != null)
    throw new JwtException("JWT header injection detected");   // [2] jwk/jku
if (header.getKeyID() != null)
    throw new JwtException("Unexpected kid header");           // [3] kid

// Introspect (gọi verifyToken)
authenticationService.introspect(token);                       // [4]

// Decode với NimbusJwtDecoder được pin HS512
nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKeySpec)
    .macAlgorithm(MacAlgorithm.HS512)                         // [5] pin alg
    .build();
```

### AuthenticationService.verifyToken() — Tầng 2
```java
// [6] Algorithm pinning
JWSAlgorithm alg = signedJWT.getHeader().getAlgorithm();
if (!JWSAlgorithm.HS512.equals(alg))
    throw new AppException(ErrorCode.UNAUTHENTICATED);

// [7] Signature verification với server-side key
JWSVerifier verifier = new MACVerifier(signerKeyBytes());
if (!signedJWT.verify(verifier))
    throw new AppException(ErrorCode.UNAUTHENTICATED);

// [8] Expiry check
if (!expiryTime.after(new Date()))
    throw new AppException(ErrorCode.UNAUTHENTICATED);

// [9] Blacklist check (logout)
if (invalidatedTokenRepository.existsById(jti))
    throw new AppException(ErrorCode.UNAUTHENTICATED);
```

---

## 11. Flow demo đề xuất (20 phút)

```
[5 phút]  Chuẩn bị
  └─ Khởi động backend + frontend
  └─ Cấu hình Burp proxy + JWT Editor extension

[3 phút]  Baseline — token hợp lệ
  └─ POST /auth/customer/login                     → 200, nhận cookie
  └─ GET  /customers/myInfo (valid token)          → 200, thấy profile

[4 phút]  Tấn công 1 — Forged Signature
  └─ POST /security-lab/jwt/forge/lab1             → tạo forged token
  └─ GET  /customers/myInfo (forged token)         → 401 ⛔
  └─ GET  /customers       (forged ROLE_ADMIN)     → 401 ⛔

[4 phút]  Tấn công 2 — alg:none
  └─ Dùng forged_token_lab2_none (từ forge/lab1)
  └─ GET  /customers/myInfo (alg:none token)       → 401 ⛔
  └─ POST /bookings         (alg:none token)       → 401 ⛔

[4 phút]  Tấn công 3 — JWK Header Injection
  └─ POST /security-lab/jwt/forge/lab4             → tạo RSA-injected token
  └─ GET  /customers/myInfo (jwk injected)         → 401 ⛔
  └─ GET  /customers        (jwk injected)         → 401 ⛔

[2 phút]  Giải thích defense
  └─ Chỉ vào CustomJwtDecoder + verifyToken trong code
  └─ Hệ thống phòng thủ 9 tầng
```
