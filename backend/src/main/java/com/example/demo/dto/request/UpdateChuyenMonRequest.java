package com.example.demo.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Request DTO để cập nhật Chuyên môn
 * 
 * Endpoint: PUT /api/admin/chuyenmons/{id}
 * 
 * Tất cả fields đều optional (chỉ gửi field cần sửa)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateChuyenMonRequest {

    @Size(max = 100, message = "Tên chuyên môn tối đa 100 ký tự")
    private String tenChuyenMon;  // Đổi tên (optional)

    private String moTa;          // Đổi mô tả (optional)

    private Boolean trangThai;    // Đổi trạng thái (optional)
}
