package com.example.demo.controller;

import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.ProcedureResponse;
import com.example.demo.entity.Counter;
import com.example.demo.entity.Procedure;
import com.example.demo.entity.ProcedureType;
import com.example.demo.entity.Staff;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.ProcedureRepository;
import com.example.demo.repository.ProcedureTypeRepository;
import com.example.demo.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Staff Procedure Controller — CRUD thủ tục cho Staff
 * Staff chỉ được thao tác thủ tục thuộc chuyên môn của quầy mình.
 */
@RestController
@RequestMapping("/api/staff/procedures")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('Staff') or hasRole('Admin')")
public class StaffProcedureController {

    private final ProcedureRepository procedureRepository;
    private final ProcedureTypeRepository procedureTypeRepository;
    private final ApplicationRepository applicationRepository;
    private final StaffRepository staffRepository;

    /**
     * Lấy danh sách thủ tục thuộc chuyên môn của quầy nhân viên
     * GET /api/staff/procedures
     */
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<ProcedureResponse>>> getMyProcedures(
            Authentication authentication) {

        Staff staff = getCurrentStaff(authentication);
        Integer procedureTypeId = getStaffProcedureTypeId(staff);

        if (procedureTypeId == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("NO_PROCEDURE_TYPE",
                            "Quầy của bạn chưa được gán chuyên môn"));
        }

        List<Procedure> procedures = procedureRepository.findByProcedureTypeId(procedureTypeId);

