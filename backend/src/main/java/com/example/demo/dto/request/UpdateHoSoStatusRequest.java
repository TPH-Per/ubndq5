package com.example.demo.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateHoSoStatusRequest {

    @NotNull(message = "Thiếu trạng thái mới")
    private Integer trangThaiMoi;

    private String noiDung;
    /** ISO date yyyy-MM-dd — required when trangThaiMoi maps to SUPPLEMENT phase */
    private String ngayHen;
    /** HH:mm — required when trangThaiMoi maps to SUPPLEMENT phase */
    private String gioHen;
}
