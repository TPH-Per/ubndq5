package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateChuyenMonRequest {

    @NotBlank(message = "Mã chuyên môn không được để trống")
    @Size(max = 20, message = "Mã chuyên môn tối đa 20 ký tự")
    private String maChuyenMon;   // VD: CM01, DT-HT

    @NotBlank(message = "Tên chuyên môn không được để trống")
    @Size(max = 100, message = "Tên chuyên môn tối đa 100 ký tự")
    private String tenChuyenMon;  // VD: Dân số - Hộ tịch

    private String moTa;          // Mô tả (tùy chọn)
}