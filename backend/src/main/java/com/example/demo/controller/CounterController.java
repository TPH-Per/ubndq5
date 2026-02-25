package com.example.demo.controller;

import com.example.demo.dto.request.CreateCounterRequest;
import com.example.demo.dto.request.UpdateCounterRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.CounterResponse;
import com.example.demo.entity.Counter;
import com.example.demo.entity.ProcedureType;
import com.example.demo.repository.CounterRepository;
import com.example.demo.repository.ProcedureTypeRepository;
import com.example.demo.repository.StaffRepository;
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
 * Counter/Quầy Management Controller cho Admin
 */
@RestController
@RequestMapping("/api/admin/quays")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('Admin')")
@Transactional
public class CounterController {

    private final CounterRepository counterRepository;
    private final ProcedureTypeRepository procedureTypeRepository;
    private final StaffRepository staffRepository;

    /**
     * Lấy danh sách tất cả quầy
     * GET /api/admin/quays
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CounterResponse>>> getAllCounters() {
        log.info("Getting all counters");

        List<CounterResponse> counters = counterRepository.findAll().stream()
                .map(this::mapToCounterResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(counters));
    }

    /**
     * Lấy quầy theo ID
     * GET /api/admin/quays/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CounterResponse>> getCounterById(@PathVariable Integer id) {
        Counter counter = counterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy quầy với ID: " + id));

        return ResponseEntity.ok(ApiResponse.success(mapToCounterResponse(counter)));
    }

    /**
     * Tạo quầy mới
     * POST /api/admin/quays
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CounterResponse>> createCounter(
            @Valid @RequestBody CreateCounterRequest request) {
        log.info("Creating counter: {}", request.getMaQuay());

        // Check if counter code exists
        if (counterRepository.existsByCounterCode(request.getMaQuay())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("DUPLICATE_CODE", "Mã quầy đã tồn tại: " + request.getMaQuay()));
        }

        // Get procedure type if provided
        ProcedureType procedureType = null;
        if (request.getChuyenMonId() != null) {
            procedureType = procedureTypeRepository.findById(request.getChuyenMonId())
                    .orElseThrow(() -> new RuntimeException(
                            "Không tìm thấy chuyên môn với ID: " + request.getChuyenMonId()));
        }

        // Create counter
        Counter counter = Counter.builder()
                .counterCode(request.getMaQuay())
                .counterName(request.getTenQuay())
                .location(request.getViTri())
                .procedureType(procedureType)
                .notes(request.getGhiChu())
                .isActive(true)
                .build();

        counter = counterRepository.save(counter);
        log.info("Counter created: {}", counter.getCounterCode());

        return ResponseEntity.ok(ApiResponse.success(mapToCounterResponse(counter), "Tạo quầy thành công"));
    }

    /**
     * Cập nhật quầy
     * PUT /api/admin/quays/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CounterResponse>> updateCounter(
            @PathVariable Integer id,
            @RequestBody UpdateCounterRequest request) {

        Counter counter = counterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy quầy với ID: " + id));

        log.info("Updating counter: {}", counter.getCounterCode());

        // Update fields if provided
        if (request.getTenQuay() != null) {
            counter.setCounterName(request.getTenQuay());
        }
        if (request.getViTri() != null) {
            counter.setLocation(request.getViTri());
        }
        if (request.getChuyenMonId() != null) {
            ProcedureType procedureType = procedureTypeRepository.findById(request.getChuyenMonId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyên môn"));
            counter.setProcedureType(procedureType);
        }
        if (request.getGhiChu() != null) {
            counter.setNotes(request.getGhiChu());
        }
        if (request.getTrangThai() != null) {
            counter.setIsActive(request.getTrangThai());
        }

        counter = counterRepository.save(counter);
        log.info("Counter updated: {}", counter.getCounterCode());

        return ResponseEntity.ok(ApiResponse.success(mapToCounterResponse(counter), "Cập nhật thành công"));
    }

    /**
     * Xóa quầy (soft delete)
     * DELETE /api/admin/quays/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCounter(@PathVariable Integer id) {
        Counter counter = counterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy quầy với ID: " + id));

        log.info("Deleting (deactivating) counter: {}", counter.getCounterCode());

        counter.setIsActive(false);
        counterRepository.save(counter);

        return ResponseEntity.ok(ApiResponse.success(null, "Đã khóa quầy"));
    }

    /**
     * Map Counter entity to CounterResponse DTO
     */
    private CounterResponse mapToCounterResponse(Counter counter) {
        // Count staff assigned to this counter
        long staffCount = staffRepository.countByCounterId(counter.getId());

        return CounterResponse.builder()
                .id(counter.getId())
                .maQuay(counter.getCounterCode())
                .tenQuay(counter.getCounterName())
                .viTri(counter.getLocation())
                .chuyenMonId(counter.getProcedureType() != null ? counter.getProcedureType().getId() : null)
                .tenChuyenMon(counter.getProcedureType() != null ? counter.getProcedureType().getName() : null)
                .trangThai(counter.getIsActive())
                .ghiChu(counter.getNotes())
                .ngayTao(counter.getCreatedAt())
                .soNhanVien((int) staffCount)
                .build();
    }
}
