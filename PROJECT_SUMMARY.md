# Tong Quan Du An Cinema Management

## 1) Muc tieu va kien truc tong quan
Du an gom 3 phan chinh:
- `backend/theatermgnt`: Spring Boot (Java) cung cap REST API, auth JWT, nghiep vu dat ve, quan ly rap/phong/phim/lich/cham cong/doanh thu.
- `frontend/client`: Next.js (App Router) cho khach hang dat ve.
- `frontend/admin`: React + Vite cho nhan vien/quan tri van hanh he thong.

Kien truc giao tiep:
- `client` va `admin` goi API ve `http://localhost:8080/api/theater-mgnt` (co the doi qua env).
- Backend su dung PostgreSQL va JPA/Hibernate.

Thong ke nhanh (theo ma nguon hien tai):
- Backend Java files: `335`
- Admin source files: `252`
- Client files: `154`
- Backend Controllers: `31`
- Backend Services: `47`
- Backend Repositories: `35`
- Mapping HTTP (Get/Post/Put/Delete): `124`

## 2) Cau truc thu muc root
- `backend/theatermgnt`: API server Spring Boot.
- `frontend/client`: trang customer booking.
- `frontend/admin`: trang quan tri noi bo.
- `.gitignore`: bo qua `node_modules`, file `.env*`, `.DS_Store`.
- `package.json` (root): dependency dung chung UI utility (`clsx`, `tailwind-merge`, `lucide-react`, ...).

## 3) Backend (Spring Boot)
### 3.1 Cong nghe va cau hinh chinh
- Runtime: Spring Boot `3.5.6`.
- Java: `25` (pom set source/target 25, `--enable-preview`).
- DB: PostgreSQL (driver `org.postgresql`).
- ORM: Spring Data JPA + Hibernate (`ddl-auto: update`).
- Security: Spring Security + OAuth2 Resource Server (JWT).
- Build: Maven, MapStruct, Lombok, JaCoCo, Spotless.

File chinh:
- `backend/theatermgnt/pom.xml`
- `backend/theatermgnt/src/main/resources/application.yml`
- `backend/theatermgnt/src/main/java/com/theatermgnt/theatermgnt/configuration/SecurityConfig.java`
- `backend/theatermgnt/src/main/java/com/theatermgnt/theatermgnt/TheatermgntApplication.java`

### 3.2 Domain/module backend
Cac module nghiep vu chinh (folder cap 1 trong package):
- `authentication`, `authorization`, `account`
- `movie`, `review`, `screening`, `screeningSeat`
- `booking`, `ticket`, `payment`, `revenue`
- `cinema`, `room`, `seat`, `seatType`, `priceConfig`
- `combo`, `bookingCombo`
- `staff`, `customer`, `schedule`, `ShiftType`, `equipment`
- `common`, `configuration`, `validator`, `constant`

Mau to chuc code trong moi module:
- `controller` -> nhan HTTP request
- `service` -> xu ly nghiep vu
- `repository` -> truy cap DB
- `entity` -> mo hinh du lieu JPA
- `dto/request` + `dto/response` -> model API
- `mapper` -> map entity <-> DTO

### 3.3 API endpoint theo nhom (base path backend)
Base URL:
- `http://localhost:8080/api/theater-mgnt`

Nhom endpoint chinh:
- Auth/Account:
  - `/auth/admin/login`, `/auth/customer/login`, `/auth/refresh`, `/auth/logout`, `/auth/introspect`
  - `/auth/forgot-password`, `/auth/reset-password`
  - `/register`, `/auth/accounts/create-password`
- Phim/lich/review:
  - `/movies`, `/genres`, `/age_ratings`, `/screenings`, `/reviews`
- Dat ve/thanh toan/hoa don:
  - `/bookings`, `/bookings/{id}/summary`, `/bookings/{id}/combos`, `/bookings/{id}/create-invoice`, `/bookings/{id}/cancel`, `/bookings/{id}/redeem-points`
  - `/tickets` (check-in, my tickets, transfer)
  - `/payment/cash/{invoiceId}`, `/invoices`
