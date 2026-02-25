package com.example.demo.dto.response;

import lombok.*;
import java.time.LocalDateTime;

/**
 * Procedure/Loại thủ tục Response DTO với field tiếng Việt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcedureResponse {

    private Integer id;
    private String maThuTuc;
    private String tenThuTuc;
    private String moTa;
    private Integer chuyenMonId;
    private String tenChuyenMon;
    private Integer thoiGianXuLy;
    private String giayToYeuCau;
    private String formSchema;
    private Integer thuTu;
    private Boolean trangThai;
    private LocalDateTime ngayTao;
    private Integer soHoSo; // Số hồ sơ liên kết
}
