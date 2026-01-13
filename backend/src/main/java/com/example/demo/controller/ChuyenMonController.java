package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.request.CreateChuyenMonRequest;
import com.example.demo.dto.request.UpdateChuyenMonRequest;
import com.example.demo.dto.response.ChuyenMonResponse;
import com.example.demo.service.ChuyenMonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller quản lý Chuyên môn (Admin only)
 * 
 * Base path: /api/admin/chuyenmons
 * 
 * Tất cả endpoints đều yêu cầu role Admin
 */
@RestController
@RequestMapping("/api/admin/chuyenmons")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('Admin')")
public class ChuyenMonController {
    
    private final ChuyenMonService chuyenMonService;
    
    /**
     * Lấy danh sách tất cả chuyên môn
     * 
     * GET /api/admin/chuyenmons
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ChuyenMonResponse>>> getAllChuyenMons() {
        List<ChuyenMonResponse> chuyenMons = chuyenMonService.getAllChuyenMons();
        return ResponseEntity.ok(
            ApiResponse.success(chuyenMons, "Lấy danh sách chuyên môn thành công")
        );
    }
    
    /**
     * Lấy chuyên môn theo ID
     * 
     * GET /api/admin/chuyenmons/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ChuyenMonResponse>> getChuyenMonById(@PathVariable Integer id) {
        ChuyenMonResponse chuyenMon = chuyenMonService.getChuyenMonById(id);
        return ResponseEntity.ok(
            ApiResponse.success(chuyenMon, "Lấy thông tin chuyên môn thành công")
        );
    }
    
    /**
     * Tạo chuyên môn mới
     * 
     * POST /api/admin/chuyenmons
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ChuyenMonResponse>> createChuyenMon(
            @Valid @RequestBody CreateChuyenMonRequest request) {
        
        log.info("Admin tạo chuyên môn mới: {}", request.getMaChuyenMon());
        ChuyenMonResponse chuyenMon = chuyenMonService.createChuyenMon(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.success(chuyenMon, "Tạo chuyên môn thành công")
        );
    }
    
    /**
     * Cập nhật chuyên môn
     * 
     * PUT /api/admin/chuyenmons/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ChuyenMonResponse>> updateChuyenMon(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateChuyenMonRequest request) {
        
        log.info("Admin cập nhật chuyên môn ID: {}", id);
        ChuyenMonResponse chuyenMon = chuyenMonService.updateChuyenMon(id, request);
        
        return ResponseEntity.ok(
            ApiResponse.success(chuyenMon, "Cập nhật chuyên môn thành công")
        );
    }
    
    /**
     * Xóa chuyên môn (soft delete)
     * 
     * DELETE /api/admin/chuyenmons/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteChuyenMon(@PathVariable Integer id) {
        log.info("Admin xóa chuyên môn ID: {}", id);
        chuyenMonService.deleteChuyenMon(id);
        
        return ResponseEntity.ok(
            ApiResponse.success("Khóa chuyên môn thành công")
        );
    }
}
