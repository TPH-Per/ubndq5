package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.request.CreateUserRequest;
import com.example.demo.dto.request.UpdateUserRequest;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller quản lý Users (Admin only)
 * 
 * Base path: /api/admin/users
 * 
 * Tất cả endpoints đều yêu cầu role Admin
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('Admin')")
public class UserController {
    
    private final UserService userService;
    
    /**
     * Lấy danh sách tất cả users
     * 
     * GET /api/admin/users
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(
            ApiResponse.success(users, "Lấy danh sách users thành công")
        );
    }
    
    /**
     * Lấy user theo ID
     * 
     * GET /api/admin/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Integer id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(
            ApiResponse.success(user, "Lấy thông tin user thành công")
        );
    }
    
    /**
     * Tạo user mới
     * 
     * POST /api/admin/users
     */
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        
        log.info("Admin tạo user mới: {}", request.getMaNhanVien());
        UserResponse user = userService.createUser(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.success(user, "Tạo user thành công")
        );
    }
    
    /**
     * Cập nhật user
     * 
     * PUT /api/admin/users/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateUserRequest request) {
        
        log.info("Admin cập nhật user ID: {}", id);
        UserResponse user = userService.updateUser(id, request);
        
        return ResponseEntity.ok(
            ApiResponse.success(user, "Cập nhật user thành công")
        );
    }
    
    /**
     * Xóa user (soft delete)
     * 
     * DELETE /api/admin/users/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Integer id) {
        log.info("Admin xóa user ID: {}", id);
        userService.deleteUser(id);
        
        return ResponseEntity.ok(
            ApiResponse.success("Khóa user thành công")
        );
    }
    
    /**
     * Reset password cho user
     * 
     * POST /api/admin/users/{id}/reset-password
     */
    @PostMapping("/{id}/reset-password")
    public ResponseEntity<ApiResponse<UserResponse>> resetPassword(
            @PathVariable Integer id,
            @RequestBody ResetPasswordRequest request) {
        
        log.info("Admin reset password cho user ID: {}", id);
        UpdateUserRequest updateRequest = UpdateUserRequest.builder()
                .password(request.getNewPassword())
                .build();
        
        UserResponse user = userService.updateUser(id, updateRequest);
        
        return ResponseEntity.ok(
            ApiResponse.success(user, "Reset password thành công")
        );
    }
    
    /**
     * Inner class cho reset password request
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ResetPasswordRequest {
        private String newPassword;
    }
}
