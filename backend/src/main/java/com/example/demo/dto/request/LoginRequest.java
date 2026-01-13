package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO cho đăng nhập
 * 
 * Endpoint: POST /api/auth/login
 * 
 * Ví dụ request body:
 * {
 *   "maNhanVien": "NV001",
 *   "password": "123456"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    
    @NotBlank(message = "Mã nhân viên không được để trống")
    private String maNhanVien;
    
    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;
}
