package com.example.demo.controller;

import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.ApplicationResponse;
import com.example.demo.dto.response.QueueDashboardResponse;
import com.example.demo.entity.Application;
import com.example.demo.entity.ApplicationHistory;
import com.example.demo.entity.Counter;
import com.example.demo.entity.Staff;
import com.example.demo.repository.ApplicationHistoryRepository;
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
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Staff Queue Dashboard Controller
 * Read-only dashboard endpoints: dashboard summary, waiting list, current
 * processing, slot availability.
 * Issue #6 fix: all queries filtered by counterId (staff's assigned counter).
 */
@RestController
@RequestMapping("/api/staff/queue")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('Staff') or hasRole('Admin')")
public class StaffQueueDashboardController {

    private final ApplicationHistoryRepository applicationHistoryRepository;
    private final AppointmentRepository appointmentRepository;
    private final StaffQueueService staffQueueService;

    // Slot constants: 7:30 start, 24-min intervals, 10 per session
    private static final List<LocalTime> MORNING_SLOTS = new ArrayList<>();
    private static final List<LocalTime> AFTERNOON_SLOTS = new ArrayList<>();

    static {
        LocalTime time = LocalTime.of(7, 30);
        for (int i = 0; i < 10; i++) {
            MORNING_SLOTS.add(time);
            time = time.plusMinutes(24);
        }
        time = LocalTime.of(13, 0);
        for (int i = 0; i < 10; i++) {
            AFTERNOON_SLOTS.add(time);
            time = time.plusMinutes(24);
        }
    }

    /**
     * Lấy dashboard tổng hợp cho quầy của nhân viên.
     * Filter by counter's procedureType — only show applications whose procedure
     * belongs to the same type.
     * GET /api/staff/queue/dashboard
     */
    @Transactional(readOnly = true)
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<QueueDashboardResponse>> getDashboard(Authentication authentication) {
        Staff staff = staffQueueService.getCurrentStaff(authentication);

        if (staff.getCounter() == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("NO_COUNTER", "Bạn chưa được phân công quầy"));
        }

        Counter counter = staff.getCounter();
        LocalDate today = LocalDate.now();
        Integer counterId = counter.getId();
        Integer procedureTypeId = counter.getProcedureType() != null ? counter.getProcedureType().getId() : null;

