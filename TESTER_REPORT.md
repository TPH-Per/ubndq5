# Báo cáo xác nhận fix — App đặt thời gian đặt quầy TTHCC Chợ Lớn

**Thời điểm lập báo cáo:** 2026-05-22  
**Cập nhật:** 2026-05-22 (bổ sung xác thực Zalo accessToken)  
**Phạm vi:** Zalo Mini App + AdminStaff + Backend  

---

## 1. Zalo Mini App — Luồng truy cập & UI/UX tổng quan

| Hạng mục | Trạng thái | Mô tả fix/logic | Mã nguồn |
| --- | --- | --- | --- |
| QR Code vào Homepage | ✅ Đúng | Root route redirect về `/citizen`; base path dùng `/zapps/{APP_ID}` nên scan QR sẽ vào đúng trang chủ Mini App | `client/src/App.tsx`, `client/src/lib/zma.ts` |
| Nút "Hướng dẫn" không chuyển trang | ✅ Đã fix | Card "Hướng dẫn" gọi `navigate('guide')`; route `/citizen/guide` đã khai báo; trang Guide đầy đủ nội dung (liên hệ, thủ tục, FAQ) | `client/src/pages/citizen/Home.tsx:254-261`, `client/src/App.tsx:31`, `client/src/pages/citizen/Guide.tsx` |
| 3 card chức năng còn lại | ✅ Đúng | Các card "Đặt lịch hẹn / Lịch hẹn / Hồ sơ" đã gắn đúng route | `client/src/pages/citizen/Home.tsx` |
| Logic Chủ Nhật → tự nhảy sang Thứ 2 | ✅ Đúng logic | Danh sách ngày chỉ lấy **thứ 2–6**; nếu hôm nay là Chủ nhật, ngày đầu tiên sẽ tự động là Thứ 2 kế tiếp | `client/src/pages/citizen/BookingFlow.tsx` (Step 3: lọc weekend) |
| Race condition / slot locking | ✅ Đúng logic | Áp dụng PostgreSQL advisory lock theo **(date, time)** để chặn đặt trùng slot giữa các quầy | `backend/.../service/AppointmentBookingService.java` |
| Auto‑expire khung giờ | ✅ Đã verify | Backend đặt `available = 0` nếu **now + 2h > slot**; client chỉ hiển thị slot có `available > 0` → slot quá khứ/giờ gần sẽ bị ẩn. Đồng thời guard ở thời điểm book: nếu `now + 2h > slot` → throw `APPOINTMENT_TOO_SOON_TO_BOOK` | `backend/.../controller/CitizenAppointmentController.java:60-77`, `backend/.../service/CitizenAppointmentService.java:60-62` |

---

## 2. Module Điền Thông Tin (Data Input & Validation)

| Hạng mục | Trạng thái | Mô tả fix/logic | Mã nguồn |
| --- | --- | --- | --- |
| Camera/OCR không hoạt động | ✅ Đã fix | Hiển thị placeholder "Quét CCCD (Sắp ra mắt)" dạng `<div>` không click được, `opacity-60`, kèm divider "HOẶC NHẬP TAY" hướng dẫn nhập thủ công | `client/src/pages/citizen/BookingFlow.tsx:651-653` |
| Tuổi hợp lệ | ✅ Đúng | Chặn nếu `< 15 tuổi` | `client/src/pages/citizen/BookingFlow.tsx` |
| CCCD 12 chữ số | ✅ Đúng | Regex `^\d{12}$` + input chỉ cho số, max 12 ký tự | `client/src/pages/citizen/BookingFlow.tsx` |
| SĐT bắt buộc | ✅ Đúng | Không có số điện thoại thì không cho submit | `client/src/pages/citizen/BookingFlow.tsx` |

---

## 3. Trạng thái sau nộp (Post‑Submit)

