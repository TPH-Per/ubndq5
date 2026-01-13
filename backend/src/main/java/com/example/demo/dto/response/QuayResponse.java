package com.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO cho Quầy
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuayResponse {

    private Integer id;
    private String maQuay;
    private String tenQuay;
    private String viTri;
    private String prefixSo;
    
    private Integer chuyenMonId;
    private String tenChuyenMon;
    
    private Boolean trangThai;
    private String ghiChu;
    private LocalDateTime ngayTao;
    
    // Thống kê (optional)
    private Integer soNhanVien;  // Số nhân viên đang làm việc tại quầy
}
