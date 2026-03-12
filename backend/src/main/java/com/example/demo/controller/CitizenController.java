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
import java.util.*;
import java.util.stream.Collectors;

/**
 * Citizen Controller - Thủ tục, hàng chờ, góp ý cho công dân
 *
 * GET /api/citizen/specialties → Danh sách lĩnh vực
 * GET /api/citizen/procedures → Danh sách thủ tục
 * GET /api/citizen/queue/{ticketCode} → Tra cứu hàng chờ (no PII in URL)
 * POST /api/citizen/reports → Gửi góp ý (CCCD in body)
 * POST /api/citizen/reports/search → Xem góp ý của công dân (CCCD in body, not
 * URL)
 *
 * Appointment endpoints → CitizenAppointmentController
 * Application endpoints → CitizenApplicationController
 */
@RestController
@RequestMapping("/api/citizen")
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CitizenController {

    private final ProcedureRepository procedureRepository;
    private final ProcedureTypeRepository procedureTypeRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationHistoryRepository applicationHistoryRepository;
    private final FeedbackRepository feedbackRepository;
    private final ReplyRepository replyRepository;

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
            String rawDocs = p.getRequiredDocuments();
            List<String> docsList = (rawDocs != null && !rawDocs.isBlank())
                    ? Arrays.stream(rawDocs.split("\n"))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toList())
                    : List.of();
            map.put("requiredDocuments", docsList);
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ==================== HÀNG CHỜ ====================

    /**
     * Tra cứu trạng thái hàng chờ bằng mã vé - no PII in URL
     * GET /api/citizen/queue/{ticketCode}
     */
    @GetMapping("/queue/{ticketCode}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getQueueStatus(
            @PathVariable String ticketCode) {

        Application foundApp = applicationRepository.findByApplicationCode(ticketCode).orElse(null);
        if (foundApp == null) {
            foundApp = applicationRepository.findByQueueDisplay(ticketCode).orElse(null);
        }

        if (foundApp == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("NOT_FOUND", "Không tìm thấy số thứ tự"));
        }

        final Application app = foundApp;
        LocalDate today = LocalDate.now();

        // Issue #10 fix: use countPeopleAhead() instead of loading all appointments
        // into memory
        long waitingCount = 0;
        Integer appQueueNum = app.getQueueNumber() != null ? app.getQueueNumber() : 0;
        if ((app.getCurrentPhase() == Application.PHASE_QUEUE
                || app.getCurrentPhase() == Application.PHASE_PENDING)
                && appQueueNum > 0) {
            waitingCount = applicationRepository.countPeopleAhead(today, appQueueNum);
        }

        List<Application> processing = applicationHistoryRepository
                .findApplicationsByAppointmentDateAndPhase(today, Application.PHASE_PROCESSING);
        int currentServing = processing.isEmpty() ? 0
                : (processing.get(0).getQueueNumber() != null ? processing.get(0).getQueueNumber() : 0);

        Map<String, Object> result = new HashMap<>();
        result.put("ticketNumber", app.getQueueNumber());
        result.put("ticketDisplay", app.getQueueDisplay());
        result.put("currentServing", currentServing);
        result.put("waitingCount", waitingCount);
        result.put("estimatedWaitMinutes", waitingCount * 15);
        result.put("status", CitizenHelperUtils.getQueueStatusName(app.getCurrentPhase()));
        result.put("procedureName", app.getProcedure().getProcedureName());

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ==================== GÓP Ý / PHẢN HỒI ====================

    /**
     * Gửi góp ý/phản hồi - CCCD in body
     * POST /api/citizen/reports
     */
    @Transactional
    @PostMapping("/reports")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createReport(
            @RequestBody Map<String, Object> body) {

        Integer type = ((Number) body.get("type")).intValue();
        Integer applicationId = body.get("applicationId") != null
                ? ((Number) body.get("applicationId")).intValue()
                : null;
        String title = (String) body.get("title");
        String content = (String) body.get("content");
        // Accept citizenCccd OR citizenId (legacy alias)
        String citizenCccd = body.get("citizenCccd") != null
                ? (String) body.get("citizenCccd")
                : (String) body.get("citizenId");
        String citizenName = (String) body.get("citizenName");
        String zaloId = (String) body.get("zaloId");

        if (citizenCccd == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("MISSING_FIELDS", "Thiếu thông tin bắt buộc"));
        }

        Integer rating = body.get("rating") != null
                ? ((Number) body.get("rating")).intValue()
                : null;

        Application app = null;
        if (applicationId != null) {
            app = applicationRepository.findById(applicationId).orElse(null);
        }

        Feedback feedback = Feedback.builder()
                .citizenCccd(citizenCccd)
                .citizenName(citizenName != null ? citizenName : "Ẩn danh")
                .zaloId(zaloId)
                .type(type)
                .title(title)
                .content(content)
                .rating(rating)
                .application(app)
                .status(Feedback.STATUS_PENDING)
                .build();
        feedback = feedbackRepository.save(feedback);

        log.info("Citizen {} submitted feedback: {}", citizenCccd, title);

        Map<String, Object> result = new HashMap<>();
        result.put("id", feedback.getId());
        result.put("title", feedback.getTitle());
        result.put("status", "PENDING");

        return ResponseEntity.ok(ApiResponse.success(result, "Gửi góp ý thành công"));
    }

    /**
     * Lấy danh sách góp ý của công dân.
     * POST /api/citizen/reports/search
     * Body: { "cccd": "...", "zaloId": "..." }
     *
     * Ownership rule:
     * - zaloId cung cấp → chỉ trả về feedback của Zalo account đó,
     * hoặc feedback ẩn danh (không có zaloId) khớp CCCD.
     * - Không có zaloId → chỉ trả về feedback ẩn danh khớp CCCD.
     * Ngăn enumeration CCCD-only cho Zalo-linked feedbacks.
     */
    @PostMapping("/reports/search")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> searchReports(
            @RequestBody Map<String, String> body) {

        String cccd = body.get("cccd");
        String zaloId = body.get("zaloId");
        if (cccd == null || !cccd.matches("\\d{12}")) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("INVALID_CCCD", "CCCD không hợp lệ"));
        }

        List<Feedback> feedbacks;
        if (zaloId != null && !zaloId.isBlank()) {
            // Verified path: feedbacks owned by this Zalo account (or anonymous w/ CCCD)
            feedbacks = feedbackRepository.findByZaloIdOrAnonymousCccd(zaloId, cccd);
        } else {
            // Legacy/unauthenticated: only anonymous (no-zaloId) feedbacks
            feedbacks = feedbackRepository.findByCitizenCccd(cccd).stream()
                    .filter(f -> f.getZaloId() == null || f.getZaloId().isBlank())
                    .collect(java.util.stream.Collectors.toList());
        }

        List<Map<String, Object>> result = feedbacks.stream().map(f -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", f.getId());
            map.put("type", f.getType());
            map.put("title", f.getTitle());
            map.put("content", f.getContent());
            map.put("status", CitizenHelperUtils.getReportStatusName(f.getStatus()));
            map.put("createdAt", f.getCreatedAt());
            if (f.getApplication() != null) {
                map.put("applicationCode", f.getApplication().getApplicationCode());
                map.put("applicationId", f.getApplication().getId());
            }
            // Include staff replies
            List<Map<String, Object>> replies = replyRepository.findByFeedbackId(f.getId()).stream()
                    .map(r -> {
                        Map<String, Object> rd = new HashMap<>();
                        rd.put("id", r.getId());
                        rd.put("content", r.getContent());
                        rd.put("staffName", r.getStaff().getFullName());
                        rd.put("createdAt", r.getCreatedAt());
                        return rd;
                    }).collect(Collectors.toList());
            map.put("replies", replies);
            map.put("rating", f.getRating());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
