package com.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO cho đăng nhập thành công
 * 
 * Endpoint: POST /api/auth/login
 * 
 * Ví dụ response:
 * {
 *   "token": "eyJhbGciOiJIUzI1NiJ9...",
 *   "tokenType": "Bearer",
 *   "expiresIn": 604800000,
 *   "user": { ... }
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    
    /**
     * JWT token
     */
    private String token;
    
    /**
     * Loại token (luôn là "Bearer")
     */
    @Builder.Default
    private String tokenType = "Bearer";
    
    /**
     * Thời gian hết hạn (milliseconds)
     */
    private long expiresIn;
    
    /**
     * Thông tin user đã đăng nhập
     */
    private UserResponse user;
}
