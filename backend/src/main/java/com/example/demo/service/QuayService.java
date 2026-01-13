package com.example.demo.service;

import com.example.demo.dto.request.CreateQuayRequest;
import com.example.demo.dto.request.UpdateQuayRequest;
import com.example.demo.dto.response.QuayResponse;
import com.example.demo.entity.ChuyenMon;
import com.example.demo.entity.Quay;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.QuayRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service quản lý Quầy (CRUD)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QuayService {
    
    private final QuayRepository quayRepository;
    private final EntityManager entityManager;
    
    /**
     * Lấy danh sách tất cả quầy
     */
    @Transactional(readOnly = true)
    public List<QuayResponse> getAllQuays() {
        return quayRepository.findAll().stream()
                .map(this::mapToQuayResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy quầy theo ID
     */
    @Transactional(readOnly = true)
    public QuayResponse getQuayById(Integer id) {
        Quay quay = quayRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COUNTER_NOT_FOUND, "Không tìm thấy quầy"));
        return mapToQuayResponse(quay);
    }
    
    /**
     * Tạo quầy mới
     */
    @Transactional
    public QuayResponse createQuay(CreateQuayRequest request) {
        log.info("Tạo quầy mới: {}", request.getMaQuay());
        
        // Kiểm tra mã quầy đã tồn tại
        if (quayRepository.findByMaQuay(request.getMaQuay()).isPresent()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, 
                    "Mã quầy " + request.getMaQuay() + " đã tồn tại");
        }
        
        // Lấy ChuyenMon
        ChuyenMon chuyenMon = entityManager.find(ChuyenMon.class, request.getChuyenMonId());
        if (chuyenMon == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Chuyên môn không hợp lệ");
        }
        
        // Tạo entity
        Quay quay = Quay.builder()
                .maQuay(request.getMaQuay())
                .tenQuay(request.getTenQuay())
                .viTri(request.getViTri())
                .prefixSo(request.getPrefixSo())
                .chuyenMon(chuyenMon)
                .ghiChu(request.getGhiChu())
                .trangThai(true)
                .build();
        
        Quay savedQuay = quayRepository.save(quay);
        log.info("Đã tạo quầy: {} - {}", savedQuay.getMaQuay(), savedQuay.getTenQuay());
        
        return mapToQuayResponse(savedQuay);
    }
    
    /**
     * Cập nhật quầy
     */
    @Transactional
    public QuayResponse updateQuay(Integer id, UpdateQuayRequest request) {
        log.info("Cập nhật quầy ID: {}", id);
        
        Quay quay = quayRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COUNTER_NOT_FOUND, "Không tìm thấy quầy"));
        
        // Cập nhật các field nếu có
        if (request.getTenQuay() != null) {
            quay.setTenQuay(request.getTenQuay());
        }
        
        if (request.getViTri() != null) {
            quay.setViTri(request.getViTri());
        }
        
        if (request.getPrefixSo() != null) {
            quay.setPrefixSo(request.getPrefixSo());
        }
        
        if (request.getChuyenMonId() != null) {
            ChuyenMon chuyenMon = entityManager.find(ChuyenMon.class, request.getChuyenMonId());
            if (chuyenMon != null) {
                quay.setChuyenMon(chuyenMon);
            }
        }
        
        if (request.getGhiChu() != null) {
            quay.setGhiChu(request.getGhiChu());
        }
        
        if (request.getTrangThai() != null) {
            quay.setTrangThai(request.getTrangThai());
        }
        
        Quay savedQuay = quayRepository.save(quay);
        log.info("Đã cập nhật quầy: {}", savedQuay.getMaQuay());
        
        return mapToQuayResponse(savedQuay);
    }
    
    /**
     * Xóa quầy (soft delete)
     */
    @Transactional
    public void deleteQuay(Integer id) {
        Quay quay = quayRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COUNTER_NOT_FOUND, "Không tìm thấy quầy"));
        
        quay.setTrangThai(false);
        quayRepository.save(quay);
        log.info("Đã khóa quầy: {}", quay.getMaQuay());
    }
    
    /**
     * Map Quay entity sang QuayResponse
     */
    private QuayResponse mapToQuayResponse(Quay quay) {
        return QuayResponse.builder()
                .id(quay.getId())
                .maQuay(quay.getMaQuay())
                .tenQuay(quay.getTenQuay())
                .viTri(quay.getViTri())
                .prefixSo(quay.getPrefixSo())
                .chuyenMonId(quay.getChuyenMon() != null ? quay.getChuyenMon().getId() : null)
                .tenChuyenMon(quay.getChuyenMon() != null ? quay.getChuyenMon().getTenChuyenMon() : null)
                .trangThai(quay.getTrangThai())
                .ghiChu(quay.getGhiChu())
                .ngayTao(quay.getNgayTao())
                .soNhanVien(quay.getUsers() != null ? (int) quay.getUsers().stream()
                        .filter(u -> u.getTrangThai()).count() : 0)
                .build();
    }
}
