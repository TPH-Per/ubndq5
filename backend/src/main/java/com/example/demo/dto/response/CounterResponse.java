package com.example.demo.dto.response;

import lombok.*;
import java.time.LocalDateTime;

/**
 * Counter/Quầy Response DTO với field tiếng Việt để tương thích frontend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CounterResponse {

    private Integer id;
    private String maQuay;
    private String tenQuay;
    private String viTri;
    private Integer chuyenMonId;
    private String tenChuyenMon;
    private Boolean trangThai;
    private String ghiChu;
    private LocalDateTime ngayTao;
    private Integer soNhanVien;
}
