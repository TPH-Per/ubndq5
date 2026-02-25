package com.example.demo.dto.response;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Lịch hẹn Response DTO với field tiếng Việt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LichHenResponse {
    private Integer id;
    private String maLichHen;
    private Integer soThuTu;
    private String soThuTuDisplay;
    private String cccd;
    private String hoTenCongDan;
    private String soDienThoai;
    private String tenThuTuc;
    private String maThuTuc;
    private String tenQuay;
    private String maQuay;
    private LocalDate ngayHen;
    private LocalTime thoiGianDuKien;
    private LocalDateTime thoiGianGoiSo;
    private LocalDateTime thoiGianBatDauXuLy;
    private LocalDateTime thoiGianKetThuc;
    private Integer trangThai;
    private String trangThaiText;
    private String tenNhanVienXuLy;
    private String lyDoHuy;
}
