package com.example.demo.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO để cập nhật Quầy
 * 
 * Endpoint: PUT /api/admin/quays/{id}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateQuayRequest {

    @Size(max = 50, message = "Tên quầy tối đa 50 ký tự")
    private String tenQuay;

    @Size(max = 100, message = "Vị trí tối đa 100 ký tự")
    private String viTri;

    @Size(max = 5, message = "Prefix số tối đa 5 ký tự")
    private String prefixSo;

    private Integer chuyenMonId;

    private String ghiChu;

    private Boolean trangThai;
}
