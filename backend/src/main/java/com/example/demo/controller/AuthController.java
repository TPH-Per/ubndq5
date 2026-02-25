package com.example.demo.controller;

import com.example.demo.dto.request.LoginRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.LoginResponse;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.entity.Staff;
import com.example.demo.repository.StaffRepository;
import com.example.demo.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;

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
     * Reset password (for development only!)
     * POST /api/auth/reset-password
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> request) {
        String staffCode = request.get("staffCode");
        String newPassword = request.get("newPassword");

        if (staffCode == null || newPassword == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "staffCode and newPassword are required"));
        }

        Staff staff = staffRepository.findByStaffCode(staffCode)
                .orElse(null);

        if (staff == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Staff not found: " + staffCode));
        }

        String hashedPassword = passwordEncoder.encode(newPassword);
        staff.setPasswordHash(hashedPassword);
        staffRepository.save(staff);

        log.info("Password reset for staff: {}", staffCode);
        return ResponseEntity.ok(Map.of("message", "Password reset successfully for " + staffCode));
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
}
