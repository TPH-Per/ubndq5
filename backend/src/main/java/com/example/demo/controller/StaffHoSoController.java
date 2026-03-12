package com.example.demo.controller;

import com.example.demo.dto.request.CreateHoSoRequest;
import com.example.demo.dto.request.UpdateHoSoStatusRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.HoSoResponse;
import com.example.demo.service.StaffHoSoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Staff HoSo Management Controller - thin HTTP layer only.
 * All domain logic lives in {@link StaffHoSoService}.
 */
@RestController
@RequestMapping("/api/staff/hoso")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('Staff') or hasRole('Admin')")
public class StaffHoSoController {

    private final StaffHoSoService staffHoSoService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<HoSoResponse.DashboardData>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success(staffHoSoService.getDashboard()));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<HoSoResponse>>> getList(
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(staffHoSoService.getList(trangThai, page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HoSoResponse>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(staffHoSoService.getById(id)));
    }

    @GetMapping("/by-zalo/{zaloId}")
    public ResponseEntity<ApiResponse<List<HoSoResponse>>> getByZaloId(@PathVariable String zaloId) {
        return ResponseEntity.ok(ApiResponse.success(staffHoSoService.getByZaloId(zaloId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<HoSoResponse>> create(
            @Valid @RequestBody CreateHoSoRequest req,
            Authentication authentication) {
        HoSoResponse resp = staffHoSoService.create(req, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(resp, "Tao ho so thanh cong"));
    }

    @PostMapping("/from-lichhen/{lichHenId}")
    public ResponseEntity<ApiResponse<HoSoResponse>> createFromLichHen(
            @PathVariable Integer lichHenId,
            Authentication authentication) {
        HoSoResponse resp = staffHoSoService.createFromLichHen(lichHenId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(resp, "Tao ho so tu lich hen thanh cong"));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<HoSoResponse>> updateStatus(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateHoSoStatusRequest req,
            Authentication authentication) {
        HoSoResponse resp = staffHoSoService.updateStatus(id, req, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(resp, "Cap nhat trang thai thanh cong"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<HoSoResponse>> update(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> body,
            Authentication authentication) {
        HoSoResponse resp = staffHoSoService.update(id, body, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(resp, "Cap nhat ho so thanh cong"));
    }
}
