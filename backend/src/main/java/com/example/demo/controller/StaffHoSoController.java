package com.example.demo.controller;

import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.HoSoResponse;
import com.example.demo.entity.Application;
import com.example.demo.entity.ApplicationHistory;
import com.example.demo.entity.Citizen;
import com.example.demo.entity.Procedure;
import com.example.demo.entity.Staff;
import com.example.demo.entity.Appointment;
import com.example.demo.repository.ApplicationHistoryRepository;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.AppointmentRepository;
import com.example.demo.repository.CitizenRepository;
import com.example.demo.repository.ProcedureRepository;
import com.example.demo.repository.StaffRepository;
import com.example.demo.repository.ZaloAccountRepository;
import com.example.demo.entity.ZaloAccount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.text.Normalizer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Staff HoSo Management Controller
 * API cho nhân viên quản lý hồ sơ
 */
@RestController
@RequestMapping("/api/staff/hoso")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('Staff') or hasRole('Admin')")
@Transactional
public class StaffHoSoController {

    private final ApplicationRepository applicationRepository;
    private final ApplicationHistoryRepository applicationHistoryRepository;
    private final AppointmentRepository appointmentRepository;
    private final StaffRepository staffRepository;
    private final CitizenRepository citizenRepository;
    private final ProcedureRepository procedureRepository;
    private final ZaloAccountRepository zaloAccountRepository;

    // Sử dụng Application.PHASE_* thay vì constants riêng

