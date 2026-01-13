package com.example.demo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO để tạo user mới
 * 
 * Endpoint: POST /api/admin/users
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
    
    @NotBlank(message = "Mã nhân viên không được để trống")
    @Size(max = 20, message = "Mã nhân viên tối đa 20 ký tự")
    private String maNhanVien;
    
    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
    private String hoTen;
    
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;
    
    @Size(max = 15, message = "Số điện thoại tối đa 15 ký tự")
    private String soDienThoai;
    
    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu ít nhất 6 ký tự")
    private String password;
    
    /**
     * ID của role (1 = Admin, 2 = NhanVien)
     */
    private Integer roleId;
    
    /**
     * ID của quầy (chỉ cần cho NhanVien)
     */
    private Integer quayId;
}
