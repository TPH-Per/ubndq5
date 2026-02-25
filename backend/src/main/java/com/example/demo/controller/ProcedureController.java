package com.example.demo.controller;

import com.example.demo.dto.request.CreateProcedureAdminRequest;
import com.example.demo.dto.request.UpdateProcedureAdminRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.ProcedureResponse;
import com.example.demo.entity.Procedure;
import com.example.demo.entity.ProcedureType;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.ProcedureRepository;
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
 * Procedure/Loại thủ tục Management Controller cho Admin
 */
@RestController
@RequestMapping("/api/admin/loaithutucs")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('Admin')")
@Transactional
public class ProcedureController {

    private final ProcedureRepository procedureRepository;
    private final ProcedureTypeRepository procedureTypeRepository;
    private final ApplicationRepository applicationRepository;

    /**
     * Lấy danh sách tất cả loại thủ tục
     * GET /api/admin/loaithutucs
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProcedureResponse>>> getAllProcedures() {
        log.info("Getting all procedures");

        List<ProcedureResponse> procedures = procedureRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(procedures));
    }

    /**
     * Lấy loại thủ tục theo ID
     * GET /api/admin/loaithutucs/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProcedureResponse>> getProcedureById(@PathVariable Integer id) {
        Procedure procedure = procedureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thủ tục với ID: " + id));

        return ResponseEntity.ok(ApiResponse.success(mapToResponse(procedure)));
    }

    /**
     * Lấy loại thủ tục theo chuyên môn
     * GET /api/admin/loaithutucs/by-chuyenmon/{chuyenMonId}
     */
    @GetMapping("/by-chuyenmon/{chuyenMonId}")
    public ResponseEntity<ApiResponse<List<ProcedureResponse>>> getProceduresByChuyenMon(
            @PathVariable Integer chuyenMonId) {

        List<ProcedureResponse> procedures = procedureRepository.findByProcedureTypeId(chuyenMonId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(procedures));
    }

    /**
     * Tạo loại thủ tục mới
     * POST /api/admin/loaithutucs
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ProcedureResponse>> createProcedure(
            @Valid @RequestBody CreateProcedureAdminRequest request) {

        log.info("Creating procedure: {}", request.getMaThuTuc());

        // Check if code exists
        if (procedureRepository.existsByProcedureCode(request.getMaThuTuc())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("DUPLICATE_CODE", "Mã thủ tục đã tồn tại: " + request.getMaThuTuc()));
        }

        // Get procedure type
        ProcedureType procedureType = procedureTypeRepository.findById(request.getChuyenMonId())
                .orElseThrow(
                        () -> new RuntimeException("Không tìm thấy chuyên môn với ID: " + request.getChuyenMonId()));

        // Create procedure
        Procedure procedure = Procedure.builder()
                .procedureCode(request.getMaThuTuc())
                .procedureName(request.getTenThuTuc())
                .description(request.getMoTa())
                .processingDays(request.getThoiGianXuLy() != null ? request.getThoiGianXuLy() : 15)
                .requiredDocuments(request.getGiayToYeuCau())
                .formSchema(request.getFormSchema())
                .displayOrder(request.getThuTu() != null ? request.getThuTu() : 0)
                .procedureType(procedureType)
                .isActive(true)
                .build();

        procedure = procedureRepository.save(procedure);
        log.info("Procedure created: {}", procedure.getProcedureCode());

        return ResponseEntity.ok(ApiResponse.success(mapToResponse(procedure), "Tạo thủ tục thành công"));
    }

    /**
     * Cập nhật loại thủ tục
     * PUT /api/admin/loaithutucs/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProcedureResponse>> updateProcedure(
            @PathVariable Integer id,
            @RequestBody UpdateProcedureAdminRequest request) {

        Procedure procedure = procedureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thủ tục với ID: " + id));

        log.info("Updating procedure: {}", procedure.getProcedureCode());

        // Update fields if provided
        if (request.getTenThuTuc() != null) {
            procedure.setProcedureName(request.getTenThuTuc());
        }
        if (request.getMoTa() != null) {
            procedure.setDescription(request.getMoTa());
        }
        if (request.getChuyenMonId() != null) {
            ProcedureType procedureType = procedureTypeRepository.findById(request.getChuyenMonId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyên môn"));
            procedure.setProcedureType(procedureType);
        }
        if (request.getThoiGianXuLy() != null) {
            procedure.setProcessingDays(request.getThoiGianXuLy());
        }
        if (request.getGiayToYeuCau() != null) {
            procedure.setRequiredDocuments(request.getGiayToYeuCau());
        }
        if (request.getFormSchema() != null) {
            procedure.setFormSchema(request.getFormSchema());
        }
        if (request.getThuTu() != null) {
            procedure.setDisplayOrder(request.getThuTu());
        }
        if (request.getTrangThai() != null) {
            procedure.setIsActive(request.getTrangThai());
        }

        procedure = procedureRepository.save(procedure);
        log.info("Procedure updated: {}", procedure.getProcedureCode());

        return ResponseEntity.ok(ApiResponse.success(mapToResponse(procedure), "Cập nhật thành công"));
    }

    /**
     * Xóa loại thủ tục (soft delete)
     * DELETE /api/admin/loaithutucs/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProcedure(@PathVariable Integer id) {
        Procedure procedure = procedureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thủ tục với ID: " + id));

        log.info("Deleting (deactivating) procedure: {}", procedure.getProcedureCode());

        procedure.setIsActive(false);
        procedureRepository.save(procedure);

        return ResponseEntity.ok(ApiResponse.success(null, "Đã khóa thủ tục"));
    }

    /**
     * Map Procedure entity to Response DTO
     */
    private ProcedureResponse mapToResponse(Procedure procedure) {
        // Count related applications
        long applicationCount = applicationRepository.countByProcedureId(procedure.getId());

        return ProcedureResponse.builder()
                .id(procedure.getId())
                .maThuTuc(procedure.getProcedureCode())
                .tenThuTuc(procedure.getProcedureName())
                .moTa(procedure.getDescription())
                .chuyenMonId(procedure.getProcedureType() != null ? procedure.getProcedureType().getId() : null)
                .tenChuyenMon(procedure.getProcedureType() != null ? procedure.getProcedureType().getName() : null)
                .thoiGianXuLy(procedure.getProcessingDays())
                .giayToYeuCau(procedure.getRequiredDocuments())
                .formSchema(procedure.getFormSchema())
                .thuTu(procedure.getDisplayOrder())
                .trangThai(procedure.getIsActive())
                .ngayTao(procedure.getCreatedAt())
                .soHoSo((int) applicationCount)
                .build();
    }
}