    /**
     * Lấy dashboard thống kê hồ sơ
     * GET /api/staff/hoso/dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<HoSoResponse.DashboardData>> getDashboard() {
        List<Application> allApps = applicationRepository.findAll();

        int choXuLy = 0, dangXuLy = 0, hoanThanh = 0, treHan = 0;
        LocalDate today = LocalDate.now();

        for (Application app : allApps) {
            if (app.getCurrentPhase() == Application.PHASE_QUEUE ||
                    app.getCurrentPhase() == Application.PHASE_PENDING) {
                choXuLy++;
                if (app.getDeadline() != null && app.getDeadline().isBefore(today)) {
                    treHan++;
                }
            } else if (app.getCurrentPhase() == Application.PHASE_PROCESSING ||
                    app.getCurrentPhase() == Application.PHASE_RECEIVED) {
                dangXuLy++;
                if (app.getDeadline() != null && app.getDeadline().isBefore(today)) {
                    treHan++;
                }
            } else if (app.getCurrentPhase() == Application.PHASE_COMPLETED) {
                hoanThanh++;
            }
        }

        HoSoResponse.DashboardData dashboard = HoSoResponse.DashboardData.builder()
                .tongSoHoSo(allApps.size())
                .choXuLy(choXuLy)
                .dangXuLy(dangXuLy)
                .hoanThanh(hoanThanh)
                .treHan(treHan)
                .build();

        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }

    /**
     * Lấy danh sách hồ sơ (có thể filter theo trạng thái)
     * GET /api/staff/hoso?trangThai=1
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<HoSoResponse>>> getList(
            @RequestParam(required = false) Integer trangThai) {
        List<Application> apps;

        if (trangThai != null) {
            // Map trangThai từ frontend sang phase
            int phase = mapTrangThaiToPhase(trangThai);
            apps = applicationRepository.findAll().stream()
                    .filter(a -> a.getCurrentPhase() == phase)
                    .collect(Collectors.toList());
        } else {
            apps = applicationRepository.findAll();
        }

        List<HoSoResponse> responses = apps.stream()
                .map(this::mapToHoSoResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Lấy chi tiết hồ sơ
     * GET /api/staff/hoso/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HoSoResponse>> getById(@PathVariable Integer id) {
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ"));

        return ResponseEntity.ok(ApiResponse.success(mapToHoSoResponse(app)));
    }

    /**
     * Lấy danh sách hồ sơ theo Zalo ID
     * GET /api/staff/hoso/by-zalo/{zaloId}
     */
    @GetMapping("/by-zalo/{zaloId}")
    public ResponseEntity<ApiResponse<List<HoSoResponse>>> getByZaloId(@PathVariable String zaloId) {
        // Find Zalo account
        ZaloAccount zaloAccount = zaloAccountRepository.findByZaloId(zaloId)
                .orElse(null);

        if (zaloAccount == null) {
            return ResponseEntity.ok(ApiResponse.success(List.of(), "Không tìm thấy tài khoản Zalo"));
        }

        // Find all applications linked to this Zalo account
        List<Application> apps = applicationRepository.findByZaloAccountId(zaloAccount.getId());

        List<HoSoResponse> responses = apps.stream()
                .map(this::mapToHoSoResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Tạo hồ sơ mới
     * POST /api/staff/hoso
     */
    @PostMapping
    public ResponseEntity<ApiResponse<HoSoResponse>> create(
            @RequestBody Map<String, Object> body,
            Authentication authentication) {

        Staff staff = getCurrentStaff(authentication);

        String cccd = (String) body.get("cccd");
        String hoTen = (String) body.get("hoTen");
        String soDienThoai = (String) body.get("soDienThoai");
        String email = (String) body.get("email");
        Integer loaiThuTucId = ((Number) body.get("loaiThuTucId")).intValue();
        Integer doUuTien = body.get("doUuTien") != null
                ? ((Number) body.get("doUuTien")).intValue()
                : 0;
        Boolean confirmDuplicate = body.get("confirmDuplicate") != null
                ? (Boolean) body.get("confirmDuplicate")
                : false;

        // Check Citizen Conflict
        // Check Citizen Conflict
        Citizen citizen = citizenRepository.findById(cccd).orElse(null);

        if (citizen != null) {
            String existName = normalizeString(citizen.getFullName());
            String newName = normalizeString(hoTen);

            if (!existName.equals(newName)) {
                // Conflict
                if (!confirmDuplicate) {
                    return ResponseEntity.status(409).body(ApiResponse.error("CITIZEN_CONFLICT",
                            "CCCD " + cccd + " đang thuộc về công dân: " + citizen.getFullName()
                                    + ". Bạn có muốn CẬP NHẬT tên mới (" + hoTen + ") cho công dân này không?"));
                } else {
                    // Logic OVERWRITE Info
                    citizen.setFullName(hoTen);
                    if (soDienThoai != null && !soDienThoai.isEmpty())
                        citizen.setPhone(soDienThoai);
                    if (email != null && !email.isEmpty())
                        citizen.setEmail(email);
                    citizen = citizenRepository.save(citizen);
                    log.info("Overwrote citizen {} info. Old Name -> {}", cccd, hoTen);
                }
            } else {
                // Update info if same person
                if (soDienThoai != null && !soDienThoai.isEmpty())
                    citizen.setPhone(soDienThoai);
                if (email != null && !email.isEmpty())
                    citizen.setEmail(email);
                citizen = citizenRepository.save(citizen);
            }
        } else {
            // New
            citizen = Citizen.builder()
                    .citizenId(cccd)
                    .fullName(hoTen)
                    .phone(soDienThoai)
                    .email(email)
                    .build();
            citizen = citizenRepository.save(citizen);
        }

        // Get procedure
        Procedure procedure = procedureRepository.findById(loaiThuTucId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thủ tục"));

        // Create application code
        String appCode = "HS" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Create application
        Application app = Application.builder()
                .applicationCode(appCode)
                .citizen(citizen)
                .procedure(procedure)
                .currentPhase(Application.PHASE_PENDING)
                .priority(doUuTien)
                .deadline(LocalDate.now().plusDays(procedure.getProcessingDays()))
                .build();

        app = applicationRepository.save(app);

        // Save history
        saveHistory(app, staff, "TẠO HỒ SƠ",
                null, Application.PHASE_PENDING,
                "Tạo hồ sơ mới");

        log.info("Created hoso: {} by {}", app.getApplicationCode(), staff.getStaffCode());

        return ResponseEntity.ok(ApiResponse.success(mapToHoSoResponse(app), "Tạo hồ sơ thành công"));
    }

    /**
     * Tạo hồ sơ từ lịch hẹn
     * POST /api/staff/hoso/from-lichhen/{lichHenId}
     */
    @PostMapping("/from-lichhen/{lichHenId}")
    public ResponseEntity<ApiResponse<HoSoResponse>> createFromLichHen(
            @PathVariable Integer lichHenId,
            Authentication authentication) {

        Staff staff = getCurrentStaff(authentication);
        Application app = applicationRepository.findById(lichHenId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch hẹn"));

        // Chuyển sang phase PENDING (hồ sơ chờ xử lý)
        app.setCurrentPhase(Application.PHASE_PENDING);
        app.setDeadline(LocalDate.now().plusDays(app.getProcedure().getProcessingDays()));
        app = applicationRepository.save(app);

        saveHistory(app, staff, "TẠO HỒ SƠ TỪ LỊCH HẸN",
                Application.PHASE_COMPLETED, Application.PHASE_PENDING,
                "Tạo hồ sơ từ lịch hẹn " + app.getQueueDisplay());

        log.info("Created hoso from lichhen: {} by {}", app.getApplicationCode(), staff.getStaffCode());

        return ResponseEntity.ok(ApiResponse.success(mapToHoSoResponse(app), "Tạo hồ sơ từ lịch hẹn thành công"));
    }

    /**
     * Cập nhật trạng thái hồ sơ
     * PUT /api/staff/hoso/{id}/status
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<HoSoResponse>> updateStatus(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> body,
            Authentication authentication) {

        Staff staff = getCurrentStaff(authentication);
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ"));

        Integer trangThaiMoi = ((Number) body.get("trangThaiMoi")).intValue();
        String noiDung = (String) body.get("noiDung");
        String ngayHenStr = (String) body.get("ngayHen");
        String gioHenStr = (String) body.get("gioHen");

        int oldPhase = app.getCurrentPhase();
        int newPhase = mapTrangThaiToPhase(trangThaiMoi);
        LocalDate appointmentDate = null;
        LocalTime expectedTime = null;

        if (newPhase == Application.PHASE_SUPPLEMENT) {
            if (ngayHenStr == null || gioHenStr == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("MISSING_DATE_TIME", "Vui lòng chọn ngày và giờ hẹn bổ sung"));
            }
            appointmentDate = LocalDate.parse(ngayHenStr);
            expectedTime = LocalTime.parse(gioHenStr);

            // Create Appointment
            Appointment appointment = Appointment.builder()
                    .application(app)
                    .staff(staff)
                    .appointmentDate(appointmentDate)
                    .appointmentTime(expectedTime)
                    .status(Appointment.STATUS_SCHEDULED)
                    .build();
            appointmentRepository.save(appointment);
        }

        app.setCurrentPhase(newPhase);
        app = applicationRepository.save(app);

        // Update active appointments to Completed/Cancelled if phase changes to
        // final/processing
        if (newPhase == Application.PHASE_COMPLETED || newPhase == Application.PHASE_CANCELLED
                || newPhase == Application.PHASE_PROCESSING) {
            List<Appointment> apps = appointmentRepository.findActiveByApplicationId(app.getId());
            for (Appointment a : apps) {
                a.setStatus(newPhase == Application.PHASE_CANCELLED ? Appointment.STATUS_CANCELLED
                        : Appointment.STATUS_COMPLETED);
                appointmentRepository.save(a);
            }
        }

        saveHistoryExtended(app, staff, getActionByPhase(newPhase),
                oldPhase, newPhase,
                noiDung != null ? noiDung : HoSoResponse.getTrangThaiText(newPhase),
                appointmentDate, expectedTime);

        log.info("Updated hoso status: {} to {} by {}",
                app.getApplicationCode(), newPhase, staff.getStaffCode());

        return ResponseEntity.ok(ApiResponse.success(mapToHoSoResponse(app), "Cập nhật trạng thái thành công"));
    }

    /**
     * Cập nhật thông tin hồ sơ
     * PUT /api/staff/hoso/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<HoSoResponse>> update(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> body,
            Authentication authentication) {

        Staff staff = getCurrentStaff(authentication);
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ"));

        // Update citizen info if provided
        Citizen citizen = app.getCitizen();
        if (body.get("hoTen") != null) {
            citizen.setFullName((String) body.get("hoTen"));
        }
        if (body.get("soDienThoai") != null) {
            citizen.setPhone((String) body.get("soDienThoai"));
        }
        // diaChi: Citizen entity hiện chưa có field này, có thể thêm sau
        citizenRepository.save(citizen);

        // Update priority if provided
        if (body.get("doUuTien") != null) {
            app.setPriority(((Number) body.get("doUuTien")).intValue());
        }

        app = applicationRepository.save(app);

        // Save history for info update
        saveHistory(app, staff, "CẬP NHẬT THÔNG TIN",
                app.getCurrentPhase(), app.getCurrentPhase(),
                "Cập nhật thông tin hồ sơ");

        log.info("Updated hoso: {} by {}", app.getApplicationCode(), staff.getStaffCode());

        return ResponseEntity.ok(ApiResponse.success(mapToHoSoResponse(app), "Cập nhật hồ sơ thành công"));
    }

    // ==================== HELPER METHODS ====================

    private Staff getCurrentStaff(Authentication authentication) {
        String staffCode = authentication.getName();
        return staffRepository.findByStaffCode(staffCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));
    }

    private int mapTrangThaiToPhase(Integer trangThai) {
        return switch (trangThai) {
            case 0 -> Application.PHASE_CANCELLED;
            case 1 -> Application.PHASE_QUEUE; // Chờ gọi số
            case 2 -> Application.PHASE_PENDING; // Chờ xử lý
            case 3 -> Application.PHASE_PROCESSING;
            case 4 -> Application.PHASE_COMPLETED;
            case 5 -> Application.PHASE_RECEIVED;
            case 6 -> Application.PHASE_SUPPLEMENT; // Yêu cầu bổ sung
            default -> Application.PHASE_PENDING;
        };
    }

    private String getActionByPhase(int phase) {
        if (phase == Application.PHASE_SUPPLEMENT)
            return "YÊU CẦU BỔ SUNG";
        if (phase == Application.PHASE_COMPLETED)
            return "HOÀN THÀNH";
        if (phase == Application.PHASE_PROCESSING)
            return "CHUYỂN GỌI SỐ";
        return "CẬP NHẬT TRẠNG THÁI";
    }

    private void saveHistoryExtended(Application app, Staff staff, String action,
            Integer oldPhase, int newPhase, String content,
            LocalDate date, LocalTime time) {
        ApplicationHistory history = ApplicationHistory.builder()
                .application(app)
                .counter(staff.getCounter())
                .staff(staff)
                .action(action)
                .phaseFrom(oldPhase)
                .phaseTo(newPhase)
                .content(content)
                .appointmentDate(date)
                .expectedTime(time)
                .createdAt(LocalDateTime.now())
                .build();
        applicationHistoryRepository.save(history);
    }

    private void saveHistory(Application app, Staff staff, String action,
            Integer oldPhase, int newPhase, String content) {
        saveHistoryExtended(app, staff, action, oldPhase, newPhase, content, null, null);
    }

    private HoSoResponse mapToHoSoResponse(Application app) {
        List<ApplicationHistory> histories = applicationHistoryRepository.findByApplicationId(app.getId());

        List<HoSoResponse.HistoryDto> historyDtos = histories.stream().map(h -> HoSoResponse.HistoryDto.builder()
                .nguoiXuLy(h.getStaff() != null ? h.getStaff().getFullName() : "Hệ thống")
                .hanhDong(h.getAction())
                .trangThaiCu(HoSoResponse.getTrangThaiText(h.getPhaseFrom()))
                .trangThaiMoi(HoSoResponse.getTrangThaiText(h.getPhaseTo()))
                .noiDung(h.getContent())
                .thoiGian(h.getCreatedAt())
                .build()).collect(Collectors.toList());

        return HoSoResponse.builder()
                .id(app.getId())
                .maHoSo(app.getApplicationCode())
                .cccd(app.getCitizen().getCitizenId())
                .hoTenCongDan(app.getCitizen().getFullName())
                .soDienThoai(app.getCitizen().getPhone())
                .email(app.getCitizen().getEmail())
                .tenThuTuc(app.getProcedure().getProcedureName())
                .maThuTuc(app.getProcedure().getProcedureCode())
                .trangThai(app.getCurrentPhase())
                .trangThaiText(HoSoResponse.getTrangThaiText(app.getCurrentPhase()))
                .doUuTien(app.getPriority())
                .ngayNop(app.getCreatedAt())
                .hanXuLy(app.getDeadline())
                .nguonGoc(app.getZaloAccount() != null ? "Zalo" : "Trực tiếp")
                .maLichHen(app.getQueueDisplay())
                .loaiThuTucId(app.getProcedure().getId())
                .thoiGianXuLyQuyDinh(app.getProcedure().getProcessingDays())
                .lichSuXuLy(historyDtos)
                .zaloId(app.getZaloAccount() != null ? app.getZaloAccount().getZaloId() : null)
                .zaloName(app.getZaloAccount() != null ? app.getZaloAccount().getZaloName() : null)
                .build();
    }

    private String normalizeString(String s) {
        if (s == null)
            return "";
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").toLowerCase().trim();
    }
}
