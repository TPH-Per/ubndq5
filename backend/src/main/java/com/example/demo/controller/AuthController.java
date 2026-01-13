package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.request.LoginRequest;
import com.example.demo.dto.response.LoginResponse;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý Authentication (đăng nhập, đăng xuất)
 * 
 * Base path: /api/auth
 * 
 * Endpoints:
 * - POST /api/auth/login  → Đăng nhập
 * - POST /api/auth/logout → Đăng xuất (optional, vì JWT stateless)
 * - GET  /api/auth/me     → Lấy thông tin user hiện tại
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * Đăng nhập
     * 
     * POST /api/auth/login
     * Body: { "maNhanVien": "NV001", "password": "123456" }
     * 
     * @param request LoginRequest chứa maNhanVien và password
     * @return ApiResponse chứa LoginResponse (JWT token + user info)
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid 
            @RequestBody LoginRequest request) {
        
        log.info("Login request cho user: {}", request.getMaNhanVien());
        
        LoginResponse response = authService.login(request);
        
        return ResponseEntity.ok(
            ApiResponse.success(response, "Đăng nhập thành công")
        );
    }
    
    /**
     * Đăng xuất
     * 
     * POST /api/auth/logout
     * 
     * Với JWT stateless, logout chỉ cần xóa token ở client.
     * Server không cần làm gì vì không lưu session.
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        if (userDetails != null) {
            log.info("User {} đã đăng xuất", userDetails.getUsername());
        }
        
        return ResponseEntity.ok(
            ApiResponse.success("Đăng xuất thành công")
        );
    }
    
    /**
     * Lấy thông tin user hiện tại
     * 
     * GET /api/auth/me
     * Header: Authorization: Bearer <token>
     * 
     * @return ApiResponse chứa UserResponse
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        UserResponse currentUser = authService.getCurrentUser();
        
        return ResponseEntity.ok(
            ApiResponse.success(currentUser, "Lấy thông tin thành công")
        );
    }
    
    /**
     * Kiểm tra token còn valid không
     * 
     * GET /api/auth/validate
     * Header: Authorization: Bearer <token>
     */
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<TokenValidationResponse>> validateToken(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        TokenValidationResponse response = TokenValidationResponse.builder()
                .valid(userDetails != null)
                .username(userDetails != null ? userDetails.getUsername() : null)
                .role(userDetails != null ? userDetails.getRoleName() : null)
                .build();
        
        return ResponseEntity.ok(
            ApiResponse.success(response, "Token hợp lệ")
        );
    }
    
    /**
     * Inner class cho validate token response
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TokenValidationResponse {
        private boolean valid;
        private String username;
        private String role;
    }
}
