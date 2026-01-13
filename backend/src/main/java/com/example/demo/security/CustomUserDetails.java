package com.example.demo.security;

import com.example.demo.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Custom implementation của UserDetails interface
 * 
 * UserDetails là interface của Spring Security, đại diện cho thông tin user
 * đã được xác thực. Spring Security sử dụng object này để:
 * - Kiểm tra password
 * - Kiểm tra quyền (authorities/roles)
 * - Kiểm tra trạng thái tài khoản (locked, expired, enabled...)
 * 
 * Tại sao cần custom?
 * - Để wrap Entity User của chúng ta vào định dạng Spring Security hiểu
 * - Để có thể truy cập thông tin User từ SecurityContext sau khi đăng nhập
 */
@Getter
public class CustomUserDetails implements UserDetails {
    
    /**
     * Lưu trữ entity User gốc để có thể lấy thông tin chi tiết sau
     */
    private final User user;
    
    public CustomUserDetails(User user) {
        this.user = user;
    }
    
    /**
     * Trả về danh sách quyền của user
     * 
     * GrantedAuthority: Đại diện cho 1 quyền (permission/role)
     * SimpleGrantedAuthority: Implementation đơn giản, chỉ cần tên role
     * 
     * Prefix "ROLE_" là convention của Spring Security cho role-based authorization
     * Ví dụ: ROLE_ADMIN, ROLE_NHANVIEN
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Lấy roleName từ entity Role và thêm prefix ROLE_
        String roleName = user.getRole().getRoleName();
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + roleName));
    }
    
    /**
     * Trả về password đã hash để Spring Security so sánh
     */
    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }
    
    /**
     * Trả về username (dùng mã nhân viên làm username)
     */
    @Override
    public String getUsername() {
        return user.getMaNhanVien();
    }
    
    /**
     * Tài khoản có hết hạn không?
     * true = chưa hết hạn (có thể đăng nhập)
     * 
     * Nếu cần tính năng này, thêm field expiryDate vào User entity
     */
    @Override
    public boolean isAccountNonExpired() {
        return true; // Không implement tính năng hết hạn tài khoản
    }
    
    /**
     * Tài khoản có bị khóa không?
     * true = không bị khóa (có thể đăng nhập)
     * 
     * Sử dụng field trangThai từ User entity
     */
    @Override
    public boolean isAccountNonLocked() {
        return user.getTrangThai() != null && user.getTrangThai();
    }
    
    /**
     * Credentials (password) có hết hạn không?
     * true = chưa hết hạn
     * 
     * Nếu cần tính năng đổi mật khẩu định kỳ, implement ở đây
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Không implement tính năng password expiry
    }
    
    /**
     * Tài khoản có được kích hoạt không?
     * true = đã kích hoạt (có thể đăng nhập)
     */
    @Override
    public boolean isEnabled() {
        return user.getTrangThai() != null && user.getTrangThai();
    }
    
    // ================== Helper methods ==================
    
    /**
     * Lấy ID của user
     */
    public Integer getId() {
        return user.getId();
    }
    
    /**
     * Lấy họ tên đầy đủ
     */
    public String getHoTen() {
        return user.getHoTen();
    }
    
    /**
     * Lấy tên role
     */
    public String getRoleName() {
        return user.getRole().getRoleName();
    }
}
