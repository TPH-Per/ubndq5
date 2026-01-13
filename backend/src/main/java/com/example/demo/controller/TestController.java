package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.request.CreateUserRequest;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * Controller tạm để test và setup ban đầu
 * XÓA SAU KHI PRODUCTION
 */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class TestController {
    
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final UserService userService;
    
    @GetMapping("/hash/{password}")
    public String hashPassword(@PathVariable String password) {
        return encoder.encode(password);
    }
    
    @GetMapping("/verify")
    public String verifyPassword(
            @RequestParam String password,
            @RequestParam String hash) {
        boolean matches = encoder.matches(password, hash);
        return "Password: " + password + "\nHash: " + hash + "\nMatches: " + matches;
    }
    
    /**
     * Tạo Admin user ban đầu
     * CHỈ DÙNG 1 LẦN ĐỂ SETUP HỆ THỐNG
     * 
     * POST /api/public/init-admin
     * Body: {"maNhanVien":"ADMIN001","hoTen":"Admin","email":"admin@test.com","password":"123456"}
     */
    @PostMapping("/init-admin")
    public ResponseEntity<ApiResponse<UserResponse>> initAdmin(@RequestBody CreateUserRequest request) {
        // Force roleId = 1 (Admin)
        request.setRoleId(1);
        
        UserResponse user = userService.createUser(request);
        
        return ResponseEntity.ok(
            ApiResponse.success(user, "Tạo Admin user thành công")
        );
    }
    
    /**
     * Tạo Staff user ban đầu
     * 
     * POST /api/public/init-staff
     */
    @PostMapping("/init-staff")
    public ResponseEntity<ApiResponse<UserResponse>> initStaff(@RequestBody CreateUserRequest request) {
        // Force roleId = 2 (NhanVien)
        request.setRoleId(2);
        
        UserResponse user = userService.createUser(request);
        
        return ResponseEntity.ok(
            ApiResponse.success(user, "Tạo Staff user thành công")
        );
    }
}
