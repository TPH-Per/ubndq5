package com.example.demo.controller;

import com.example.demo.dto.request.CreateProcedureTypeRequest;
import com.example.demo.dto.request.UpdateProcedureTypeRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.ProcedureTypeResponse;
import com.example.demo.entity.ProcedureType;
import com.example.demo.repository.ProcedureTypeRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ProcedureType/Chuyên môn Management Controller cho Admin
 */
@RestController
@RequestMapping("/api/admin/chuyenmons")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('Admin')")
@Transactional
public class ProcedureTypeController {

    private final ProcedureTypeRepository procedureTypeRepository;

    /**
     * Lấy danh sách tất cả chuyên môn
     * GET /api/admin/chuyenmons
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProcedureTypeResponse>>> getAllProcedureTypes() {
        log.info("Getting all procedure types");

        List<ProcedureTypeResponse> types = procedureTypeRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(types));
    }

    /**
     * Lấy chuyên môn theo ID
     * GET /api/admin/chuyenmons/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProcedureTypeResponse>> getProcedureTypeById(@PathVariable Integer id) {
        ProcedureType type = procedureTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyên môn với ID: " + id));

        return ResponseEntity.ok(ApiResponse.success(mapToResponse(type)));
    }

    /**
     * Tạo chuyên môn mới
     * POST /api/admin/chuyenmons
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ProcedureTypeResponse>> createProcedureType(
            @Valid @RequestBody CreateProcedureTypeRequest request) {

        log.info("Creating procedure type: {}", request.getMaChuyenMon());

        // Check if name exists
        if (procedureTypeRepository.existsByName(request.getTenChuyenMon())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("DUPLICATE_NAME",
                            "Tên chuyên môn đã tồn tại: " + request.getTenChuyenMon()));
        }

        // Create procedure type
        ProcedureType type = ProcedureType.builder()
                .name(request.getTenChuyenMon())
                .description(request.getMoTa())
                .isActive(true)
                .build();

        type = procedureTypeRepository.save(type);
        log.info("Procedure type created: {}", type.getName());

        return ResponseEntity.ok(ApiResponse.success(mapToResponse(type), "Tạo chuyên môn thành công"));
    }

    /**
     * Cập nhật chuyên môn
     * PUT /api/admin/chuyenmons/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProcedureTypeResponse>> updateProcedureType(
            @PathVariable Integer id,
            @RequestBody UpdateProcedureTypeRequest request) {

        ProcedureType type = procedureTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyên môn với ID: " + id));

        log.info("Updating procedure type: {}", type.getName());

        // Update fields if provided
        if (request.getTenChuyenMon() != null) {
            // Check if new name exists (excluding current)
            if (!request.getTenChuyenMon().equals(type.getName())
                    && procedureTypeRepository.existsByName(request.getTenChuyenMon())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("DUPLICATE_NAME", "Tên chuyên môn đã tồn tại"));
            }
            type.setName(request.getTenChuyenMon());
        }
        if (request.getMoTa() != null) {
            type.setDescription(request.getMoTa());
        }
        if (request.getTrangThai() != null) {
            type.setIsActive(request.getTrangThai());
        }

        type = procedureTypeRepository.save(type);
        log.info("Procedure type updated: {}", type.getName());

        return ResponseEntity.ok(ApiResponse.success(mapToResponse(type), "Cập nhật thành công"));
    }

    /**
     * Xóa chuyên môn (soft delete)
     * DELETE /api/admin/chuyenmons/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProcedureType(@PathVariable Integer id) {
        ProcedureType type = procedureTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyên môn với ID: " + id));

        log.info("Deleting (deactivating) procedure type: {}", type.getName());

        type.setIsActive(false);
        procedureTypeRepository.save(type);

        return ResponseEntity.ok(ApiResponse.success(null, "Đã khóa chuyên môn"));
    }

    /**
     * Map ProcedureType entity to Response DTO
     */
    private ProcedureTypeResponse mapToResponse(ProcedureType type) {
        // Count related entities
        long counterCount = procedureTypeRepository.countCountersByProcedureTypeId(type.getId());
        long procedureCount = procedureTypeRepository.countProceduresByProcedureTypeId(type.getId());

        return ProcedureTypeResponse.builder()
                .id(type.getId())
                .maChuyenMon("CM" + String.format("%02d", type.getId())) // Generate code from ID
                .tenChuyenMon(type.getName())
                .moTa(type.getDescription())
                .trangThai(type.getIsActive())
                .ngayTao(type.getCreatedAt())
                .soQuay((int) counterCount)
                .soThuTuc((int) procedureCount)
                .build();
    }
}
