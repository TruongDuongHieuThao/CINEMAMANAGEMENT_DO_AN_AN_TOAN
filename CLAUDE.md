# Cinema Management – CLAUDE.md

Project cho đồ án môn **An toàn bảo mật trong thương mại điện tử** (UIT).  
Mục tiêu: implement CSRF attack/defense và JWT attack/defense vào hệ thống rạp phim.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 3.x · Java 21 · Spring Security (OAuth2 Resource Server) |
| JWT lib | Nimbus JOSE (`com.nimbusds:nimbus-jose-jwt`) |
| Database | PostgreSQL (Supabase remote / local Docker) |
| Frontend Admin | React 19 + Vite + TypeScript + Zustand + Axios + ShadCN UI |
| Frontend Client | Next.js 16 (App Router) + TypeScript + Zustand + Axios + ShadCN UI |
| Container | Docker + Docker Compose |

---

## Directory Structure

```
CINEMAMANAGEMENT_DO_AN_AN_TOAN/
├── CLAUDE.md                          ← this file
├── docs/
│   └── SECURITY_PLAN.md               ← detailed implementation plan
├── backend/
│   └── theatermgnt/
│       ├── Dockerfile
│       ├── docker-compose.yml          ← currently only postgres service
│       ├── pom.xml
│       └── src/main/
│           ├── java/com/theatermgnt/theatermgnt/
│           │   ├── configuration/
│           │   │   ├── SecurityConfig.java        ← CSRF disabled here
│           │   │   ├── CustomJwtDecoder.java      ← JWT decode + introspect
│           │   │   └── JwtAuthenticationEntryPoint.java
│           │   ├── authentication/
│           │   │   ├── controller/AuthenticationController.java
│           │   │   ├── service/AuthenticationService.java  ← verifyToken()
│           │   │   └── service/TokenService.java           ← generateToken()
│           │   ├── account/ · booking/ · cinema/ · movie/
│           │   ├── customer/ · staff/ · screening/ · seat/
│           │   ├── payment/ · review/ · combo/ · room/
│           │   └── security/           ← (NEW – tạo khi implement)
│           └── resources/
│               └── application.yml    ← jwt.signerKey, jwt.valid-duration
└── frontend/
    ├── admin/   (React Vite – port 5173)
    └── client/  (Next.js  – port 3000)
```

---

## Database Tables (PostgreSQL)

| Table | Key Columns | Relations |
|---|---|---|
| `accounts` | id, email, username, password, account_type, is_active | ← base auth |
| `staffs` | id, account_id, cinema_id, full_name, ... | account_id → accounts |
| `customers` | id, account_id, full_name, phone, ... | account_id → accounts |
| `roles` | id, name, description | M:N staffs |
| `permissions` | id, name, description | M:N roles |
| `cinemas` | id, name, address, ... | |
| `rooms` | id, cinema_id, name, ... | cinema_id → cinemas |
| `seat_types` | id, name, ... | |
| `seats` | id, room_id, seat_type_id, row, col | room_id → rooms |
| `movies` | id, title, duration, age_rating_id, ... | |
| `genres` | id, name | M:N movies |
| `age_ratings` | id, name, description | |
| `screenings` | id, movie_id, room_id, start_time, ... | movie_id → movies, room_id → rooms |
| `screening_seats` | id, screening_id, seat_id, status, price | screening_id → screenings, seat_id → seats |
| `bookings` | id, customer_id, screening_id, status, total_amount, ... | customer_id → customers |
| `tickets` | id, booking_id, screening_seat_id, ... | booking_id → bookings |
| `combos` | id, name, price, ... | |
| `combo_items` | id, combo_id, name, quantity | combo_id → combos |
| `booking_combos` | id, booking_id, combo_id, quantity | |
| `invoices` | id, booking_id, status, ... | booking_id → bookings |
| `payments` | id, invoice_id, method, amount, ... | invoice_id → invoices |
| `movie_reviews` | id, customer_id, movie_id, rating, content | |
| `review_votes` | id, review_id, customer_id, vote_type | review_id → movie_reviews |
| `invalidated_tokens` | id (jti), expiry_time | token denylist (logout) |
| `otp_tokens` | id, account_id, code, expiry_time | password reset OTP |
| `price_configs` | id, seat_type_id, base_price, ... | |
| `work_schedules` | id, staff_id, shift_type_id, date | |
| `shift_types` | id, name, start_time, end_time | |
| `equipments` | id, category_id, room_id, name | |
| `equipment_categories` | id, name | |

