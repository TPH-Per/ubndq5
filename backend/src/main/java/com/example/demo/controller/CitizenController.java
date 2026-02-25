package com.example.demo.controller;

import com.example.demo.dto.response.ApiResponse;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.text.Normalizer;

/**
 * Citizen Controller - API cho công dân (Client/Zalo Mini App)
 * Không yêu cầu xác thực Staff, chỉ cần CCCD
 */
@RestController
@RequestMapping("/api/citizen")
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CitizenController {

    private final ApplicationRepository applicationRepository;
    private final ApplicationHistoryRepository applicationHistoryRepository;
    private final AppointmentRepository appointmentRepository;
    private final ProcedureRepository procedureRepository;
    private final ProcedureTypeRepository procedureTypeRepository;
    private final CitizenRepository citizenRepository;
    private final FeedbackRepository feedbackRepository;
    private final ZaloAccountRepository zaloAccountRepository;

    // ==================== THỦ TỤC HÀNH CHÍNH ====================

    /**
     * Lấy danh sách chuyên môn (lĩnh vực)
     * GET /api/citizen/specialties
     */
    @GetMapping("/specialties")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getSpecialties() {
        List<ProcedureType> types = procedureTypeRepository.findAll();

        List<Map<String, Object>> result = types.stream().map(t -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", t.getId());
            map.put("name", t.getName());
            map.put("description", t.getDescription());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Lấy danh sách thủ tục theo chuyên môn
     * GET /api/citizen/procedures?specialtyId={id}
     */
    @GetMapping("/procedures")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getProcedures(
            @RequestParam(required = false) Integer specialtyId) {

        List<Procedure> procedures;
        if (specialtyId != null) {
            procedures = procedureRepository.findByProcedureTypeIdAndIsActive(specialtyId, true);
        } else {
            procedures = procedureRepository.findAllActive();
        }

        List<Map<String, Object>> result = procedures.stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("name", p.getProcedureName());
            map.put("code", p.getProcedureCode());
            map.put("specialtyId", p.getProcedureType() != null ? p.getProcedureType().getId() : null);
            map.put("estimatedDays", p.getProcessingDays());
            map.put("description", p.getDescription());
            map.put("requiredDocuments", p.getRequiredDocuments());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ==================== LỊCH HẸN ====================

    /**
     * Lấy slot khả dụng cho một ngày
     * GET
     * /api/citizen/appointments/available-slots?date={yyyy-MM-dd}&procedureId={id}
     */
    @GetMapping("/appointments/available-slots")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAvailableSlots(
            @RequestParam String date,
            @RequestParam(required = false) Integer procedureId) {

        LocalDate targetDate = LocalDate.parse(date);

        // Lấy các slot đã book
        List<LocalTime> bookedTimes = appointmentRepository.findBookedTimes(targetDate);

        // Tạo danh sách slot
        List<Map<String, Object>> slots = new ArrayList<>();

        // Morning slots: 7:30, 7:54, 8:18, 8:42, 9:06, 9:30, 9:54, 10:18, 10:42, 11:06
        LocalTime morningStart = LocalTime.of(7, 30);
        for (int i = 0; i < 10; i++) {
            LocalTime slotTime = morningStart.plusMinutes(i * 24);
            Map<String, Object> slot = new HashMap<>();
            slot.put("time", slotTime.toString());
            slot.put("available", bookedTimes.contains(slotTime) ? 0 : 5);
            slot.put("maxCapacity", 5);
            slots.add(slot);
        }

        // Afternoon slots: 13:00, 13:24, 13:48, 14:12, 14:36, 15:00, 15:24, 15:48,
        // 16:12, 16:36
        LocalTime afternoonStart = LocalTime.of(13, 0);
        for (int i = 0; i < 10; i++) {
            LocalTime slotTime = afternoonStart.plusMinutes(i * 24);
            Map<String, Object> slot = new HashMap<>();
            slot.put("time", slotTime.toString());
            slot.put("available", bookedTimes.contains(slotTime) ? 0 : 5);
            slot.put("maxCapacity", 5);
            slots.add(slot);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("date", date);
        result.put("slots", slots);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Đặt lịch hẹn mới
     * POST /api/citizen/appointments
     * 
     * Body:
     * - procedureId: số nguyên ID thủ tục
     * - appointmentDate: yyyy-MM-dd
     * - appointmentTime: HH:mm
     * - citizenName: Họ tên công dân
     * - citizenId: CCCD (12 số)
     * - phoneNumber: Số điện thoại
     * - zaloId: (tùy chọn) Zalo User ID
     * - zaloName: (tùy chọn) Tên hiển thị trên Zalo
     */
    @Transactional
    @PostMapping("/appointments")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createAppointment(
            @RequestBody Map<String, Object> body) {

        Integer procedureId = ((Number) body.get("procedureId")).intValue();
        String appointmentDateStr = (String) body.get("appointmentDate");
        String appointmentTimeStr = (String) body.get("appointmentTime");
        String citizenName = (String) body.get("citizenName");
        String citizenId = (String) body.get("citizenId");
        String phoneNumber = (String) body.get("phoneNumber");

        // Zalo account info (optional)
        String zaloId = (String) body.get("zaloId");
        String zaloName = (String) body.get("zaloName");

        // Validate required fields
        if (procedureId == null || appointmentDateStr == null || appointmentTimeStr == null ||
                citizenName == null || citizenId == null || phoneNumber == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("MISSING_FIELDS", "Thiếu thông tin bắt buộc"));
        }

        // Validate CCCD
        if (!citizenId.matches("\\d{12}")) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("INVALID_CCCD", "CCCD phải có đúng 12 số"));
        }

        // Check Citizen conflict: CCCD exists but name is different → REJECT
        Optional<Citizen> existingCitizen = citizenRepository.findByCitizenId(citizenId);
        if (existingCitizen.isPresent()) {
            String existingName = normalizeString(existingCitizen.get().getFullName());
            String newName = normalizeString(citizenName);

            if (!existingName.equals(newName)) {
                // Conflict: same CCCD but different name
                return ResponseEntity.status(409)
                        .body(ApiResponse.error("CITIZEN_CONFLICT",
                                "CCCD " + citizenId + " đã được đăng ký với tên: " + existingCitizen.get().getFullName()
                                        + ". Vui lòng kiểm tra lại thông tin hoặc liên hệ hỗ trợ."));
            }
        }

        Procedure procedure = procedureRepository.findById(procedureId)
                .orElseThrow(() -> new RuntimeException("Thủ tục không tồn tại"));

        LocalDate appointmentDate = LocalDate.parse(appointmentDateStr);
        LocalTime appointmentTime = LocalTime.parse(appointmentTimeStr);

        // Tìm hoặc tạo Citizen
        Citizen citizen = citizenRepository.findByCitizenId(citizenId)
                .orElseGet(() -> {
                    Citizen newCitizen = Citizen.builder()
                            .citizenId(citizenId)
                            .fullName(citizenName)
                            .phone(phoneNumber)
                            .build();
                    return citizenRepository.save(newCitizen);
                });

        // Update citizen info nếu có thay đổi
        citizen.setFullName(citizenName);
        citizen.setPhone(phoneNumber);
        citizenRepository.save(citizen);

        // Handle Zalo Account (optional)
        ZaloAccount zaloAccount = null;
        if (zaloId != null && !zaloId.isEmpty()) {
            zaloAccount = zaloAccountRepository.findByZaloId(zaloId)
                    .orElseGet(() -> {
                        ZaloAccount newZalo = ZaloAccount.builder()
                                .zaloId(zaloId)
                                .zaloName(zaloName)
                                .isActive(true)
                                .build();
                        return zaloAccountRepository.save(newZalo);
                    });

            // Update zalo name if changed
            if (zaloName != null && !zaloName.equals(zaloAccount.getZaloName())) {
                zaloAccount.setZaloName(zaloName);
                zaloAccountRepository.save(zaloAccount);
            }
        }

        // Generate queue number
        int queueNumber = applicationRepository.countByCreatedAtDate(LocalDate.now()) + 1;
        String prefix = procedure.getProcedureCode().substring(0, Math.min(2, procedure.getProcedureCode().length()));

        // Create Application
        Application app = Application.builder()
                .applicationCode(
                        "HS-" + LocalDate.now().toString().replace("-", "") + "-" + String.format("%03d", queueNumber))
                .procedure(procedure)
                .citizen(citizen)
                .zaloAccount(zaloAccount) // Link Zalo account
                .currentPhase(Application.PHASE_PENDING)
                .priority(Application.PRIORITY_NORMAL)
                .queueNumber(queueNumber)
                .queuePrefix(prefix)
                .build();
        app = applicationRepository.save(app);

        // Create Appointment
        Appointment appointment = Appointment.builder()
                .application(app)
                .appointmentDate(appointmentDate)
                .appointmentTime(appointmentTime)
                .status(Appointment.STATUS_SCHEDULED)
                .build();
        appointmentRepository.save(appointment);

        // Create History
        String source = zaloId != null ? "Zalo Mini App" : "App";
        ApplicationHistory history = ApplicationHistory.builder()
                .application(app)
                .action("ĐẶT LỊCH")
                .phaseFrom(null)
                .phaseTo(Application.PHASE_PENDING)
                .content("Đặt lịch hẹn qua " + source)
                .appointmentDate(appointmentDate)
                .expectedTime(appointmentTime)
                .createdAt(LocalDateTime.now())
                .build();
        applicationHistoryRepository.save(history);

        log.info("Citizen {} created appointment for procedure {} via {}",
                citizenId, procedure.getProcedureCode(), source);

        Map<String, Object> result = new HashMap<>();
        result.put("id", app.getId());
        result.put("code", app.getApplicationCode());
        result.put("procedureName", procedure.getProcedureName());
        result.put("appointmentDate", appointmentDate.toString());
        result.put("appointmentTime", appointmentTime.toString());
        result.put("queueDisplay", app.getQueueDisplay());
        result.put("status", "SCHEDULED");
        result.put("zaloLinked", zaloAccount != null);

        return ResponseEntity.ok(ApiResponse.success(result, "Đặt lịch thành công"));
    }

    /**
     * Lấy danh sách lịch hẹn của công dân
     * GET /api/citizen/appointments?cccd={cccd}&status={status}
     */
    @GetMapping("/appointments")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAppointments(
            @RequestParam String cccd,
            @RequestParam(required = false) String status) {

        List<Application> apps = applicationRepository.findByCitizenCitizenId(cccd);

        // Filter by status if provided
        if (status != null) {
            apps = apps.stream().filter(app -> {
                switch (status.toUpperCase()) {
                    case "UPCOMING":
                        return app.getCurrentPhase() == Application.PHASE_PENDING
                                || app.getCurrentPhase() == Application.PHASE_QUEUE;
                    case "COMPLETED":
                        return app.getCurrentPhase() == Application.PHASE_COMPLETED;
                    case "CANCELLED":
                        return app.getCurrentPhase() == Application.PHASE_CANCELLED;
                    default:
                        return true;
                }
            }).collect(Collectors.toList());
        }

        List<Map<String, Object>> result = apps.stream().map(app -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", app.getId());
            map.put("code", app.getApplicationCode());
            map.put("procedureName", app.getProcedure().getProcedureName());
            map.put("status", getStatusName(app.getCurrentPhase()));
            map.put("queueDisplay", app.getQueueDisplay());
            map.put("createdAt", app.getCreatedAt());

            // Lấy thông tin lịch hẹn
            List<ApplicationHistory> histories = applicationHistoryRepository.findLatestAppointmentHistory(app.getId());
            if (!histories.isEmpty()) {
                map.put("appointmentDate", histories.get(0).getAppointmentDate());
                map.put("appointmentTime", histories.get(0).getExpectedTime());
            }

            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Hủy lịch hẹn
     * POST /api/citizen/appointments/{id}/cancel
     */
    @Transactional
    @PostMapping("/appointments/{id}/cancel")
    public ResponseEntity<ApiResponse<Object>> cancelAppointment(
            @PathVariable Integer id,
            @RequestParam String cccd) {

        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch hẹn"));

        // Verify ownership
        if (!app.getCitizen().getCitizenId().equals(cccd)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("UNAUTHORIZED", "Bạn không có quyền hủy lịch hẹn này"));
        }

        if (app.getCurrentPhase() != Application.PHASE_PENDING && app.getCurrentPhase() != Application.PHASE_QUEUE) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("INVALID_STATUS", "Không thể hủy lịch hẹn ở trạng thái này"));
        }

        int oldPhase = app.getCurrentPhase();
        app.setCurrentPhase(Application.PHASE_CANCELLED);
        app.setCancelReason("Công dân tự hủy");
        app.setCancelType(Application.CANCEL_SELF);
        applicationRepository.save(app);

        // Update appointment
        List<Appointment> appointments = appointmentRepository.findActiveByApplicationId(app.getId());
        for (Appointment a : appointments) {
            a.setStatus(Appointment.STATUS_CANCELLED);
            appointmentRepository.save(a);
        }

        // Log history
        ApplicationHistory history = ApplicationHistory.builder()
                .application(app)
                .action("HỦY LỊCH")
                .phaseFrom(oldPhase)
                .phaseTo(Application.PHASE_CANCELLED)
                .content("Công dân tự hủy lịch hẹn")
                .createdAt(LocalDateTime.now())
                .build();
        applicationHistoryRepository.save(history);

        return ResponseEntity.ok(ApiResponse.success(null, "Đã hủy lịch hẹn"));
    }

    /**
     * Lấy chi tiết lịch hẹn theo ID
     * GET /api/citizen/appointments/{id}?cccd={cccd}
     */
    @GetMapping("/appointments/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAppointmentById(
            @PathVariable Integer id,
            @RequestParam String cccd) {

        Application app = applicationRepository.findById(id)
                .orElse(null);

        if (app == null) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("NOT_FOUND", "Không tìm thấy lịch hẹn"));
        }

        // Verify ownership
        if (!app.getCitizen().getCitizenId().equals(cccd)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("FORBIDDEN", "Bạn không có quyền xem lịch hẹn này"));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", app.getId());
        result.put("code", app.getApplicationCode());
        result.put("procedureName", app.getProcedure().getProcedureName());
        result.put("procedureCode", app.getProcedure().getProcedureCode());
        result.put("status", getStatusName(app.getCurrentPhase()));
        result.put("queueDisplay", app.getQueueDisplay());
        result.put("queueNumber", app.getQueueNumber());
        result.put("citizenName", app.getCitizen().getFullName());
        result.put("createdAt", app.getCreatedAt());
        result.put("deadline", app.getDeadline());

        // Calculate queue position (how many people ahead)
        int queuePosition = 0;
        if (app.getCurrentPhase() == Application.PHASE_QUEUE || app.getCurrentPhase() == Application.PHASE_PENDING) {
            LocalDate today = LocalDate.now();
            List<Application> queueApps = applicationRepository.findAll().stream()
                    .filter(a -> (a.getCurrentPhase() == Application.PHASE_QUEUE
                            || a.getCurrentPhase() == Application.PHASE_PENDING))
                    .filter(a -> a.getCreatedAt() != null && a.getCreatedAt().toLocalDate().equals(today))
                    .filter(a -> a.getQueueNumber() != null && a.getQueueNumber() < app.getQueueNumber())
                    .toList();
            queuePosition = queueApps.size();
        }
        result.put("peopleAhead", queuePosition);
        result.put("estimatedWaitMinutes", queuePosition * 15);

        // Current serving number (placeholder - would be from queue service)
        result.put("currentServing", null);

        // Get appointment info from history
        List<ApplicationHistory> histories = applicationHistoryRepository.findLatestAppointmentHistory(app.getId());
        if (!histories.isEmpty()) {
            result.put("appointmentDate", histories.get(0).getAppointmentDate());
            result.put("appointmentTime", histories.get(0).getExpectedTime());
        }

        // Get counter if assigned
        result.put("counter", null);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ==================== HỒ SƠ ====================

    /**
     * Lấy danh sách hồ sơ của công dân
     * GET /api/citizen/applications?cccd={cccd}&status={status}
     */
    @GetMapping("/applications")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getApplications(
            @RequestParam String cccd,
            @RequestParam(required = false) String status) {

        // Sử dụng lại logic getAppointments
        return getAppointments(cccd, status);
    }

    /**
     * Lấy chi tiết hồ sơ
     * GET /api/citizen/applications/{id}?cccd={cccd}
     */
    @GetMapping("/applications/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getApplicationDetail(
            @PathVariable Integer id,
            @RequestParam String cccd) {

        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ"));

        // Verify ownership
        if (!app.getCitizen().getCitizenId().equals(cccd)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("UNAUTHORIZED", "Bạn không có quyền xem hồ sơ này"));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", app.getId());
        result.put("code", app.getApplicationCode());
        result.put("procedureName", app.getProcedure().getProcedureName());
        result.put("procedureCode", app.getProcedure().getProcedureCode());
        result.put("status", getStatusName(app.getCurrentPhase()));
        result.put("statusCode", app.getCurrentPhase());
        result.put("queueDisplay", app.getQueueDisplay());
        result.put("createdAt", app.getCreatedAt());
        result.put("deadline", app.getDeadline());
        // notes field not available

        // Citizen info
        result.put("citizenName", app.getCitizen().getFullName());
        result.put("citizenId", app.getCitizen().getCitizenId());
        result.put("phone", app.getCitizen().getPhone());

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Lấy lịch sử xử lý hồ sơ
     * GET /api/citizen/applications/{id}/history?cccd={cccd}
     */
    @GetMapping("/applications/{id}/history")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getApplicationHistory(
            @PathVariable Integer id,
            @RequestParam String cccd) {

        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ"));

        // Verify ownership
        if (!app.getCitizen().getCitizenId().equals(cccd)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("UNAUTHORIZED", "Bạn không có quyền xem hồ sơ này"));
        }

        List<ApplicationHistory> histories = applicationHistoryRepository.findByApplicationId(id);

        List<Map<String, Object>> result = histories.stream().map(h -> {
            Map<String, Object> map = new HashMap<>();
            map.put("action", h.getAction());
            map.put("content", h.getContent());
            map.put("createdAt", h.getCreatedAt());
            map.put("staffName", h.getStaff() != null ? h.getStaff().getFullName() : "Hệ thống");
            map.put("statusFrom", getStatusName(h.getPhaseFrom()));
            map.put("statusTo", getStatusName(h.getPhaseTo()));
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ==================== HÀNG CHỜ ====================

    /**
     * Tra cứu trạng thái hàng chờ
     * GET /api/citizen/queue/{ticketCode}
     */
    @GetMapping("/queue/{ticketCode}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getQueueStatus(
            @PathVariable String ticketCode) {

        Application foundApp = applicationRepository.findByApplicationCode(ticketCode)
                .orElse(null);

        if (foundApp == null) {
            // Try to find by queue display
            foundApp = applicationRepository.findByQueueDisplay(ticketCode).orElse(null);
        }

        if (foundApp == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("NOT_FOUND", "Không tìm thấy số thứ tự"));
        }

        final Application app = foundApp; // Make effectively final for lambda
        LocalDate today = LocalDate.now();

        // Đếm số người đang chờ phía trước
        long waitingCount = 0;
        Integer appQueueNum = app.getQueueNumber() != null ? app.getQueueNumber() : 0;
        if (app.getCurrentPhase() == Application.PHASE_QUEUE || app.getCurrentPhase() == Application.PHASE_PENDING) {
            List<Application> allWaiting = applicationHistoryRepository.findApplicationsByAppointmentDate(today);
            final int finalAppQueueNum = appQueueNum;
            waitingCount = allWaiting.stream()
                    .filter(a -> (a.getCurrentPhase() == Application.PHASE_QUEUE
                            || a.getCurrentPhase() == Application.PHASE_PENDING)
                            && a.getQueueNumber() != null
                            && a.getQueueNumber() < finalAppQueueNum)
                    .count();
        }

        // Lấy số đang được gọi
        List<Application> processing = applicationHistoryRepository.findApplicationsByAppointmentDateAndPhase(today,
                Application.PHASE_PROCESSING);
        int currentServing = processing.isEmpty() ? 0
                : (processing.get(0).getQueueNumber() != null ? processing.get(0).getQueueNumber() : 0);

        Map<String, Object> result = new HashMap<>();
        result.put("ticketNumber", app.getQueueNumber());
        result.put("ticketDisplay", app.getQueueDisplay());
        result.put("currentServing", currentServing);
        result.put("waitingCount", waitingCount);
        result.put("estimatedWaitMinutes", waitingCount * 15);
        result.put("status", getQueueStatusName(app.getCurrentPhase()));
        result.put("procedureName", app.getProcedure().getProcedureName());

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ==================== GÓP Ý / PHẢN HỒI ====================

    /**
     * Gửi góp ý/phản hồi
     * POST /api/citizen/reports
     */
    @Transactional
    @PostMapping("/reports")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createReport(
            @RequestBody Map<String, Object> body) {

        Integer type = ((Number) body.get("type")).intValue();
        Integer applicationId = body.get("applicationId") != null ? ((Number) body.get("applicationId")).intValue()
                : null;
        String title = (String) body.get("title");
        String content = (String) body.get("content");
        String citizenId = (String) body.get("citizenId");
        String citizenName = (String) body.get("citizenName");
        String phone = (String) body.get("phone");

        if (title == null || content == null || citizenId == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("MISSING_FIELDS", "Thiếu thông tin bắt buộc"));
        }

        Application app = null;
        if (applicationId != null) {
            app = applicationRepository.findById(applicationId).orElse(null);
        }

        Citizen citizen = citizenRepository.findByCitizenId(citizenId)
                .orElseGet(() -> {
                    Citizen newCitizen = Citizen.builder()
                            .citizenId(citizenId)
                            .fullName(citizenName != null ? citizenName : "Công dân")
                            .phone(phone)
                            .build();
                    return citizenRepository.save(newCitizen);
                });

        Feedback feedback = Feedback.builder()
                .citizen(citizen)
                .application(app)
                .type(type)
                .title(title)
                .content(content)
                .status(0) // Pending
                .build();
        feedback = feedbackRepository.save(feedback);

        log.info("Citizen {} created feedback: {}", citizenId, title);

        Map<String, Object> result = new HashMap<>();
        result.put("id", feedback.getId());
        result.put("title", feedback.getTitle());
        result.put("status", "PENDING");

        return ResponseEntity.ok(ApiResponse.success(result, "Gửi góp ý thành công"));
    }

    /**
     * Lấy danh sách góp ý của công dân
     * GET /api/citizen/reports?cccd={cccd}
     */
    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getReports(
            @RequestParam String cccd) {

        List<Feedback> feedbacks = feedbackRepository.findByCitizenCitizenId(cccd);

        List<Map<String, Object>> result = feedbacks.stream().map(f -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", f.getId());
            map.put("type", f.getType());
            map.put("title", f.getTitle());
            map.put("status", getReportStatusName(f.getStatus()));
            map.put("createdAt", f.getCreatedAt());
            if (f.getApplication() != null) {
                map.put("applicationCode", f.getApplication().getApplicationCode());
            }
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ==================== HELPER METHODS ====================

    private String getStatusName(Integer phase) {
        if (phase == null)
            return "---";
        return switch (phase) {
            case 0 -> "CANCELLED";
            case 1 -> "IN_QUEUE";
            case 2 -> "PENDING";
            case 3 -> "PROCESSING";
            case 4 -> "COMPLETED";
            case 5 -> "RECEIVED";
            case 6 -> "SUPPLEMENT";
            default -> "UNKNOWN";
        };
    }

    private String getQueueStatusName(int phase) {
        return switch (phase) {
            case 0 -> "CANCELLED";
            case 1, 2 -> "WAITING";
            case 3 -> "CALLED";
            case 4, 5 -> "COMPLETED";
            case 6 -> "SUPPLEMENT";
            default -> "UNKNOWN";
        };
    }

    private String getReportStatusName(int status) {
        return switch (status) {
            case 0 -> "PENDING";
            case 1 -> "PROCESSING";
            case 2 -> "RESOLVED";
            default -> "UNKNOWN";
        };
    }

    /**
     * Normalize Vietnamese string for comparison
     * Removes diacritics and converts to lowercase
     */
    private String normalizeString(String s) {
        if (s == null)
            return "";
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").toLowerCase().trim();
    }
}
