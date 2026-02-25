package com.example.demo.controller;

import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.ApplicationResponse;
import com.example.demo.dto.response.QueueDashboardResponse;
import com.example.demo.entity.Application;
import com.example.demo.entity.ApplicationHistory;
import com.example.demo.entity.Appointment;
import com.example.demo.entity.Staff;
import com.example.demo.repository.ApplicationHistoryRepository;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.AppointmentRepository;
import com.example.demo.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Staff Queue Management Controller
 * API cho nhân viên quản lý hàng chờ tại quầy
 */
@RestController
@RequestMapping("/api/staff/queue")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('Staff') or hasRole('Admin')")
@Transactional
public class StaffQueueController {

        private final ApplicationRepository applicationRepository;
        private final ApplicationHistoryRepository applicationHistoryRepository;
        private final AppointmentRepository appointmentRepository;
        private final StaffRepository staffRepository;

        // Constants cho Slots
        private static final List<LocalTime> MORNING_SLOTS = new ArrayList<>();
        private static final List<LocalTime> AFTERNOON_SLOTS = new ArrayList<>();

        static {
                // Morning: 7:30 start, +24 mins
                LocalTime time = LocalTime.of(7, 30);
                for (int i = 0; i < 10; i++) {
                        MORNING_SLOTS.add(time);
                        time = time.plusMinutes(24);
                }
                // Afternoon: 13:00 start
                time = LocalTime.of(13, 0);
                for (int i = 0; i < 10; i++) {
                        AFTERNOON_SLOTS.add(time);
                        time = time.plusMinutes(24);
                }
        }

        /**
         * Lấy dashboard tổng hợp cho quầy của nhân viên
         * GET /api/staff/queue/dashboard
         */
        @GetMapping("/dashboard")
        public ResponseEntity<ApiResponse<QueueDashboardResponse>> getDashboard(Authentication authentication) {
                Staff staff = getCurrentStaff(authentication);

                if (staff.getCounter() == null) {
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.error("NO_COUNTER", "Bạn chưa được phân công quầy"));
                }

                LocalDate today = LocalDate.now();
                Integer counterId = staff.getCounter().getId();

                // Get active waiting list (QUEUE)
                List<ApplicationHistory> waitingHistories = applicationHistoryRepository.findActiveQueueHistories(today,
                                Application.PHASE_QUEUE);

                // Get pending list (PENDING - has appointment but not checked in yet)
                List<Application> pendingApps = applicationHistoryRepository
                                .findApplicationsByAppointmentDateAndPhase(today, Application.PHASE_PENDING);

                // Get supplement list (SUPPLEMENT - waiting for citizen to bring additional
                // docs)
                List<Application> supplementApps = applicationHistoryRepository
                                .findApplicationsByAppointmentDateAndPhase(today, Application.PHASE_SUPPLEMENT);

                LocalTime nowTime = LocalTime.now();
                List<ApplicationResponse> waitingResponses = new ArrayList<>();

                // Add Queue items first
                for (ApplicationHistory h : waitingHistories) {
                        // Check if late > 24 mins
                        if (h.getExpectedTime() != null && h.getExpectedTime().plusMinutes(24).isBefore(nowTime)) {
                                Application app = h.getApplication();
                                app.setCurrentPhase(Application.PHASE_CANCELLED);
                                app.setCancelReason("Tự động hủy do trễ hẹn quá 24 phút");
                                app.setCancelType(Application.CANCEL_NO_SHOW);
                                applicationRepository.save(app);

                                ApplicationHistory cancelHistory = ApplicationHistory.builder()
                                                .application(app)
                                                .staff(staff)
                                                .counter(staff.getCounter())
                                                .action(ApplicationHistory.ACTION_CANCEL_NO_SHOW)
                                                .phaseFrom(Application.PHASE_QUEUE)
                                                .phaseTo(Application.PHASE_CANCELLED)
                                                .content("Tự động hủy do quá giờ hẹn")
                                                .createdAt(java.time.LocalDateTime.now())
                                                .build();
                                applicationHistoryRepository.save(cancelHistory);

                                log.info("Auto cancelled application {} due to late arrival", app.getApplicationCode());
                                continue;
                        }

                        ApplicationResponse res = mapToResponse(h.getApplication());
                        res.setAppointmentDate(h.getAppointmentDate());
                        res.setExpectedTime(h.getExpectedTime());
                        waitingResponses.add(res);
                }

