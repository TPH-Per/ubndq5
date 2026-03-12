package com.example.demo.controller;

import com.example.demo.dto.request.LoginRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.LoginResponse;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * Login endpoint
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request for staff: {}", request.getStaffCode());
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Đăng nhập thành công"));
    }

    /**
     * Get current logged in staff info
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentStaff() {
        UserResponse response = authService.getCurrentStaff();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Validate token
     * GET /api/auth/validate
     */
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> validateToken() {
        // If request reaches here, token is valid (JWT filter already validated)
        return ResponseEntity.ok(ApiResponse.success(Map.of("valid", true)));
    }

    /**
     * Health check endpoint
     * GET /api/auth/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "OK", "message", "Auth service is running"));
    }

    /**
     * Update current user's profile
     * PUT /api/auth/profile
     */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @Valid @RequestBody com.example.demo.dto.request.UpdateProfileRequest request) {
        log.info("Update profile request");
        UserResponse response = authService.updateProfile(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật thông tin thành công"));
    }

    /**
     * Change current user's password
     * POST /api/auth/change-password
     */
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Map<String, String>>> changePassword(
            @Valid @RequestBody com.example.demo.dto.request.ChangePasswordRequest request) {
        log.info("Change password request");
        authService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("message", "Đổi mật khẩu thành công"),
                "Đổi mật khẩu thành công"));
    }

    /**
     * Logout endpoint
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        // Since we are using stateless JWT, we don't need to do much on the backend.
        // Frontend will remove the token.
        // Optionally, we could blacklist the token here if we had a token blacklist.
        log.info("Logout request received");
        return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công", "Đăng xuất thành công"));
    }
}
