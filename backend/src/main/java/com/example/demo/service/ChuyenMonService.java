package com.example.demo.service;

import com.example.demo.dto.request.CreateChuyenMonRequest;
import com.example.demo.dto.request.UpdateChuyenMonRequest;
import com.example.demo.dto.response.ChuyenMonResponse;
import com.example.demo.entity.ChuyenMon;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.ChuyenMonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service quản lý Chuyên môn (CRUD)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChuyenMonService {
    
    private final ChuyenMonRepository chuyenMonRepository;
    
    /**
     * Lấy danh sách tất cả chuyên môn
     */
    @Transactional(readOnly = true)
    public List<ChuyenMonResponse> getAllChuyenMons() {
        return chuyenMonRepository.findAll().stream()
                .map(this::mapToChuyenMonResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy chuyên môn theo ID
     */
    @Transactional(readOnly = true)
    public ChuyenMonResponse getChuyenMonById(Integer id) {
        ChuyenMon chuyenMon = chuyenMonRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR, "Không tìm thấy chuyên môn"));
        return mapToChuyenMonResponse(chuyenMon);
    }
    
    /**
     * Tạo chuyên môn mới
     */
    @Transactional
    public ChuyenMonResponse createChuyenMon(CreateChuyenMonRequest request) {
        log.info("Tạo chuyên môn mới: {}", request.getMaChuyenMon());
        
        // Kiểm tra mã chuyên môn đã tồn tại
        if (chuyenMonRepository.findByMaChuyenMon(request.getMaChuyenMon()).isPresent()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, 
                    "Mã chuyên môn " + request.getMaChuyenMon() + " đã tồn tại");
        }
        
        // Tạo entity
        ChuyenMon chuyenMon = ChuyenMon.builder()
                .maChuyenMon(request.getMaChuyenMon())
                .tenChuyenMon(request.getTenChuyenMon())
                .moTa(request.getMoTa())
                .trangThai(true)
                .build();
        
        ChuyenMon savedChuyenMon = chuyenMonRepository.save(chuyenMon);
        log.info("Đã tạo chuyên môn: {} - {}", savedChuyenMon.getMaChuyenMon(), savedChuyenMon.getTenChuyenMon());
        
        return mapToChuyenMonResponse(savedChuyenMon);
    }
    
    /**
     * Cập nhật chuyên môn
     */
    @Transactional
    public ChuyenMonResponse updateChuyenMon(Integer id, UpdateChuyenMonRequest request) {
        log.info("Cập nhật chuyên môn ID: {}", id);
        
        ChuyenMon chuyenMon = chuyenMonRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR, "Không tìm thấy chuyên môn"));
        
        // Cập nhật các field nếu có
        if (request.getTenChuyenMon() != null) {
            chuyenMon.setTenChuyenMon(request.getTenChuyenMon());
        }
        
        if (request.getMoTa() != null) {
            chuyenMon.setMoTa(request.getMoTa());
        }
        
        if (request.getTrangThai() != null) {
            chuyenMon.setTrangThai(request.getTrangThai());
        }
        
        ChuyenMon savedChuyenMon = chuyenMonRepository.save(chuyenMon);
        log.info("Đã cập nhật chuyên môn: {}", savedChuyenMon.getMaChuyenMon());
        
        return mapToChuyenMonResponse(savedChuyenMon);
    }
    
    /**
     * Xóa chuyên môn (soft delete)
     */
    @Transactional
    public void deleteChuyenMon(Integer id) {
        ChuyenMon chuyenMon = chuyenMonRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR, "Không tìm thấy chuyên môn"));
        
        chuyenMon.setTrangThai(false);
        chuyenMonRepository.save(chuyenMon);
        log.info("Đã khóa chuyên môn: {}", chuyenMon.getMaChuyenMon());
    }
    
    /**
     * Map ChuyenMon entity sang ChuyenMonResponse
     */
    private ChuyenMonResponse mapToChuyenMonResponse(ChuyenMon chuyenMon) {
        return ChuyenMonResponse.builder()
                .id(chuyenMon.getId())
                .maChuyenMon(chuyenMon.getMaChuyenMon())
                .tenChuyenMon(chuyenMon.getTenChuyenMon())
                .moTa(chuyenMon.getMoTa())
                .trangThai(chuyenMon.getTrangThai())
                .ngayTao(chuyenMon.getNgayTao())
                .soQuay(chuyenMon.getQuays() != null ? chuyenMon.getQuays().size() : 0)
                .soThuTuc(chuyenMon.getLoaiThuTucs() != null ? chuyenMon.getLoaiThuTucs().size() : 0)
                .build();
    }
}