---

## Authentication Flow (Current)

```
Client → POST /api/theater-mgnt/auth/customer/login  (username+password)
       ← { token: "eyJ..." }   (JWT, HS512, 1h expiry)

Client → [subsequent requests]
       Authorization: Bearer eyJ...
       
Backend: CustomJwtDecoder.decode()
           → AuthenticationService.introspect()  (check invalidated_tokens)
           → NimbusJwtDecoder (HS512 pinned)
```

**Token storage**: localStorage (frontend) — không dùng cookie cho JWT chính.  
**Algorithm**: HS512, key hardcoded trong `application.yml` (`jwt.signerKey`).  
**Blacklist**: Logout lưu jti vào bảng `invalidated_tokens`.

---

## Current Security State (Before migration)

### CSRF
```java
httpSecurity.csrf(AbstractHttpConfigurer::disable);  // ← CSRF hoàn toàn bị tắt
```
JWT gửi qua Authorization header (localStorage) → không vulnerable CSRF tự nhiên.
**Plan:** Migrate JWT sang httpOnly cookie → tạo CSRF attack surface → add CSRF lab endpoints.

### JWT (sau khi migrate sang cookie)
- **Cần thêm**: explicit `alg == HS512` check trong `verifyToken()` trước khi verify
- **Cần thêm**: reject `jwk`, `jku`, `kid` headers trong `CustomJwtDecoder`
- **Cần sửa**: `jwt.signerKey` hardcode → `${JWT_SIGNER_KEY}` từ env var
- **Đã có**: pin HS512 trong NimbusJwtDecoder ✅, token blacklist ✅, expiry check ✅

### Security Lab endpoints (NEW)
```
/security-lab/csrf/lab1/...   ← CSRF no defenses (attack demo)
/security-lab/csrf/lab2/...   ← token validation depends on method
/security-lab/csrf/lab3/...   ← token depends on being present
/security-lab/csrf/lab11/...  ← referer depends on header present
/security-lab/csrf/defended/  ← double submit cookie defense
/security-lab/jwt/lab1/...    ← unverified signature
/security-lab/jwt/lab2/...    ← alg:none
/security-lab/jwt/lab4/...    ← JWK injection
```

### Attacker site (NEW — port 4000)
Static nginx serving CSRF PoC HTML pages. Different origin từ victim app → proper cross-site demo.

---

## Key Files for Security Work

| File | Purpose |
|---|---|
| `backend/theatermgnt/src/main/java/.../configuration/SecurityConfig.java` | Enable/disable CSRF, filter chain |
| `backend/theatermgnt/src/main/java/.../configuration/CustomJwtDecoder.java` | JWT decode, algorithm pinning |
| `backend/theatermgnt/src/main/java/.../authentication/service/TokenService.java` | generateToken() |
| `backend/theatermgnt/src/main/java/.../authentication/service/AuthenticationService.java` | verifyToken(), introspect() |
| `backend/theatermgnt/src/main/resources/application.yml` | jwt.signerKey, cors config |
| `backend/theatermgnt/docker-compose.yml` | Docker services (cần mở rộng) |
| `backend/theatermgnt/Dockerfile` | Backend image (Maven + Java 21) |

---

## API Base URL

```
http://localhost:8080/api/theater-mgnt
```

### Public endpoints (no auth needed)
```
POST /auth/customer/login
POST /auth/admin/login
POST /auth/logout
POST /auth/refresh
POST /register
GET  /movies/**
GET  /genres/**
GET  /screenings/**
GET  /cinemas
```

### Protected endpoints (Bearer JWT required)
```
POST   /bookings
GET    /bookings/{id}
POST   /reviews
PUT    /customers/{id}
POST   /bookings/{id}/cancel
...
```

---

## Frontend Pages

### Admin (React Vite – port 5173)
- Login → Dashboard → Movies / Cinemas / Rooms / Showtimes / Bookings / Combos / Customers / Staff / Roles / Permissions / Revenue / Equipment / Work Schedules

### Client (Next.js – port 3000)
- Home → Movies list → Movie detail → Booking flow (seat select → combo → payment → success) → My Tickets → Profile

---

## Assignment Context

- Course: An toàn bảo mật trong thương mại điện tử – UIT
- Two security techniques chosen: **CSRF** + **JWT**
- Required: 1 attack technique + 1 defense technique implemented in the web app
- Docker packaging required
- See `docs/SECURITY_PLAN.md` for full implementation specification
