package com.example.demo.service;

import com.example.demo.dto.request.LoginRequest;
import com.example.demo.dto.response.LoginResponse;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.entity.User;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service xử lý Authentication (đăng nhập, đăng xuất)
 * 
 * Flow đăng nhập:
 * 1. Nhận LoginRequest (maNhanVien, password)
 * 2. Xác thực bằng AuthenticationManager
 * 3. Nếu đúng → Tạo JWT token
 * 4. Trả về LoginResponse (token + user info)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    
    /**
     * Xử lý đăng nhập
     * 
     * @param request Chứa maNhanVien và password
     * @return LoginResponse chứa JWT token và thông tin user
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("Đang xử lý đăng nhập cho: {}", request.getMaNhanVien());
        
        // Bước 1: Xác thực bằng Spring Security
        // AuthenticationManager sẽ gọi CustomUserDetailsService.loadUserByUsername()
        // và so sánh password với passwordHash trong DB
        // Nếu sai → Spring throw BadCredentialsException → GlobalExceptionHandler catch
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getMaNhanVien(),
                request.getPassword()
            )
        );
        
        // Bước 2: Lưu authentication vào SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // Bước 3: Lấy thông tin user từ authentication
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();
        
        // Bước 4: Cập nhật thời gian đăng nhập
        user.setLanDangNhapCuoi(LocalDateTime.now());
        userRepository.save(user);
        
        // Bước 5: Tạo JWT token
        String token = jwtUtils.generateToken(
            user.getId(),
            user.getMaNhanVien(),
            user.getRole().getRoleName()
        );
        
        // Bước 6: Tạo UserResponse để trả về (không chứa password)
        UserResponse userResponse = mapToUserResponse(user);
        
        log.info("Đăng nhập thành công: {} - Role: {}", user.getHoTen(), user.getRole().getRoleName());
        
        // Bước 7: Trả về response
        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtils.getExpirationTime())
                .user(userResponse)
                .build();
    }
    
    /**
     * Lấy thông tin user hiện tại đang đăng nhập
     * 
     * @return UserResponse của user hiện tại
     */
    public UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        if (authentication.getPrincipal() instanceof String) {
            // Principal là "anonymousUser" khi chưa đăng nhập
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return mapToUserResponse(userDetails.getUser());
    }
    
    /**
     * Chuyển đổi User entity sang UserResponse
     * 
     * Tách riêng method này để:
     * - Dễ maintain
     * - Có thể reuse
     * - Tuân theo Single Responsibility Principle
     */
    public UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .maNhanVien(user.getMaNhanVien())
                .hoTen(user.getHoTen())
                .email(user.getEmail())
                .soDienThoai(user.getSoDienThoai())
                .roleName(user.getRole().getRoleName())
                .roleDisplayName(user.getRole().getDisplayName())
                .tenQuay(user.getQuay() != null ? user.getQuay().getTenQuay() : null)
                .quayId(user.getQuay() != null ? user.getQuay().getId() : null)
                .trangThai(user.getTrangThai())
                .lanDangNhapCuoi(user.getLanDangNhapCuoi())
                .build();
    }
}