- Van hanh rap:
  - `/cinemas`, `/rooms`, `/seatTypes`, `/priceConfigs`
  - `/equipments`, `/equipment-categories`
  - `/roles`, `/permissions`
  - `/cinemas/{cinemaId}/schedules`, `/cinemas/{cinemaId}/shift-types`
  - `/revenue` (report, daily, movie, reprocess)

### 3.4 Security + CORS
- Public endpoint (POST/GET mot so route): auth, register, movies/genres/screenings/reviews/cinemas/payment.
- Cac endpoint con lai yeu cau JWT.
- CORS origin tu `app.cors.allowed-origins` trong `application.yml` (hien co localhost:3000 va localhost:5173).

### 3.5 Resource va template
- `application.yml`
- Email templates:
  - `templates/email/welcome-staff.html`
  - `templates/email/welcome-customer.html`
  - `templates/email/reset-password.html`
  - `templates/email/ticket-issue.html`
  - `templates/email/refund-notification.html`

### 3.6 Docker va local infra
- `docker-compose.yml`: chay PostgreSQL container (`postgres:17.5`, port host `5433`).
- `Dockerfile`: multi-stage build jar bang Maven, runtime Corretto 21.

## 4) Frontend Client (Next.js - customer)
### 4.1 Cong nghe
- Next.js `16.x` + React `19.x` + TypeScript.
- UI: Radix + Tailwind + custom components.
- State: Zustand.
- HTTP: Axios interceptor + auto refresh token.

File entry/cau hinh:
- `frontend/client/package.json`
- `frontend/client/next.config.ts`
- `frontend/client/configurations/configuration.ts`
- `frontend/client/configurations/httpClient.ts`
- `frontend/client/app/layout.tsx`

### 4.2 Page map (App Router)
- `/` -> `app/page.tsx`: trang home (hero, now showing, coming soon, faq...)
- `/movies/[id]` -> chi tiet phim + chon suat chieu + review
- `/booking` -> page bao ve user da login (demo/protected)
- `/booking/[movieId]/[showtimeId]` -> luong booking 5 buoc
- `/booking/success/[bookingId]` -> trang ket qua/thong tin ve
- `/my-tickets` -> danh sach ve da mua, filter, chuyen nhuong ve
- `/profile` -> thong tin ca nhan

### 4.3 Luong booking customer (chinh)
Trong `app/booking/[movieId]/[showtimeId]/page.tsx`:
1. Chon ghe (kiem tra orphan seat + seat availability).
2. Tao booking (`createBooking`) tren backend.
3. Chon combo (`updateBookingCombos`).
4. Xac nhan + redeem loyalty points (`redeemBookingPoints`).
5. Thanh toan (`PaymentStep`) -> thanh cong -> `SuccessStep`.

Chi tiet xu ly:
- Luu state booking tam trong `sessionStorage` de phuc hoi khi reload.
- Co `BookingTimer` xu ly timeout/expired booking.
- Khi quay lai step 1 se cancel booking tam de giai phong ghe.

### 4.4 Auth guard/phien dang nhap
- `RouteGuard`: chan route protected; neu chua login -> redirect home + mo login modal.
- `StoreInitializer`: check token, introspect, thu refresh neu token het han.
- `httpClient`: tu dong gan bearer token cho private APIs, retry 1 lan neu 401 bang refresh token.

## 5) Frontend Admin (React + Vite)
### 5.1 Cong nghe
- React `19.x`, Vite `7.x`, TypeScript.
- Router: `react-router-dom`.
- State: Zustand stores (`useAuthStore`, `useNotificationStore`, ...).
- HTTP: Axios interceptor (xu ly 401/403/500 toan cuc).

File entry/cau hinh:
- `frontend/admin/package.json`
- `frontend/admin/vite.config.ts`
- `frontend/admin/src/main.tsx`
- `frontend/admin/src/App.tsx`
- `frontend/admin/src/config/app.config.ts`
- `frontend/admin/src/configurations/configuration.ts`
- `frontend/admin/src/configurations/httpClient.ts`