                // Add Pending items
                for (Application app : pendingApps) {
                        ApplicationResponse res = mapToResponse(app);
                        // For pending apps, we need to find their appointment time
                        List<ApplicationHistory> histories = applicationHistoryRepository
                                        .findLatestAppointmentHistory(app.getId());
                        if (!histories.isEmpty()) {
                                res.setAppointmentDate(histories.get(0).getAppointmentDate());
                                res.setExpectedTime(histories.get(0).getExpectedTime());
                        }
                        waitingResponses.add(res);
                }

                // Add Supplement items (citizens coming back to submit additional docs)
                for (Application app : supplementApps) {
                        ApplicationResponse res = mapToResponse(app);
                        List<ApplicationHistory> histories = applicationHistoryRepository
                                        .findLatestAppointmentHistory(app.getId());
                        if (!histories.isEmpty()) {
                                res.setAppointmentDate(histories.get(0).getAppointmentDate());
                                res.setExpectedTime(histories.get(0).getExpectedTime());
                        }
                        waitingResponses.add(res);
                }

                // Sort by time
                try {
                        waitingResponses.sort((a, b) -> {
                                if (a.getExpectedTime() == null)
                                        return 1;
                                if (b.getExpectedTime() == null)
                                        return -1;
                                return a.getExpectedTime().compareTo(b.getExpectedTime());
                        });
                } catch (Exception e) {
                        // Ignore sort errors
                }

                // Get current processing (phase = PROCESSING)
                List<Application> processingApps = applicationHistoryRepository
                                .findApplicationsByAppointmentDateAndPhase(today, Application.PHASE_PROCESSING);
                Application currentProcessing = processingApps.isEmpty() ? null : processingApps.get(0);

                // Get today's stats
                Long totalCompleted = applicationHistoryRepository.countByAppointmentDateAndPhase(today,
                                Application.PHASE_COMPLETED);
                Long totalCancelled = applicationHistoryRepository.countByAppointmentDateAndPhase(today,
                                Application.PHASE_CANCELLED);

                QueueDashboardResponse dashboard = QueueDashboardResponse.builder()
                                .counterId(counterId)
                                .counterName(staff.getCounter().getCounterName())
                                .counterCode(staff.getCounter().getCounterCode())
                                .currentProcessing(currentProcessing != null ? mapToResponse(currentProcessing) : null)
                                .waitingList(waitingResponses)
                                .totalWaiting(waitingResponses.size())
                                .totalCompleted(totalCompleted != null ? totalCompleted.intValue() : 0)
                                .totalCancelled(totalCancelled != null ? totalCancelled.intValue() : 0)
                                .averageProcessingTime(15) // Default 15 minutes
                                .build();

