package com.example.demo.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho request đăng nhập
 * 
 * Giải thích cho người mới:
 * - @Data: Lombok tự động tạo getter, setter, toString, equals, hashCode
 * - @NotBlank: Validation - field này không được null hoặc rỗng
 * - DTO (Data Transfer Object): Object dùng để truyền dữ liệu giữa client và server
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    
    @NotBlank(message = "Mã nhân viên không được để trống")
    private String maNhanVien;
    
    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;
}