        if (procedureTypeId == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("NO_PROCEDURE_TYPE", "Quầy chưa được cấu hình loại thủ tục"));
        }

        List<ApplicationHistory> waitingHistories = applicationHistoryRepository
                .findActiveQueueHistoriesByProcedureType(today, Application.PHASE_QUEUE, procedureTypeId);

        List<Application> pendingApps = applicationHistoryRepository
                .findApplicationsByAppointmentDateAndPhaseAndProcedureType(today, Application.PHASE_PENDING,
                        procedureTypeId);

        List<Application> supplementApps = applicationHistoryRepository
                .findApplicationsByAppointmentDateAndPhaseAndProcedureType(today, Application.PHASE_SUPPLEMENT,
                        procedureTypeId);

        List<ApplicationResponse> waitingResponses = new ArrayList<>();

        for (ApplicationHistory h : waitingHistories) {
            ApplicationResponse res = staffQueueService.mapToResponse(h.getApplication());
            res.setAppointmentDate(h.getAppointmentDate());
            res.setExpectedTime(h.getExpectedTime());
            waitingResponses.add(res);
        }

        for (Application app : pendingApps) {
            ApplicationResponse res = staffQueueService.mapToResponse(app);
            List<ApplicationHistory> histories = applicationHistoryRepository.findLatestAppointmentHistory(app.getId());
            if (!histories.isEmpty()) {
                res.setAppointmentDate(histories.get(0).getAppointmentDate());
                res.setExpectedTime(histories.get(0).getExpectedTime());
            }
            waitingResponses.add(res);
        }

        for (Application app : supplementApps) {
            ApplicationResponse res = staffQueueService.mapToResponse(app);
            List<ApplicationHistory> histories = applicationHistoryRepository.findLatestAppointmentHistory(app.getId());
            if (!histories.isEmpty()) {
                res.setAppointmentDate(histories.get(0).getAppointmentDate());
                res.setExpectedTime(histories.get(0).getExpectedTime());
            }
            waitingResponses.add(res);
        }

        try {
            waitingResponses.sort((a, b) -> {
                if (a.getExpectedTime() == null)
                    return 1;
                if (b.getExpectedTime() == null)
                    return -1;
                return a.getExpectedTime().compareTo(b.getExpectedTime());
            });
        } catch (Exception e) {
            // ignore sort errors
        }

        List<Application> processingApps = applicationHistoryRepository
                .findApplicationsByAppointmentDateAndPhaseAndProcedureType(today, Application.PHASE_PROCESSING,
                        procedureTypeId);
        Application currentProcessing = processingApps.isEmpty() ? null : processingApps.get(0);

        Long totalCompleted = applicationHistoryRepository.countByAppointmentDateAndPhaseAndProcedureType(today,
                Application.PHASE_COMPLETED, procedureTypeId);
        Long totalCancelled = applicationHistoryRepository.countByAppointmentDateAndPhaseAndProcedureType(today,
                Application.PHASE_CANCELLED, procedureTypeId);

        QueueDashboardResponse dashboard = QueueDashboardResponse.builder()
                .counterId(counterId)
                .counterName(counter.getCounterName())
                .counterCode(counter.getCounterCode())
                .currentProcessing(
                        currentProcessing != null ? staffQueueService.mapToResponse(currentProcessing) : null)
                .waitingList(waitingResponses)
                .totalWaiting(waitingResponses.size())
                .totalCompleted(totalCompleted != null ? totalCompleted.intValue() : 0)
                .totalCancelled(totalCancelled != null ? totalCancelled.intValue() : 0)
                .averageProcessingTime(15)
                .build();

        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }

    /**
     * Lấy danh sách đang chờ — filtered by counter's procedureType.
     * GET /api/staff/queue/waiting
     */
    @Transactional(readOnly = true)
    @GetMapping("/waiting")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getWaitingList(Authentication authentication) {
        Staff staff = staffQueueService.getCurrentStaff(authentication);
        LocalDate today = LocalDate.now();
        Integer procedureTypeId = (staff.getCounter() != null && staff.getCounter().getProcedureType() != null)
                ? staff.getCounter().getProcedureType().getId()
                : null;

        List<Application> waitingApps;
        List<Application> pendingApps;
        List<Application> supplementApps;

        if (procedureTypeId != null) {
            waitingApps = applicationHistoryRepository.findApplicationsByAppointmentDateAndPhaseAndProcedureType(today,
                    Application.PHASE_QUEUE, procedureTypeId);
            pendingApps = applicationHistoryRepository.findApplicationsByAppointmentDateAndPhaseAndProcedureType(today,
                    Application.PHASE_PENDING, procedureTypeId);
            supplementApps = applicationHistoryRepository.findApplicationsByAppointmentDateAndPhaseAndProcedureType(
                    today, Application.PHASE_SUPPLEMENT, procedureTypeId);
        } else {
            waitingApps = applicationHistoryRepository.findApplicationsByAppointmentDateAndPhase(today,
                    Application.PHASE_QUEUE);
            pendingApps = applicationHistoryRepository.findApplicationsByAppointmentDateAndPhase(today,
                    Application.PHASE_PENDING);
            supplementApps = applicationHistoryRepository.findApplicationsByAppointmentDateAndPhase(today,
                    Application.PHASE_SUPPLEMENT);
        }

        List<ApplicationResponse> responses = new ArrayList<>();
        for (Application app : waitingApps)
            responses.add(staffQueueService.mapToResponse(app));
        for (Application app : pendingApps)
            responses.add(staffQueueService.mapToResponse(app));
        for (Application app : supplementApps)
            responses.add(staffQueueService.mapToResponse(app));

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Lấy lượt đang xử lý hiện tại — filtered by counter's procedureType.
     * GET /api/staff/queue/current
     */
    @Transactional(readOnly = true)
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<ApplicationResponse>> getCurrentProcessing(Authentication authentication) {
        Staff staff = staffQueueService.getCurrentStaff(authentication);
        LocalDate today = LocalDate.now();
        Integer procedureTypeId = (staff.getCounter() != null && staff.getCounter().getProcedureType() != null)
                ? staff.getCounter().getProcedureType().getId()
                : null;

        List<Application> processingApps;
        if (procedureTypeId != null) {
            processingApps = applicationHistoryRepository.findApplicationsByAppointmentDateAndPhaseAndProcedureType(
                    today, Application.PHASE_PROCESSING, procedureTypeId);
        } else {
            processingApps = applicationHistoryRepository.findApplicationsByAppointmentDateAndPhase(today,
                    Application.PHASE_PROCESSING);
        }
        Application current = processingApps.isEmpty() ? null : processingApps.get(0);

        return ResponseEntity
                .ok(ApiResponse.success(current != null ? staffQueueService.mapToResponse(current) : null));
    }

    /**
     * Lấy danh sách slot hẹn bổ sung còn trống.
     * Issue #8 fix: use count-based capacity (max 5 per slot), not boolean
     * contains.
     * GET /api/staff/queue/slots?date={yyyy-MM-dd}
     */
    @Transactional(readOnly = true)
    @GetMapping("/slots")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSlots(@RequestParam LocalDate date) {
        List<Map<String, Object>> morning = buildSlotList(MORNING_SLOTS, date);
        List<Map<String, Object>> afternoon = buildSlotList(AFTERNOON_SLOTS, date);

        Map<String, Object> result = new HashMap<>();
        result.put("morning", morning);
        result.put("afternoon", afternoon);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    private List<Map<String, Object>> buildSlotList(List<LocalTime> slots, LocalDate date) {
        List<Map<String, Object>> list = new ArrayList<>();
        final int MAX_CAPACITY = 5;
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        for (LocalTime slot : slots) {
            long bookedCount = appointmentRepository.countBookedByDateAndTime(date, slot);
            int available = Math.max(0, MAX_CAPACITY - (int) bookedCount);

            java.time.LocalDateTime slotDateTime = java.time.LocalDateTime.of(date, slot);
            boolean isTooSoon = now.plusHours(2).isAfter(slotDateTime);

            Map<String, Object> slotInfo = new HashMap<>();
            slotInfo.put("time", slot.toString());
            slotInfo.put("booked", available == 0 || isTooSoon);
            slotInfo.put("bookedCount", (int) bookedCount);
            slotInfo.put("availableCount", isTooSoon ? 0 : available);
            list.add(slotInfo);
        }
        return list;
    }
}
