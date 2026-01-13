package com.example.demo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO để cập nhật user
 * 
 * Endpoint: PUT /api/admin/users/{id}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    
    @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
    private String hoTen;
    
    @Email(message = "Email không đúng định dạng")
    private String email;
    
    @Size(max = 15, message = "Số điện thoại tối đa 15 ký tự")
    private String soDienThoai;
    
    /**
     * Mật khẩu mới (null = không đổi)
     */
    @Size(min = 6, message = "Mật khẩu ít nhất 6 ký tự")
    private String password;
    
    /**
     * ID của role
     */
    private Integer roleId;
    
    /**
     * ID của quầy
     */
    private Integer quayId;
    
    /**
     * Trạng thái tài khoản
     */
    private Boolean trangThai;
}
