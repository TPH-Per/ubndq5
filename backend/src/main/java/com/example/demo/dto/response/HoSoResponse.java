package com.example.demo.dto.response;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Hồ sơ Response DTO với field tiếng Việt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoSoResponse {
    private Integer id;
    private String maHoSo;
    private String cccd;
    private String hoTenCongDan;
    private String soDienThoai;
    private String email;
    private String diaChi;
    private String tenThuTuc;
    private String maThuTuc;
    private String tenQuay;
    private Integer trangThai;
    private String trangThaiText;
    private Integer doUuTien;
    private LocalDateTime ngayNop;
    private LocalDate hanXuLy;
    private LocalDateTime ngayHoanThanh;
    private String nguonGoc;
    private String maLichHen;
    private Integer loaiThuTucId;
    private Integer thoiGianXuLyQuyDinh;
    private Map<String, Object> thongTinHoSo;
    private List<Map<String, Object>> fileDinhKem;
    private String ghiChu;
    private List<HistoryDto> lichSuXuLy;
    // Zalo account info
    private String zaloId;
    private String zaloName;

    // For dashboard stats
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardData {
        private Integer tongSoHoSo;
        private Integer choXuLy;
        private Integer dangXuLy;
        private Integer hoanThanh;
        private Integer treHan;
    }

    @Data
    @Builder
    public static class HistoryDto {
        private String nguoiXuLy;
        private String hanhDong;
        private String trangThaiCu;
        private String trangThaiMoi;
        private String noiDung;
        private LocalDateTime thoiGian;
    }

    // Phase text helper
    public static String getTrangThaiText(Integer trangThai) {
        if (trangThai == null)
            return "---";
        return switch (trangThai) {
            case 0 -> "Đã hủy";
            case 1 -> "Chờ gọi số";
            case 2 -> "Đang tiếp nhận";
            case 3 -> "Chờ gọi số";
            case 4 -> "Đã hoàn thành";
            case 5 -> "Đã tiếp nhận";
            case 6 -> "Bổ sung";
            default -> "Không xác định";
        };
    }
}
