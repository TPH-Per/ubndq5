package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.request.CreateQuayRequest;
import com.example.demo.dto.request.UpdateQuayRequest;
import com.example.demo.dto.response.QuayResponse;
import com.example.demo.service.QuayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller quản lý Quầy (Admin only)
 * 
 * Base path: /api/admin/quays
 * 
 * Tất cả endpoints đều yêu cầu role Admin
 */
@RestController
@RequestMapping("/api/admin/quays")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('Admin')")
public class QuayController {
    
    private final QuayService quayService;
    
    /**
     * Lấy danh sách tất cả quầy
     * 
     * GET /api/admin/quays
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<QuayResponse>>> getAllQuays() {
        List<QuayResponse> quays = quayService.getAllQuays();
        return ResponseEntity.ok(
            ApiResponse.success(quays, "Lấy danh sách quầy thành công")
        );
    }
    
    /**
     * Lấy quầy theo ID
     * 
     * GET /api/admin/quays/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QuayResponse>> getQuayById(@PathVariable Integer id) {
        QuayResponse quay = quayService.getQuayById(id);
        return ResponseEntity.ok(
            ApiResponse.success(quay, "Lấy thông tin quầy thành công")
        );
    }
    
    /**
     * Tạo quầy mới
     * 
     * POST /api/admin/quays
     */
    @PostMapping
    public ResponseEntity<ApiResponse<QuayResponse>> createQuay(
            @Valid @RequestBody CreateQuayRequest request) {
        
        log.info("Admin tạo quầy mới: {}", request.getMaQuay());
        QuayResponse quay = quayService.createQuay(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.success(quay, "Tạo quầy thành công")
        );
    }
    
    /**
     * Cập nhật quầy
     * 
     * PUT /api/admin/quays/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<QuayResponse>> updateQuay(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateQuayRequest request) {
        
        log.info("Admin cập nhật quầy ID: {}", id);
        QuayResponse quay = quayService.updateQuay(id, request);
        
        return ResponseEntity.ok(
            ApiResponse.success(quay, "Cập nhật quầy thành công")
        );
    }
    
    /**
     * Xóa quầy (soft delete)
     * 
     * DELETE /api/admin/quays/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteQuay(@PathVariable Integer id) {
        log.info("Admin xóa quầy ID: {}", id);
        quayService.deleteQuay(id);
        
        return ResponseEntity.ok(
            ApiResponse.success("Khóa quầy thành công")
        );
    }
}