| Hạng mục | Trạng thái | Mô tả | Mã nguồn |
| --- | --- | --- | --- |
| Theo dõi trạng thái hồ sơ | ✅ Đúng | Có trang Lịch hẹn/Tracking hiển thị tiến trình (PENDING → QUEUE → PROCESSING → COMPLETED) | `client/src/pages/citizen/MyAppointments.tsx`, `client/src/pages/citizen/QueueTracking.tsx` |
| Hiển thị lịch hẹn đúng | ✅ Đúng | API trả dữ liệu lịch hẹn + UI render theo slot đã đặt | `client/src/services/citizenApi.ts`, `client/src/pages/citizen/MyAppointments.tsx` |

---

## 4. AdminStaff (TTHCC Chợ Lớn)

| Hạng mục | Trạng thái | Mô tả | Mã nguồn |
| --- | --- | --- | --- |
| Chỉ 1 tài khoản Admin tối cao | ✅ Đúng theo thiết kế | Admin mặc định được bootstrap khi khởi tạo DB | `backend/.../service/StaffService.java`, `backend/.../controller/StaffController.java` |
| UI/UX Admin & module | ✅ Ổn định | Sidebar + module đã có sẵn, hoạt động như thiết kế | `AdminStaff/src/views/**` |
| Data checking & staff management | ✅ Ổn định | Danh sách booking/nhân sự hiển thị đúng | `AdminStaff/src/views/**` |
| Dashboard/Analytics | ✅ Ổn định | Có thống kê realtime | `AdminStaff/src/views/admin/**` |
| Bug Data Mapping "Công dân Zalo" | ✅ Đã fix | **Cách fix:** `citizenNameOverride` được truyền từ form nhập liệu → `SimulationContext.bookAppointment()` → backend `CitizenAppointmentService` dùng priority chain: `tên nhập tay > tên Zalo profile > "Công dân Zalo"`. Admin thấy **Full Name** từ form, không còn mặc định "Công dân Zalo" | `client/src/pages/citizen/BookingFlow.tsx:257`, `client/src/context/SimulationContext.tsx:192`, `backend/.../service/CitizenAppointmentService.java:89-91` |

---

## 5. Bảo mật & Xác thực

### 5.1 Xác thực Zalo accessToken (MỚI — 2026-05-22)

**Vấn đề:** Trước đó, backend chấp nhận bất kỳ `zaloId` nào client gửi lên mà không verify. Ai cũng có thể gọi API trực tiếp với `zaloId` giả.

**Cách fix:**
- **Client:** `loadZaloProfile()` gọi `sdk.getAccessToken({})` từ Zalo Mini App SDK để lấy access_token. Token được lưu trong `SimulationContext` và gửi kèm mọi API request.
- **Backend:** `ZaloAccountService.verifyAccessToken()` gọi `https://graph.zalo.me/v2.0/me?fields=id` với access_token để lấy `zaloId` đã được verify. **KHÔNG dùng** `zaloId` client gửi lên.
- **Xóa backdoor:** `window.__TEST_ZALO_PROFILE__` đã bị xóa khỏi production code (chỉ còn trong test fixtures cho Playwright E2E).

**10/10 citizen endpoints đều được bảo vệ:**

| Endpoint | Verify token | Mã nguồn |
| --- | --- | --- |
| POST /appointments (đặt lịch) | ✅ | `CitizenAppointmentService.java:43` |
| POST /appointments/search | ✅ | `CitizenAppointmentService.java:158` |
| POST /appointments/{id}/cancel | ✅ | `CitizenAppointmentService.java:206` |
| POST /appointments/{id}/view | ✅ | `CitizenAppointmentService.java:265` |
| POST /applications/search | ✅ | `CitizenApplicationController.java:49` |
| POST /applications/{id}/view | ✅ | `CitizenApplicationController.java:105` |
| POST /applications/{id}/history | ✅ | `CitizenApplicationController.java:156` |
| POST /reports (góp ý) | ✅ | `CitizenController.java:180` |
| POST /reports/search | ✅ | `CitizenController.java:241` |
| GET /appointments/available-slots | Không cần (public, no PII) | — |

**Files thay đổi:**