        List<ProcedureResponse> responses = procedures.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Lấy chi tiết thủ tục (chỉ thuộc chuyên môn quầy)
     * GET /api/staff/procedures/{id}
     */
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<ProcedureResponse>> getById(
            @PathVariable Integer id,
            Authentication authentication) {

        Staff staff = getCurrentStaff(authentication);
        Integer procedureTypeId = getStaffProcedureTypeId(staff);

        Procedure procedure = procedureRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.LOAITHUTUC_NOT_FOUND));

        if (procedureTypeId == null || procedure.getProcedureType() == null
                || !procedure.getProcedureType().getId().equals(procedureTypeId)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("FORBIDDEN",
                            "Bạn không có quyền xem thủ tục này (không thuộc chuyên môn quầy)"));
        }

        return ResponseEntity.ok(ApiResponse.success(mapToResponse(procedure)));
    }

    /**
     * Tạo thủ tục mới (tự động gán chuyên môn quầy)
     * POST /api/staff/procedures
     */
    @PostMapping
    @Transactional
    public ResponseEntity<ApiResponse<ProcedureResponse>> create(
            @RequestBody Map<String, Object> body,
            Authentication authentication) {

        Staff staff = getCurrentStaff(authentication);
        Integer procedureTypeId = getStaffProcedureTypeId(staff);

        if (procedureTypeId == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("NO_PROCEDURE_TYPE",
                            "Quầy của bạn chưa được gán chuyên môn, không thể tạo thủ tục"));
        }

        String maThuTuc = (String) body.get("maThuTuc");
        String tenThuTuc = (String) body.get("tenThuTuc");

        if (maThuTuc == null || maThuTuc.isBlank() || tenThuTuc == null || tenThuTuc.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("MISSING_FIELDS", "Mã thủ tục và tên thủ tục là bắt buộc"));
        }

        if (procedureRepository.existsByProcedureCode(maThuTuc)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("DUPLICATE_CODE", "Mã thủ tục đã tồn tại: " + maThuTuc));
        }

        ProcedureType procedureType = procedureTypeRepository.findById(procedureTypeId)
                .orElseThrow(() -> new AppException(ErrorCode.CHUYENMON_NOT_FOUND));

        Procedure procedure = Procedure.builder()
                .procedureCode(maThuTuc)
                .procedureName(tenThuTuc)
                .description((String) body.get("moTa"))
                .processingDays(body.get("thoiGianXuLy") != null
                        ? ((Number) body.get("thoiGianXuLy")).intValue()
                        : 15)
                .requiredDocuments((String) body.get("giayToYeuCau"))
                .displayOrder(body.get("thuTu") != null
                        ? ((Number) body.get("thuTu")).intValue()
                        : 0)
                .procedureType(procedureType)
                .isActive(true)
                .build();

        procedure = procedureRepository.save(procedure);
        log.info("Staff {} created procedure: {} under type {}", staff.getStaffCode(),
                procedure.getProcedureCode(), procedureType.getName());

        return ResponseEntity.ok(ApiResponse.success(mapToResponse(procedure), "Tạo thủ tục thành công"));
    }

    /**
     * Cập nhật thủ tục (chỉ thuộc chuyên môn quầy)
     * PUT /api/staff/procedures/{id}
     */
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<ProcedureResponse>> update(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> body,
            Authentication authentication) {

        Staff staff = getCurrentStaff(authentication);
        Integer procedureTypeId = getStaffProcedureTypeId(staff);

        Procedure procedure = procedureRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.LOAITHUTUC_NOT_FOUND));

        if (procedureTypeId == null || procedure.getProcedureType() == null
                || !procedure.getProcedureType().getId().equals(procedureTypeId)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("FORBIDDEN",
                            "Bạn không có quyền chỉnh sửa thủ tục này (không thuộc chuyên môn quầy)"));
        }

        if (body.get("tenThuTuc") != null) {
            procedure.setProcedureName((String) body.get("tenThuTuc"));
        }
        if (body.get("moTa") != null) {
            procedure.setDescription((String) body.get("moTa"));
        }
        if (body.get("thoiGianXuLy") != null) {
            procedure.setProcessingDays(((Number) body.get("thoiGianXuLy")).intValue());
        }
        if (body.get("giayToYeuCau") != null) {
            procedure.setRequiredDocuments((String) body.get("giayToYeuCau"));
        }
        if (body.get("thuTu") != null) {
            procedure.setDisplayOrder(((Number) body.get("thuTu")).intValue());
        }
        if (body.get("trangThai") != null) {
            procedure.setIsActive((Boolean) body.get("trangThai"));
        }

        procedure = procedureRepository.save(procedure);
        log.info("Staff {} updated procedure: {}", staff.getStaffCode(), procedure.getProcedureCode());

        return ResponseEntity.ok(ApiResponse.success(mapToResponse(procedure), "Cập nhật thủ tục thành công"));
    }

    /**
     * Xóa thủ tục (soft delete — chỉ thuộc chuyên môn quầy)
     * DELETE /api/staff/procedures/{id}
     */
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Integer id,
            Authentication authentication) {

        Staff staff = getCurrentStaff(authentication);
        Integer procedureTypeId = getStaffProcedureTypeId(staff);

        Procedure procedure = procedureRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.LOAITHUTUC_NOT_FOUND));

        if (procedureTypeId == null || procedure.getProcedureType() == null
                || !procedure.getProcedureType().getId().equals(procedureTypeId)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("FORBIDDEN",
                            "Bạn không có quyền xóa thủ tục này (không thuộc chuyên môn quầy)"));
        }

        procedure.setIsActive(false);
        procedureRepository.save(procedure);
        log.info("Staff {} deactivated procedure: {}", staff.getStaffCode(), procedure.getProcedureCode());

        return ResponseEntity.ok(ApiResponse.success(null, "Đã khóa thủ tục"));
    }

    // ==================== HELPER METHODS ====================

    private Staff getCurrentStaff(Authentication authentication) {
        String staffCode = authentication.getName();
        return staffRepository.findByStaffCode(staffCode)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private Integer getStaffProcedureTypeId(Staff staff) {
        Counter counter = staff.getCounter();
        if (counter == null)
            return null;
        ProcedureType pt = counter.getProcedureType();
        return pt != null ? pt.getId() : null;
    }

    private ProcedureResponse mapToResponse(Procedure procedure) {
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
