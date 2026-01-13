package com.example.demo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO để Staff cập nhật thông tin cá nhân
 *
 * Endpoint: PUT /api/profile
 *
 * Staff CHỈ được phép cập nhật các thông tin cá nhân:
 * - Họ tên
 * - Email
 * - Số điện thoại
 * - Mật khẩu (với oldPassword để verify)
 *
 * KHÔNG được phép đổi:
 * - Role
 * - Quầy
 * - Trạng thái
 * - Mã nhân viên
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
    private String hoTen;

    @Email(message = "Email không đúng định dạng")
    private String email;

    @Size(max = 15, message = "Số điện thoại tối đa 15 ký tự")
    private String soDienThoai;

    /**
     * Mật khẩu cũ (bắt buộc nếu muốn đổi password)
     */
    private String oldPassword;

    /**
     * Mật khẩu mới (null = không đổi)
     */
    @Size(min = 6, message = "Mật khẩu mới ít nhất 6 ký tự")
    private String newPassword;
}

