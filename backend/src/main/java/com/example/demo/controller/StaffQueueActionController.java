package com.example.demo.controller;

import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.ApplicationResponse;
import com.example.demo.entity.Application;
import com.example.demo.entity.ApplicationHistory;
import com.example.demo.entity.Appointment;
import com.example.demo.entity.Staff;
import com.example.demo.repository.ApplicationHistoryRepository;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.AppointmentRepository;
import com.example.demo.service.StaffQueueService;
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

/**
 * Staff Queue Action Controller
 * Write endpoints: call-next, complete, receive, cancel, supplement.
 * Issue #7 fix: callNext() uses pessimistic-locked query per counter.
 * Issue #9 fix: supplement() sets PHASE_SUPPLEMENT (not PHASE_QUEUE).
 */
@RestController
@RequestMapping("/api/staff/queue")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('Staff') or hasRole('Admin')")
@Transactional
public class StaffQueueActionController {

    private final ApplicationRepository applicationRepository;
    private final ApplicationHistoryRepository applicationHistoryRepository;
    private final AppointmentRepository appointmentRepository;
    private final StaffQueueService staffQueueService;

    /**
     * Gọi số tiếp theo (hoặc số cụ thể).
     * Issue #7 fix: auto-select path uses pessimistic-locked, counter-filtered query.
     * POST /api/staff/queue/call-next
     */
    @PostMapping("/call-next")
    public ResponseEntity<ApiResponse<ApplicationResponse>> callNext(
            @RequestBody(required = false) Map<String, Integer> body,
            Authentication authentication) {

        Staff staff = staffQueueService.getCurrentStaff(authentication);

        // Fix #5: reject early if staff has no counter — consistent with getDashboard() guard
        if (staff.getCounter() == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("NO_COUNTER", "Bạn chưa được phân công quầy"));
        }

        LocalDate today = LocalDate.now();
        Integer counterId = staff.getCounter().getId();

        // Reject if there is already a record in PROCESSING for this counter
        List<Application> currentProcessingList =
                applicationHistoryRepository.findApplicationsByAppointmentDateAndPhaseAndCounter(
                        today, Application.PHASE_PROCESSING, counterId);

