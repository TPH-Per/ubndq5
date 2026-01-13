package com.example.demo.dto.response;

import lombok.*;
import java.time.LocalDateTime;

/**
 * Response DTO cho Chuyên môn
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChuyenMonResponse {
    
    private Integer id;
    private String maChuyenMon;
    private String tenChuyenMon;
    private String moTa;
    private Boolean trangThai;
    private LocalDateTime ngayTao;
    
    // Thống kê
    private Integer soQuay;      // Số quầy thuộc chuyên môn này
    private Integer soThuTuc;    // Số thủ tục thuộc chuyên môn này
}