Backend:
- `backend/.../service/ZaloAccountService.java` — thêm `verifyAccessToken()`
- `backend/.../exception/ErrorCode.java` — thêm `ZALO_TOKEN_INVALID` (AUTH_008)
- `backend/.../dto/request/CreateAppointmentRequest.java` — thêm field `accessToken`
- `backend/.../service/CitizenAppointmentService.java` — verify token trong book/cancel/view/search
- `backend/.../controller/CitizenAppointmentController.java` — truyền accessToken
- `backend/.../controller/CitizenApplicationController.java` — verify token trong search/view/history
- `backend/.../controller/CitizenController.java` — verify token trong reports

Client:
- `client/src/lib/zalo.ts` — thêm `getAccessToken({})` từ Zalo SDK
- `client/src/context/SimulationContext.tsx` — lưu accessToken, gửi với mọi API call
- `client/src/services/citizenApi.ts` — tất cả API functions có `accessToken` param
- `client/src/pages/citizen/QueueTracking.tsx` — gửi accessToken
- `client/src/pages/citizen/MyDocuments.tsx` — gửi accessToken
- `client/src/pages/citizen/Feedback.tsx` — gửi accessToken

### 5.2 Rate Limiting

| Cơ chế | Chi tiết | Mã nguồn |
| --- | --- | --- |
| Per-endpoint filter | Đặt lịch: 5 req/phút/IP, Hủy: 5, Feedback: 5, GET: 60 | `backend/.../filter/CitizenRateLimitFilter.java` |
| Global interceptor | 30 req/phút/IP cho tất cả `/api/citizen/**` | `backend/.../config/RateLimitInterceptor.java` |
| Kích hoạt | `RATE_LIMIT_ENABLED=true` (default trong docker-compose) | `docker-compose.prod.yml:40` |

### 5.3 eKYC / OTP

| Hạng mục | Trạng thái | Ghi chú |
| --- | --- | --- |
| eKYC / OTP | ⚠️ Chưa triển khai | **Không cần cho soft launch** vì Zalo Mini App đã xác thực người dùng qua Zalo account. Cần khi mở rộng ra ngoài Zalo ecosystem. |

---

## 6. Logic biến đếm (STT / Sequence)

| Hạng mục | Trạng thái | Mô tả | Mã nguồn |
| --- | --- | --- | --- |
| Reset STT theo ngày | ✅ Đúng | `@Scheduled(cron = "0 0 0 * * *")` chạy lúc 00:00, gọi `ALTER SEQUENCE queue_number_seq RESTART WITH 1` | `backend/.../service/ApplicationSchedulerService.java:84-89`, `backend/.../repository/ApplicationRepository.java:70-72` |
| Tạo STT atomic | ✅ Đúng | Dùng `SELECT nextval('queue_number_seq')` — thread-safe, không trùng | `backend/.../repository/ApplicationRepository.java:66-67` |
| Auto-cancel no-show | ✅ Có | Scheduler chạy mỗi 5 phút, tự hủy hồ sơ quá hạn 24 phút | `backend/.../service/ApplicationSchedulerService.java:39` |

---

## 7. Testing Environment

| Hạng mục | Trạng thái | Ghi chú | Mã nguồn |
| --- | --- | --- | --- |
| Clear dữ liệu mẫu | ✅ Có migration | V31 xóa dữ liệu mẫu (application_history, appointment, GopYPhanAnh, application, sample staff), reset sequence. **Chỉ chạy trên DB test/fresh** | `backend/src/main/resources/db/migration/V31__cleanup_sample_data.sql` |
| E2E data integrity | ✅ Hỗ trợ | Playwright test fixtures inject test profile qua `page.addInitScript()` | `tests/fixtures/base.ts`, `tests/specs/**` |

---

## 8. DevOps & Triển khai — CẦN LÀM

### 8.1 Domain & SSL

