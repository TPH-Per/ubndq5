package com.example.demo.controller;

import com.example.demo.dto.response.ApiResponse;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Citizen Application Controller - Hồ sơ hành chính cho công dân
 * POST /api/citizen/applications/search     → Tìm hồ sơ (CCCD in body, not URL)
 * POST /api/citizen/applications/{id}/view  → Chi tiết hồ sơ (CCCD in body)
 * POST /api/citizen/applications/{id}/history → Lịch sử xử lý (CCCD in body)
 */
@RestController
@RequestMapping("/api/citizen")
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CitizenApplicationController {

    private final ApplicationRepository applicationRepository;
    private final ApplicationHistoryRepository applicationHistoryRepository;

    /**
     * Tìm kiếm hồ sơ của công dân theo Zalo account
     * POST /api/citizen/applications/search
     * Body: { "zaloId": "...", "status": "UPCOMING|COMPLETED|CANCELLED" }
     */
    @PostMapping("/applications/search")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> searchApplications(
            @RequestBody Map<String, String> body) {

        String zaloId = body.get("zaloId");
        String status = body.get("status");

        if (zaloId == null || zaloId.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("MISSING_ZALO_ID", "Cần zaloId để tra cứu hồ sơ"));
        }

        List<Application> apps = applicationRepository.findByZaloAccount_ZaloIdOrderByCreatedAtDesc(zaloId);

        if (status != null) {
            apps = apps.stream().filter(app -> switch (status.toUpperCase()) {
                case "UPCOMING" -> app.getCurrentPhase() == Application.PHASE_PENDING
                        || app.getCurrentPhase() == Application.PHASE_QUEUE;
                case "COMPLETED" -> app.getCurrentPhase() == Application.PHASE_COMPLETED;
                case "CANCELLED" -> app.getCurrentPhase() == Application.PHASE_CANCELLED;
                default -> true;
            }).collect(Collectors.toList());
        }

        List<Map<String, Object>> result = apps.stream().map(app -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", app.getId());
            map.put("code", app.getApplicationCode());
            map.put("procedureName", app.getProcedure().getProcedureName());
            map.put("status", CitizenHelperUtils.getStatusName(app.getCurrentPhase()));
            map.put("queueDisplay", app.getQueueDisplay());
            map.put("createdAt", app.getCreatedAt());

            List<ApplicationHistory> histories =
                    applicationHistoryRepository.findLatestAppointmentHistory(app.getId());
            if (!histories.isEmpty()) {
                map.put("appointmentDate", histories.get(0).getAppointmentDate());
                map.put("appointmentTime", histories.get(0).getExpectedTime());
            }
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Xem chi tiết hồ sơ - xác thực qua zaloId
     * POST /api/citizen/applications/{id}/view
     * Body: { "zaloId": "..." }
     */
    @PostMapping("/applications/{id}/view")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getApplicationDetail(
            @PathVariable Integer id,
            @RequestBody Map<String, String> body) {

        String zaloId = body.get("zaloId");
        if (zaloId == null || zaloId.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("MISSING_ZALO_ID", "Cần zaloId để xem chi tiết hồ sơ"));
        }

        // Uniform 404 — prevents resource enumeration
        Application app = applicationRepository.findById(id).orElse(null);
        if (app == null || app.getZaloAccount() == null
                || !app.getZaloAccount().getZaloId().equals(zaloId)) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("NOT_FOUND", "Không tìm thấy hồ sơ"));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", app.getId());
        result.put("code", app.getApplicationCode());
        result.put("procedureName", app.getProcedure().getProcedureName());
        result.put("procedureCode", app.getProcedure().getProcedureCode());
        result.put("status", CitizenHelperUtils.getStatusName(app.getCurrentPhase()));
        result.put("statusCode", app.getCurrentPhase());
        result.put("queueDisplay", app.getQueueDisplay());
        result.put("createdAt", app.getCreatedAt());
        result.put("deadline", app.getDeadline());
        result.put("citizenName", app.getCitizenName());
        result.put("citizenCccd", app.getCitizenCccd());
        result.put("phone", app.getCitizenPhone());

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Xem lịch sử xử lý hồ sơ - xác thực qua zaloId
     * POST /api/citizen/applications/{id}/history
     * Body: { "zaloId": "..." }
     */
    @PostMapping("/applications/{id}/history")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getApplicationHistory(
            @PathVariable Integer id,
            @RequestBody Map<String, String> body) {

        String zaloId = body.get("zaloId");
        if (zaloId == null || zaloId.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("MISSING_ZALO_ID", "Cần zaloId để xem lịch sử hồ sơ"));
        }

        // Uniform 404 — prevents resource enumeration
        Application app = applicationRepository.findById(id).orElse(null);
        if (app == null || app.getZaloAccount() == null
                || !app.getZaloAccount().getZaloId().equals(zaloId)) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("NOT_FOUND", "Không tìm thấy hồ sơ"));
        }

        List<ApplicationHistory> histories = applicationHistoryRepository.findByApplicationId(id);

        List<Map<String, Object>> result = histories.stream().map(h -> {
            Map<String, Object> map = new HashMap<>();
            map.put("action", h.getAction());
            map.put("content", h.getContent());
            map.put("createdAt", h.getCreatedAt());
            map.put("staffName", h.getStaff() != null ? h.getStaff().getFullName() : "Hệ thống");
            map.put("statusFrom", CitizenHelperUtils.getStatusName(h.getPhaseFrom()));
            map.put("statusTo", CitizenHelperUtils.getStatusName(h.getPhaseTo()));
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