        if (!currentProcessingList.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("HAS_PROCESSING", "Vui lòng hoàn thành lượt hiện tại trước"));
        }

        Application nextApp = null;
        Integer requestedId = (body != null) ? body.get("id") : null;

        if (requestedId != null) {
            // Staff manually picks a specific application
            nextApp = applicationRepository.findById(requestedId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ"));

            if (nextApp.getCurrentPhase() != Application.PHASE_QUEUE
                    && nextApp.getCurrentPhase() != Application.PHASE_PENDING
                    && nextApp.getCurrentPhase() != Application.PHASE_SUPPLEMENT) {
                return ResponseEntity.badRequest().body(
                        ApiResponse.error("INVALID_STATUS", "Hồ sơ không ở trong hàng chờ hoặc danh sách hẹn"));
            }
        } else {
            // Issue #7: use pessimistic-locked, counter-filtered query to prevent race condition
            // counterId is guaranteed non-null (guarded above)
            List<Application> waitingApps =
                    applicationRepository.findOldestPendingForCounter(today, Application.PHASE_QUEUE, counterId);

            if (waitingApps.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success(null, "Không còn ai trong hàng chờ (đã check-in)"));
            }
            nextApp = waitingApps.get(0);
        }

        nextApp.setCurrentPhase(Application.PHASE_PROCESSING);
        nextApp = applicationRepository.save(nextApp);

        staffQueueService.saveHistory(nextApp, staff, "GỌI SỐ",
                Application.PHASE_QUEUE, Application.PHASE_PROCESSING,
                "Gọi số " + nextApp.getQueueDisplay());

        log.info("Called next: {} by {}", nextApp.getQueueDisplay(), staff.getStaffCode());

        return ResponseEntity.ok(ApiResponse.success(staffQueueService.mapToResponse(nextApp),
                "Đã gọi số " + nextApp.getQueueDisplay()));
    }

    /**
     * Hoàn thành lượt đang xử lý.
     * POST /api/staff/queue/{id}/complete
     */
    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<ApplicationResponse>> complete(
            @PathVariable Integer id,
            @RequestBody(required = false) Map<String, String> body,
            Authentication authentication) {

        Staff staff = staffQueueService.getCurrentStaff(authentication);
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch hẹn"));

        int oldPhase = app.getCurrentPhase();
        if (oldPhase != Application.PHASE_PROCESSING && oldPhase != Application.PHASE_RECEIVED) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("INVALID_STATE", "Lịch hẹn không ở trạng thái đang xử lý"));
        }

        app.setCurrentPhase(Application.PHASE_COMPLETED);
        app = applicationRepository.save(app);

        String ghiChu = body != null ? body.get("ghiChu") : null;
        staffQueueService.saveHistory(app, staff, "HOÀN THÀNH",
                oldPhase, Application.PHASE_COMPLETED,
                ghiChu != null ? ghiChu : "Hoàn thành xử lý");

        List<Appointment> activeApps = appointmentRepository.findActiveByApplicationId(app.getId());
        for (Appointment a : activeApps) {
            a.setStatus(Appointment.STATUS_COMPLETED);
            appointmentRepository.save(a);
        }

        log.info("Completed: {} by {}", app.getQueueDisplay(), staff.getStaffCode());
        return ResponseEntity.ok(ApiResponse.success(staffQueueService.mapToResponse(app), "Đã hoàn thành"));
    }

    /**
     * Tiếp nhận hồ sơ (chuyển trạng thái sang RECEIVED).
     * POST /api/staff/queue/{id}/receive
     */
    @PostMapping("/{id}/receive")
    public ResponseEntity<ApiResponse<ApplicationResponse>> receive(
            @PathVariable Integer id,
            @RequestBody(required = false) Map<String, String> body,
            Authentication authentication) {

        Staff staff = staffQueueService.getCurrentStaff(authentication);
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch hẹn"));

        if (app.getCurrentPhase() != Application.PHASE_PROCESSING) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("INVALID_STATE", "Lịch hẹn không ở trạng thái đang xử lý"));
        }

        String appointmentDateStr = body != null ? body.get("appointmentDate") : null;
        String expectedTimeStr = body != null ? body.get("expectedTime") : null;
        String historyContent = "Đã tiếp nhận hồ sơ";

        if (appointmentDateStr != null) {
            try {
                LocalDate deadline = LocalDate.parse(appointmentDateStr);
                app.setDeadline(deadline);
                historyContent += ". Hẹn trả: " + deadline;
                if (expectedTimeStr != null) historyContent += " " + expectedTimeStr;
            } catch (Exception e) {
                log.warn("Invalid date format: {}", appointmentDateStr);
            }
        }

        app.setCurrentPhase(Application.PHASE_RECEIVED);
        app = applicationRepository.save(app);

        staffQueueService.saveHistory(app, staff, "TIẾP NHẬN",
                Application.PHASE_PROCESSING, Application.PHASE_RECEIVED, historyContent);

        log.info("Received: {} by {}", app.getQueueDisplay(), staff.getStaffCode());
        return ResponseEntity.ok(ApiResponse.success(staffQueueService.mapToResponse(app), "Đã tiếp nhận hồ sơ"));
    }

    /**
     * Hủy / Đánh dấu khách không đến.
     * POST /api/staff/queue/{id}/cancel
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<ApplicationResponse>> cancel(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> body,
            Authentication authentication) {

        Staff staff = staffQueueService.getCurrentStaff(authentication);
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch hẹn"));

        String lyDo = (String) body.get("lyDo");
        Integer trangThai = body.get("trangThai") != null
                ? ((Number) body.get("trangThai")).intValue()
                : Application.CANCEL_NO_SHOW;

        int oldPhase = app.getCurrentPhase();
        app.setCurrentPhase(Application.PHASE_CANCELLED);
        app.setCancelReason(lyDo);
        app.setCancelType(trangThai);
        app = applicationRepository.save(app);

        staffQueueService.saveHistory(app, staff, "HỦY", oldPhase, Application.PHASE_CANCELLED, lyDo);

        List<Appointment> appointments = appointmentRepository.findActiveByApplicationId(app.getId());
        for (Appointment a : appointments) {
            a.setStatus(Appointment.STATUS_CANCELLED);
            appointmentRepository.save(a);
        }

        log.info("Cancelled: {} by {} - reason: {}", app.getQueueDisplay(), staff.getStaffCode(), lyDo);
        return ResponseEntity.ok(ApiResponse.success(staffQueueService.mapToResponse(app), "Đã hủy lượt"));
    }

    /**
     * Hẹn bổ sung hồ sơ.
     * Issue #9 fix: sets PHASE_SUPPLEMENT (6), not PHASE_QUEUE (1).
     * POST /api/staff/queue/{id}/supplement
     */
    @PostMapping("/{id}/supplement")
    public ResponseEntity<ApiResponse<ApplicationResponse>> supplement(
            @PathVariable Integer id,
            @RequestBody Map<String, String> body,
            Authentication authentication) {

        Staff staff = staffQueueService.getCurrentStaff(authentication);
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch hẹn"));

        // Fix #6: phase guard — cannot supplement a completed or cancelled application
        int currentPhase = app.getCurrentPhase();
        if (currentPhase == Application.PHASE_CANCELLED || currentPhase == Application.PHASE_COMPLETED) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("INVALID_STATE",
                            "Không thể hẹn bổ sung hồ sơ đã hoàn thành hoặc đã hủy"));
        }

        String dateStr = body.get("appointmentDate");
        String timeStr = body.get("appointmentTime");

        if (dateStr == null || timeStr == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("MISSING_DATA", "Thiếu ngày giờ hẹn"));
        }

        LocalDate date = LocalDate.parse(dateStr);
        LocalTime time = LocalTime.parse(timeStr.length() == 5 ? timeStr + ":00" : timeStr);

        int oldPhase = app.getCurrentPhase();
        // Issue #9 fix: PHASE_SUPPLEMENT (6), not PHASE_QUEUE (1)
        app.setCurrentPhase(Application.PHASE_SUPPLEMENT);
        app = applicationRepository.save(app);

        ApplicationHistory history = ApplicationHistory.builder()
                .application(app)
                .counter(staff.getCounter())
                .staff(staff)
                .action(ApplicationHistory.ACTION_RESCHEDULE)
                .phaseFrom(oldPhase)
                // Issue #9 fix: phaseTo = PHASE_SUPPLEMENT
                .phaseTo(Application.PHASE_SUPPLEMENT)
                .content("Hẹn bổ sung hồ sơ. Ngày hẹn: " + date + " " + time)
                .appointmentDate(date)
                .expectedTime(time)
                .createdAt(LocalDateTime.now())
                .build();
        applicationHistoryRepository.save(history);

        Appointment appointment = Appointment.builder()
                .application(app)
                .staff(staff)
                .appointmentDate(date)
                .appointmentTime(time)
                .status(Appointment.STATUS_SCHEDULED)
                .build();
        appointmentRepository.save(appointment);

        log.info("Supplement scheduled: {} at {} {} by {}", app.getApplicationCode(), date, time,
                staff.getStaffCode());
        return ResponseEntity.ok(ApiResponse.success(staffQueueService.mapToResponse(app), "Đã đặt lịch bổ sung"));
    }
}