                return ResponseEntity.ok(ApiResponse.success(dashboard));
        }

        /**
         * Lấy danh sách đang chờ (bao gồm cả chưa check-in)
         * GET /api/staff/queue/waiting
         */
        @GetMapping("/waiting")
        public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getWaitingList() {
                LocalDate today = LocalDate.now();
                List<Application> waitingApps = applicationHistoryRepository
                                .findApplicationsByAppointmentDateAndPhase(today, Application.PHASE_QUEUE);

                List<Application> pendingApps = applicationHistoryRepository
                                .findApplicationsByAppointmentDateAndPhase(today, Application.PHASE_PENDING);

                List<Application> supplementApps = applicationHistoryRepository
                                .findApplicationsByAppointmentDateAndPhase(today, Application.PHASE_SUPPLEMENT);

                List<ApplicationResponse> responses = new ArrayList<>();

                for (Application app : waitingApps)
                        responses.add(mapToResponse(app));
                for (Application app : pendingApps)
                        responses.add(mapToResponse(app));
                for (Application app : supplementApps)
                        responses.add(mapToResponse(app));

                return ResponseEntity.ok(ApiResponse.success(responses));
        }

        /**
         * Lấy lượt đang xử lý hiện tại
         * GET /api/staff/queue/current
         */
        @GetMapping("/current")
        public ResponseEntity<ApiResponse<ApplicationResponse>> getCurrentProcessing(Authentication authentication) {
                Staff staff = getCurrentStaff(authentication);
                LocalDate today = LocalDate.now();

                List<Application> processingApps = applicationHistoryRepository
                                .findApplicationsByAppointmentDateAndPhase(today, Application.PHASE_PROCESSING);
                Application currentProcessing = processingApps.isEmpty() ? null : processingApps.get(0);

                return ResponseEntity.ok(ApiResponse.success(
                                currentProcessing != null ? mapToResponse(currentProcessing) : null));
        }

        /**
         * Gọi số tiếp theo (hoặc số cụ thể)
         * POST /api/staff/queue/call-next
         */
        @PostMapping("/call-next")
        public ResponseEntity<ApiResponse<ApplicationResponse>> callNext(
                        @RequestBody(required = false) Map<String, Integer> body,
                        Authentication authentication) {
                Staff staff = getCurrentStaff(authentication);
                LocalDate today = LocalDate.now();

                // Check if there's a current processing
                List<Application> currentProcessingList = applicationHistoryRepository
                                .findApplicationsByAppointmentDateAndPhase(today, Application.PHASE_PROCESSING);

                if (!currentProcessingList.isEmpty()) {
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.error("HAS_PROCESSING",
                                                        "Vui lòng hoàn thành lượt hiện tại trước"));
                }

                Application nextApp = null;
                Integer requestedId = (body != null) ? body.get("id") : null;

                if (requestedId != null) {
                        // Find specific app
                        nextApp = applicationRepository.findById(requestedId)
                                        .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ"));

                        if (nextApp.getCurrentPhase() != Application.PHASE_QUEUE
                                        && nextApp.getCurrentPhase() != Application.PHASE_PENDING
                                        && nextApp.getCurrentPhase() != Application.PHASE_SUPPLEMENT) {
                                return ResponseEntity.badRequest().body(
                                                ApiResponse.error("INVALID_STATUS",
                                                                "Hồ sơ không ở trong hàng chờ hoặc danh sách hẹn"));
                        }
                } else {
                        // Get first in queue (Prioritize QUEUE, then PENDING ? Or just QUEUE)
                        // Auto-call usually prioritizes those present (QUEUE).
                        List<Application> waitingApps = applicationHistoryRepository
                                        .findApplicationsByAppointmentDateAndPhase(today, Application.PHASE_QUEUE);

                        if (waitingApps.isEmpty()) {
                                // Fallback to PENDING? Maybe not for auto-call.
                                // Staff should manually pick if no one IS present but appointment exists.
                                return ResponseEntity.ok(
                                                ApiResponse.success(null, "Không còn ai trong hàng chờ (đã check-in)"));
                        }
                        nextApp = waitingApps.get(0);
                }

                nextApp.setCurrentPhase(Application.PHASE_PROCESSING);
                nextApp = applicationRepository.save(nextApp);

                // Log history
                saveHistory(nextApp, staff, "GỌI SỐ",
                                Application.PHASE_QUEUE, Application.PHASE_PROCESSING,
                                "Gọi số " + nextApp.getQueueDisplay());

                log.info("Called next: {} by {}", nextApp.getQueueDisplay(), staff.getStaffCode());

                return ResponseEntity.ok(
                                ApiResponse.success(mapToResponse(nextApp), "Đã gọi số " + nextApp.getQueueDisplay()));
        }

        /**
         * Hoàn thành lượt đang xử lý
         * POST /api/staff/queue/{id}/complete
         */
        @PostMapping("/{id}/complete")
        public ResponseEntity<ApiResponse<ApplicationResponse>> complete(
                        @PathVariable Integer id,
                        @RequestBody(required = false) Map<String, String> body,
                        Authentication authentication) {

                Staff staff = getCurrentStaff(authentication);
                Application app = applicationRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch hẹn"));

                int oldPhase = app.getCurrentPhase();
                if (oldPhase != Application.PHASE_PROCESSING && oldPhase != Application.PHASE_RECEIVED) {
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.error("INVALID_STATE",
                                                        "Lịch hẹn không ở trạng thái đang xử lý"));
                }

                app.setCurrentPhase(Application.PHASE_COMPLETED);
                app = applicationRepository.save(app);

                String ghiChu = body != null ? body.get("ghiChu") : null;
                saveHistory(app, staff, "HOÀN THÀNH",
                                oldPhase, Application.PHASE_COMPLETED,
                                ghiChu != null ? ghiChu : "Hoàn thành xử lý");

                // Update Appointment
                List<Appointment> activeApps = appointmentRepository.findActiveByApplicationId(app.getId());
                for (Appointment a : activeApps) {
                        a.setStatus(Appointment.STATUS_COMPLETED);
                        appointmentRepository.save(a);
                }

                log.info("Completed: {} by {}", app.getQueueDisplay(), staff.getStaffCode());

                return ResponseEntity.ok(ApiResponse.success(mapToResponse(app), "Đã hoàn thành"));
        }

        /**
         * Tiếp nhận hồ sơ (chuyển trạng thái sang RECEIVED)
         * POST /api/staff/queue/{id}/receive
         */
        @PostMapping("/{id}/receive")
        public ResponseEntity<ApiResponse<ApplicationResponse>> receive(
                        @PathVariable Integer id,
                        @RequestBody(required = false) Map<String, String> body,
                        Authentication authentication) {

                Staff staff = getCurrentStaff(authentication);
                Application app = applicationRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch hẹn"));

                if (app.getCurrentPhase() != Application.PHASE_PROCESSING) {
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.error("INVALID_STATE",
                                                        "Lịch hẹn không ở trạng thái đang xử lý"));
                }

                // Cập nhật deadline nếu có
                String appointmentDateStr = body != null ? body.get("appointmentDate") : null;
                String expectedTimeStr = body != null ? body.get("expectedTime") : null;

                String historyContent = "Đã tiếp nhận hồ sơ";
                if (appointmentDateStr != null) {
                        try {
                                LocalDate deadline = LocalDate.parse(appointmentDateStr);
                                app.setDeadline(deadline);
                                historyContent += ". Hẹn trả: " + deadline;
                                if (expectedTimeStr != null) {
                                        historyContent += " " + expectedTimeStr;
                                }
                        } catch (Exception e) {
                                log.warn("Invalid date format: {}", appointmentDateStr);
                        }
                }

                app.setCurrentPhase(Application.PHASE_RECEIVED);
                app = applicationRepository.save(app);

                saveHistory(app, staff, "TIẾP NHẬN",
                                Application.PHASE_PROCESSING, Application.PHASE_RECEIVED,
                                historyContent);

                log.info("Received: {} by {}", app.getQueueDisplay(), staff.getStaffCode());

                return ResponseEntity.ok(ApiResponse.success(mapToResponse(app), "Đã tiếp nhận hồ sơ"));
        }

        /**
         * Hủy / Đánh dấu khách không đến
         * POST /api/staff/queue/{id}/cancel
         */
        @PostMapping("/{id}/cancel")
        public ResponseEntity<ApiResponse<ApplicationResponse>> cancel(
                        @PathVariable Integer id,
                        @RequestBody Map<String, Object> body,
                        Authentication authentication) {

                Staff staff = getCurrentStaff(authentication);
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

                saveHistory(app, staff, "HỦY",
                                oldPhase, Application.PHASE_CANCELLED,
                                lyDo);

                // Update Appointment
                List<Appointment> appointments = appointmentRepository.findActiveByApplicationId(app.getId());
                for (Appointment a : appointments) {
                        a.setStatus(Appointment.STATUS_CANCELLED);
                        appointmentRepository.save(a);
                }

                log.info("Cancelled: {} by {} - reason: {}", app.getQueueDisplay(), staff.getStaffCode(), lyDo);

                return ResponseEntity.ok(ApiResponse.success(mapToResponse(app), "Đã hủy lượt"));
        }

        /**
         * Lấy danh sách slot hẹn bổ sung còn trống
         * GET /api/staff/queue/slots?date={yyyy-MM-dd}
         */
        @GetMapping("/slots")
        public ResponseEntity<ApiResponse<Map<String, Object>>> getSlots(
                        @RequestParam LocalDate date) {

                List<LocalTime> bookedTimes = appointmentRepository.findBookedTimes(date);

                List<Map<String, Object>> morning = new ArrayList<>();
                for (LocalTime slot : MORNING_SLOTS) {
                        Map<String, Object> slotInfo = new HashMap<>();
                        slotInfo.put("time", slot.toString());
                        slotInfo.put("booked", bookedTimes.contains(slot));
                        morning.add(slotInfo);
                }

                List<Map<String, Object>> afternoon = new ArrayList<>();
                for (LocalTime slot : AFTERNOON_SLOTS) {
                        Map<String, Object> slotInfo = new HashMap<>();
                        slotInfo.put("time", slot.toString());
                        slotInfo.put("booked", bookedTimes.contains(slot));
                        afternoon.add(slotInfo);
                }

                Map<String, Object> result = new HashMap<>();
                result.put("morning", morning);
                result.put("afternoon", afternoon);

                return ResponseEntity.ok(ApiResponse.success(result));
        }

        /**
         * Hẹn bổ sung hồ sơ
         * POST /api/staff/queue/{id}/supplement
         */
        @PostMapping("/{id}/supplement")
        public ResponseEntity<ApiResponse<ApplicationResponse>> supplement(
                        @PathVariable Integer id,
                        @RequestBody Map<String, String> body,
                        Authentication authentication) {

                Staff staff = getCurrentStaff(authentication);
                Application app = applicationRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch hẹn"));

                String dateStr = body.get("appointmentDate");
                String timeStr = body.get("appointmentTime");

                if (dateStr == null || timeStr == null) {
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.error("MISSING_DATA", "Thiếu ngày giờ hẹn"));
                }

                LocalDate date = LocalDate.parse(dateStr);
                LocalTime time = LocalTime.parse(timeStr.length() == 5 ? timeStr + ":00" : timeStr);

                int oldPhase = app.getCurrentPhase();
                app.setCurrentPhase(Application.PHASE_QUEUE);
                app = applicationRepository.save(app);

                ApplicationHistory history = ApplicationHistory.builder()
                                .application(app)
                                .counter(staff.getCounter())
                                .staff(staff)
                                .action(ApplicationHistory.ACTION_RESCHEDULE)
                                .phaseFrom(oldPhase)
                                .phaseTo(Application.PHASE_QUEUE)
                                .content("Hẹn bổ sung hồ sơ. Ngày hẹn: " + date + " " + time)
                                .appointmentDate(date)
                                .expectedTime(time)
                                .createdAt(java.time.LocalDateTime.now())
                                .build();
                applicationHistoryRepository.save(history);

                // Create Appointment
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

                return ResponseEntity.ok(ApiResponse.success(mapToResponse(app), "Đã đặt lịch bổ sung"));
        }

        // ==================== HELPER METHODS ====================

        private Staff getCurrentStaff(Authentication authentication) {
                String staffCode = authentication.getName();
                return staffRepository.findByStaffCode(staffCode)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));
        }

        private void saveHistory(Application app, Staff staff, String action,
                        int oldPhase, int newPhase, String content) {
                ApplicationHistory history = ApplicationHistory.builder()
                                .application(app)
                                .counter(staff.getCounter())
                                .staff(staff)
                                .action(action)
                                .phaseFrom(oldPhase)
                                .phaseTo(newPhase)
                                .content(content)
                                .createdAt(java.time.LocalDateTime.now())
                                .build();
                applicationHistoryRepository.save(history);
        }

        private ApplicationResponse mapToResponse(Application app) {
                // Lấy thông tin lịch hẹn từ history (nếu có)
                List<ApplicationHistory> appointments = applicationHistoryRepository
                                .findLatestAppointmentHistory(app.getId());

                LocalDate appointmentDate = null;
                java.time.LocalTime expectedTime = null;

                if (!appointments.isEmpty()) {
                        ApplicationHistory latest = appointments.get(0);
                        appointmentDate = latest.getAppointmentDate();
                        expectedTime = latest.getExpectedTime();
                }

                return ApplicationResponse.builder()
                                .id(app.getId())
                                .applicationCode(app.getApplicationCode())
                                .procedureId(app.getProcedure().getId())
                                .procedureCode(app.getProcedure().getProcedureCode())
                                .procedureName(app.getProcedure().getProcedureName())
                                .citizenId(app.getCitizen().getCitizenId())
                                .citizenName(app.getCitizen().getFullName())
                                .citizenPhone(app.getCitizen().getPhone())
                                .currentPhase(app.getCurrentPhase())
                                .phaseName(ApplicationResponse.getPhaseName(app.getCurrentPhase()))
                                .queueNumber(app.getQueueNumber())
                                .queuePrefix(app.getQueuePrefix())
                                .queueDisplay(app.getQueueDisplay())
                                .appointmentDate(appointmentDate)
                                .expectedTime(expectedTime)
                                .deadline(app.getDeadline())
                                .priority(app.getPriority())
                                .priorityName(ApplicationResponse.getPriorityName(app.getPriority()))
                                .cancelReason(app.getCancelReason())
                                .cancelType(app.getCancelType())
                                .createdAt(app.getCreatedAt())
                                .updatedAt(app.getUpdatedAt())
                                .build();
        }
}
