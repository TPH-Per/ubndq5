package com.example.demo.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreateHoSoRequest {

    @NotNull(message = "Thiếu loại thủ tục")
    private Integer loaiThuTucId;

    @Pattern(regexp = "\\d{12}", message = "CCCD phải có đúng 12 số")
    private String cccd;

    private String hoTen;
    private String soDienThoai;
    private String email;
    private Integer doUuTien;
    private String ghiChu;
}
