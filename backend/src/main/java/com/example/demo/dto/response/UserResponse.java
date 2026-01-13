package com.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO chứa thông tin user
 * 
 * Chỉ chứa thông tin AN TOÀN để gửi về frontend.
 * KHÔNG chứa password, passwordHash, salt, etc.
 * 
 * Sử dụng cho:
 * - GET /api/auth/me
 * - Response trong LoginResponse
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    
    private Integer id;
    
    private String maNhanVien;
    
    private String hoTen;
    
    private String email;
    
    private String soDienThoai;
    
    /**
     * Tên role (Admin, NhanVien)
     */
    private String roleName;
    
    /**
     * Tên role hiển thị (Quản trị viên, Nhân viên)
     */
    private String roleDisplayName;
    
    /**
     * Tên quầy được phân công (nếu có)
     */
    private String tenQuay;
    
    /**
     * ID quầy được phân công (nếu có)
     */
    private Integer quayId;
    
    /**
     * Trạng thái tài khoản (true = active)
     */
    private Boolean trangThai;
    
    /**
     * Thời gian đăng nhập cuối
     */
    private LocalDateTime lanDangNhapCuoi;
}
