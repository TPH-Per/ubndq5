package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.request.UpdateProfileRequest;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Controller quản lý Profile cá nhân
 *
 * Base path: /api/profile
 *
 * Cho phép mọi user đã đăng nhập (Admin & Staff)
 * xem và cập nhật thông tin của CHÍNH MÌNH
 */
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {

    private final UserService userService;

    /**
     * Lấy thông tin profile của user đang đăng nhập
     *
     * GET /api/profile
     */
    @GetMapping
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile() {
        String maNhanVien = getCurrentUsername();
        log.info("User {} lấy thông tin profile", maNhanVien);

        UserResponse user = userService.getUserByMaNhanVien(maNhanVien);
        return ResponseEntity.ok(
            ApiResponse.success(user, "Lấy thông tin profile thành công")
        );
    }

    /**
     * Cập nhật thông tin profile của user đang đăng nhập
     *
     * PUT /api/profile
     *
     * Staff chỉ có thể cập nhật:
     * - Họ tên
     * - Email
     * - Số điện thoại
     * - Mật khẩu (yêu cầu nhập mật khẩu cũ)
     *
     * KHÔNG được đổi: Role, Quầy, Trạng thái, Mã nhân viên
     */
    @PutMapping
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(
            @Valid @RequestBody UpdateProfileRequest request) {

        String maNhanVien = getCurrentUsername();
        log.info("User {} cập nhật thông tin profile", maNhanVien);

        UserResponse user = userService.updateProfile(maNhanVien, request);

        return ResponseEntity.ok(
            ApiResponse.success(user, "Cập nhật thông tin thành công")
        );
    }

    /**
     * Lấy username (mã nhân viên) của user đang đăng nhập từ SecurityContext
     */
    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) auth.getPrincipal()).getUsername();
        }
        throw new RuntimeException("Không thể xác định user hiện tại");
    }
}

