package com.example.demo.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for login
 * 
 * Endpoint: POST /api/auth/login
 * 
 * Example request body:
 * {
 * "maNhanVien": "NV001",
 * "password": "123456"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Staff code is required")
    @JsonAlias({ "staffCode", "maNhanVien" }) // Support both field names
    private String staffCode;

    @NotBlank(message = "Password is required")
    private String password;
}