| Việc | Chi tiết | Ưu tiên |
| --- | --- | --- |
| Domain nhà nước | Đã xin được domain. Cần cấu hình DNS trỏ về server deploy | **Critical** |
| SSL Certificate | Domain nhà nước cần cert thật (Let's Encrypt hoặc internal CA). Hiện tại dùng self-signed cert trong `nginx/ssl/` | **Critical** |
| Cập nhật `.env` | Đổi `API_DOMAIN`, `CORS_ALLOWED_ORIGINS`, `VITE_API_BASE_URL` theo domain thật | **Critical** |

**Cấu hình domain (khi có domain thật):**

```bash
# .env
API_DOMAIN=https://datlich.tthcc.cholon.gov.vn
CORS_ALLOWED_ORIGINS=https://datlich.tthcc.cholon.gov.vn,https://h5.zdn.vn,https://mini.zalo.me

# client/.env.production
VITE_API_BASE_URL=https://datlich.tthcc.cholon.gov.vn/api/citizen
```

### 8.2 Zalo Mini App Platform

| Việc | Chi tiết | Ưu tiên |
| --- | --- | --- |
| API Domain | Vào Mini App Platform → API Domain → thêm `https://datlich.tthcc.cholon.gov.vn` (status: ACTIVE) | **Critical** |
| ZMP_TOKEN | Token hiện tại hết hạn 12/2025. Cần gia hạn khi build client mới | **Critical** |
| Deploy client | `cd client && npm run build` → upload `www/` lên Zalo Mini App Platform | **Critical** |

### 8.3 Hạ tầng mạng LAN

| Việc | Chi tiết | Ưu tiên |
| --- | --- | --- |
| DevOps network | Cần DevOps chuyên network cấu hình kết nối an toàn: Zalo Cloud/External Server ↔ LAN TTHCC | **High** |
| NAT/VPN/Firewall | Cấu hình routing, NAT, VPN, firewall nội bộ cho phép traffic từ Zalo về server | **High** |
| Nginx proxy | Hiện tại nginx config đã sẵn (proxy `/api/citizen/` → backend, serve AdminStaff). Cần cập nhật SSL cert thật | **Medium** |

### 8.4 Build & Deploy

```bash
# 1. Build client cho Zalo Mini App
cd client && npm run build
# Upload www/ lên Zalo Mini App Platform

# 2. Build & chạy backend + DB + nginx
docker compose -f docker-compose.prod.yml up -d --build

# 3. Verify
curl https://datlich.tthcc.cholon.gov.vn/api/citizen/specialties
curl https://datlich.tthcc.cholon.gov.vn/api/citizen/appointments/available-slots?date=2026-05-23
```

### 8.5 Client Dockerfile (đã fix)

Client Dockerfile trước đó copy từ `/app/dist` nhưng Vite output ra `www/`. Đã align: Dockerfile giờ copy từ `/app/www`.

---

## Kết luận

| Nhóm | Trạng thái |
| --- | --- |
| UI/UX (QR, Hướng dẫn, Cards, Routing) | ✅ Hoàn tất |
| Đặt lịch (Slots, Race condition, Auto-expire, Chủ nhật) | ✅ Hoàn tất |
| Điền thông tin (Camera placeholder, CCCD, SĐT, Tuổi) | ✅ Hoàn tất |
| Data Mapping (Tên công dân → Admin) | ✅ Hoàn tất |
| STT/Sequence (Reset hàng ngày, Atomic) | ✅ Hoàn tất |
| Bảo mật (Zalo accessToken verify, Rate limiting) | ✅ Hoàn tất |
| Xóa test backdoor | ✅ Hoàn tất |
| Clear dữ liệu mẫu (Migration V31) | ✅ Hoàn tất |
| eKYC/OTP | ⚠️ Chưa cần cho soft launch |
| DevOps (Domain, SSL, LAN, Zalo Platform config) | ⚠️ Cần triển khai |

**Phần code đã ổn định và sẵn sàng deploy.** Phần còn lại là DevOps: cấu hình domain, SSL, mạng LAN nội bộ, và các thủ tục trên Zalo Mini App Platform.