### 5.2 Luong route + phan quyen
- Router trung tam: `frontend/admin/src/routes/index.tsx`.
- `PrivateRoute`: yeu cau da dang nhap.
- `ProtectedRoute`: check permission theo role (`PERMISSIONS`).
- `LayoutDefault`: khung chung app (sidebar/header/content).

### 5.3 Page/chuc nang admin chinh
Danh sach nhom page (thu muc `src/pages`):
- Dashboard: thong ke tong quan.
- Movies, Showtimes, Cinemas, Rooms.
- Tickets, TicketBooking (ban ve tai quay), Bookings, Invoices.
- Customers, Staff, WorkSchedules, ShiftTypes.
- Equipment, Combos.
- Roles, Permissions.
- Reviews, Reports.
- Settings (profile, seat-prices, buffer).
- Login, ForgotPassword, Forbidden, NotFound.

### 5.4 Service layer admin
`src/services` tach API theo domain:
- `authenticationService`, `movieService`, `showtimeService`, `cinemaService`, ...
- `staffService`, `customerService`, `bookingService`, `invoiceService`, `revenueService`, ...
- `socketService`, `chatService` (real-time/chat support).

## 6) Luong code tong the (end-to-end)
### 6.1 Luong customer
1. Customer vao home -> xem phim dang chieu/sap chieu.
2. Vao movie detail -> loc cinema/date/showtime.
3. Chuyen qua booking -> chon ghe + combo + xac nhan + thanh toan.
4. Backend tao booking/ticket/invoice, client hien trang success.
5. Customer vao `my-tickets` de xem QR/check status/chuyen nhuong.

### 6.2 Luong admin
1. Staff login qua `/auth/admin/login`.
2. Frontend admin lay token + permission.
3. Route duoc mo theo role/permission.
4. Admin quan ly phim/lich/chong ghe/trang thai rap/hoa don/nhan su/doanh thu.
5. Bao cao doanh thu thong qua module revenue.

### 6.3 Luong backend
Controller -> Service -> Repository -> DB
- Request vao controller.
- Validate + xu ly nghiep vu trong service.
- Truy van/ghi DB qua repository.
- Tra ve `ApiResponse` + DTO.

## 7) Cau hinh moi truong va deploy
### 7.1 Bien moi truong/API URL
- Client:
  - `NEXT_PUBLIC_API_URL` (fallback: `http://localhost:8080/api/theater-mgnt`)
- Admin:
  - `VITE_API_URL` (fallback: `http://localhost:8080/api/theater-mgnt`)
  - `VITE_BASE_PATH` (de deploy duoi subpath)

### 7.2 Vercel config
- `frontend/client/vercel.json`: build Next.js, co rewrite API local dev.
- `frontend/admin/vercel.json`: SPA rewrite ve `index.html`.

## 8) Lenh run co ban
Backend:
- Trong `backend/theatermgnt`:
  - `./mvnw spring-boot:run` (Windows: `mvnw.cmd spring-boot:run`)

Client:
- Trong `frontend/client`:
  - `npm install`
  - `npm run dev` (default port 3000)

Admin:
- Trong `frontend/admin`:
  - `npm install`
  - `npm run dev` (default port Vite, thuong 5173)

DB local (tuy chon):
- Trong `backend/theatermgnt`:
  - `docker compose up -d`

## 9) Luu y quan trong
- `application.yml` hien dang chua thong tin ket noi DB va `jwt.signerKey` dang hard-code. Nen dua vao bien moi truong de an toan hon.
- `ddl-auto: update` thuan tien dev, nhung production nen can nhac Flyway/Liquibase.
- Du an co quy mo lon; de theo doi de hon co the tiep tuc tach them tai lieu theo tung domain (`booking`, `movie`, `staff`, ...).

---
Tai lieu nay la ban tom tat toan canh de theo doi nhanh. Neu ban muon, minh co the tao them 1 ban `SYSTEM_FLOW.md` dang so do (sequence style) cho luong booking + admin auth de team onboarding nhanh hon.

--- 
cd attacker/html

python -m http.server 4000