package com.example.demo.controller;

import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.HoSoResponse;
import com.example.demo.entity.Application;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.ApplicationHistoryRepository;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.service.StaffHoSoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin view of all hồ sơ across all counters.
 * GET /api/admin/hoso/dashboard  — stats for today and tomorrow
 * GET /api/admin/hoso            — paginated list (filter: date, quayId, trangThai)
 * GET /api/admin/hoso/{id}       — detail
 */
@RestController
@RequestMapping("/api/admin/hoso")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('Admin')")
public class AdminHoSoController {

    private final ApplicationHistoryRepository historyRepository;
    private final ApplicationRepository applicationRepository;
    private final StaffHoSoService staffHoSoService;

    /**
     * Dashboard stats for admin.
     * GET /api/admin/hoso/dashboard?date=2026-03-11
     *
     * Returns:
     * - tongHomNay   : total applications scheduled today
     * - tongNgayMai  : total applications scheduled tomorrow
     * - tongTatCa    : total in system
     * - perCounter   : [ { quayId, tenQuay, soLuong } ] for the requested date
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard(
            @RequestParam(required = false) String date) {

        LocalDate targetDate = parseDate(date, LocalDate.now());
        LocalDate today      = LocalDate.now();
        LocalDate tomorrow   = today.plusDays(1);

        long tongHomNay  = coalesce(historyRepository.countByDate(today));
        long tongNgayMai = coalesce(historyRepository.countByDate(tomorrow));
        long tongTatCa   = applicationRepository.count();

        List<Object[]> rows = historyRepository.countByDateGroupByCounter(targetDate);
        List<Map<String, Object>> perCounter = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("quayId",   row[0]);
            entry.put("tenQuay",  row[1]);
            entry.put("soLuong",  row[2]);
            perCounter.add(entry);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("tongHomNay",  tongHomNay);
        result.put("tongNgayMai", tongNgayMai);
        result.put("tongTatCa",   tongTatCa);
        result.put("ngayFilter",  targetDate.toString());
        result.put("perCounter",  perCounter);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Paginated list of hồ sơ across all counters.
     * GET /api/admin/hoso?date=today|tomorrow|YYYY-MM-DD&quayId=1&trangThai=2&page=0&size=20
     *
     * date param: "today" | "tomorrow" | ISO date string | omit for all records
     */
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<Page<HoSoResponse>>> getList(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) Integer quayId,
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        if (size > 100) size = 100;
        if (page < 0)   page = 0;

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Application> apps;

        if (date != null && !date.isBlank()) {
            LocalDate parsedDate = parseDate(date, LocalDate.now());
            if (quayId != null) {
                apps = historyRepository.findApplicationsByDateAndCounterPaged(parsedDate, quayId, pageable);
            } else {
                apps = historyRepository.findApplicationsByDatePaged(parsedDate, pageable);
            }
        } else {
            // No date filter: use full list (with optional phase filter)
            Integer phase = trangThai != null ? mapTrangThaiToPhase(trangThai) : null;
            apps = phase != null
                    ? applicationRepository.findByCurrentPhase(phase, pageable)
                    : applicationRepository.findAllPaged(pageable);
        }

        // Optional phase post-filter when date is provided (can't combine in subquery easily)
        Page<HoSoResponse> result = apps.map(staffHoSoService::mapToHoSoResponse);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Get detail of a single hồ sơ.
     * GET /api/admin/hoso/{id}
     */
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<HoSoResponse>> getById(@PathVariable Integer id) {
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.APPLICATION_NOT_FOUND));
        return ResponseEntity.ok(ApiResponse.success(staffHoSoService.mapToHoSoResponse(app)));
    }

    // ── helpers ──

    private LocalDate parseDate(String dateStr, LocalDate fallback) {
        if (dateStr == null || dateStr.isBlank()) return fallback;
        if ("today".equalsIgnoreCase(dateStr))    return LocalDate.now();
        if ("tomorrow".equalsIgnoreCase(dateStr)) return LocalDate.now().plusDays(1);
        try {
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            return fallback;
        }
    }

    private long coalesce(Long v) {
        return v != null ? v : 0L;
    }

    private int mapTrangThaiToPhase(int trangThai) {
        return switch (trangThai) {
            case 0  -> Application.PHASE_CANCELLED;
            case 1  -> Application.PHASE_QUEUE;
            case 2  -> Application.PHASE_PENDING;
            case 3  -> Application.PHASE_PROCESSING;
            case 4  -> Application.PHASE_COMPLETED;
            case 5  -> Application.PHASE_RECEIVED;
            case 6  -> Application.PHASE_SUPPLEMENT;
            default -> Application.PHASE_PENDING;
        };
    }
}
