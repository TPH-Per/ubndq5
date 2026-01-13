package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO để tạo Quầy mới
 * 
 * Endpoint: POST /api/admin/quays
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateQuayRequest {

    @NotBlank(message = "Mã quầy không được để trống")
    @Size(max = 10, message = "Mã quầy tối đa 10 ký tự")
    private String maQuay;

    @NotBlank(message = "Tên quầy không được để trống")
    @Size(max = 50, message = "Tên quầy tối đa 50 ký tự")
    private String tenQuay;

    @Size(max = 100, message = "Vị trí tối đa 100 ký tự")
    private String viTri;

    @NotBlank(message = "Prefix số không được để trống")
    @Size(max = 5, message = "Prefix số tối đa 5 ký tự")
    private String prefixSo;

    @NotNull(message = "Chuyên môn không được để trống")
    private Integer chuyenMonId;

    private String ghiChu;
}
