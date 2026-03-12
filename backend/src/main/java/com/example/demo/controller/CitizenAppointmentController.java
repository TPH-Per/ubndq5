package com.example.demo.controller;

import com.example.demo.dto.request.CreateAppointmentRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.repository.AppointmentRepository;
import com.example.demo.service.AppointmentBookingService;
import com.example.demo.service.CitizenAppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * Citizen Appointment Controller — thin HTTP layer only.
 * All domain logic lives in {@link CitizenAppointmentService}.
 *
 * GET /api/citizen/appointments/available-slots → Slot khả dụng (no PII)
 * POST /api/citizen/appointments → Đặt lịch hẹn mới
 * POST /api/citizen/appointments/search → Tìm lịch hẹn theo Zalo
 * POST /api/citizen/appointments/{id}/cancel → Hủy lịch hẹn
 * POST /api/citizen/appointments/{id}/view → Xem chi tiết (zaloId in body)
 */
@RestController
@RequestMapping("/api/citizen")
@RequiredArgsConstructor
@Slf4j
public class CitizenAppointmentController {

    private final CitizenAppointmentService appointmentService;
    private final AppointmentRepository appointmentRepository;

    /**
     * Lấy slot khả dụng — no PII, no auth required.
     * GET /api/citizen/appointments/available-slots?date={yyyy-MM-dd}
     */
    @GetMapping("/appointments/available-slots")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAvailableSlots(
            @RequestParam String date,
            @RequestParam(required = false) Integer procedureId) {

        LocalDate targetDate = LocalDate.parse(date);
        final int MAX_CAPACITY = AppointmentBookingService.MAX_SLOT_CAPACITY;

        List<Map<String, Object>> slots = new ArrayList<>();
        addSlots(slots, LocalTime.of(7, 30), 10, targetDate, MAX_CAPACITY);
        addSlots(slots, LocalTime.of(13, 0), 10, targetDate, MAX_CAPACITY);

        Map<String, Object> result = new HashMap<>();
        result.put("date", date);
        result.put("slots", slots);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    private void addSlots(List<Map<String, Object>> out, LocalTime start, int count,
            LocalDate date, int maxCapacity) {
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < count; i++) {
            LocalTime t = start.plusMinutes(i * 24L);
            long booked = appointmentRepository.countBookedByDateAndTime(date, t);

            LocalDateTime slotDateTime = LocalDateTime.of(date, t);
            boolean isTooSoon = now.plusHours(2).isAfter(slotDateTime);

            Map<String, Object> slot = new HashMap<>();
            slot.put("time", t.toString());
            slot.put("available", isTooSoon ? 0 : Math.max(0, maxCapacity - (int) booked));
            slot.put("booked", (int) booked);
            slot.put("maxCapacity", maxCapacity);
            out.add(slot);
        }
    }

    /**
     * Đặt lịch hẹn mới — zaloId required, CCCD validated by DTO @Valid.
     * POST /api/citizen/appointments
     */
    @PostMapping("/appointments")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createAppointment(
            @Valid @RequestBody CreateAppointmentRequest req) {
        Map<String, Object> result = appointmentService.createAppointment(req);
        return ResponseEntity.ok(ApiResponse.success(result, "Đặt lịch thành công"));
    }

    /**
     * Tìm lịch hẹn theo Zalo account.
     * POST /api/citizen/appointments/search
     * Body: { "zaloId": "...", "status": "UPCOMING|COMPLETED|CANCELLED" }
     */
    @PostMapping("/appointments/search")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> searchAppointments(
            @RequestBody Map<String, String> body) {
        List<Map<String, Object>> result = appointmentService.searchAppointments(
                body.get("zaloId"), body.get("status"));
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Hủy lịch hẹn — xác thực qua zaloId.
     * POST /api/citizen/appointments/{id}/cancel
     * Body: { "zaloId": "..." }
     */
    @PostMapping("/appointments/{id}/cancel")
    public ResponseEntity<ApiResponse<Object>> cancelAppointment(
            @PathVariable Integer id,
            @RequestBody Map<String, String> body) {
        appointmentService.cancelAppointment(id, body.get("zaloId"));
        return ResponseEntity.ok(ApiResponse.success(null, "Đã hủy lịch hẹn"));
    }

    /**
     * Xem chi tiết lịch hẹn — CCCD masked, xác thực qua zaloId.
     * POST /api/citizen/appointments/{id}/view
     * Body: { "zaloId": "..." }
     */
    @PostMapping("/appointments/{id}/view")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAppointmentById(
            @PathVariable Integer id,
            @RequestBody Map<String, String> body) {
        Map<String, Object> result = appointmentService.getAppointmentDetail(id, body.get("zaloId"));
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
