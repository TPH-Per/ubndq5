package com.example.demo.dto.response;

import lombok.*;
import java.time.LocalDateTime;

/**
 * ProcedureType/Chuyên môn Response DTO với field tiếng Việt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcedureTypeResponse {

    private Integer id;
    private String maChuyenMon;
    private String tenChuyenMon;
    private String moTa;
    private Boolean trangThai;
    private LocalDateTime ngayTao;
    private Integer soQuay; // Số quầy liên kết
    private Integer soThuTuc; // Số thủ tục liên kết
}
