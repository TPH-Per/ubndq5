package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.Role;
import com.example.demo.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller lấy danh sách Roles (Admin only)
 * 
 * GET /api/admin/roles
 */
@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('Admin')")
public class RoleController {
    
    private final RoleRepository roleRepository;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
        List<RoleResponse> roles = roleRepository.findAll().stream()
                .map(role -> RoleResponse.builder()
                        .id(role.getId())
                        .roleName(role.getRoleName())
                        .displayName(role.getDisplayName())
                        .build())
                .toList();
        
        return ResponseEntity.ok(
            ApiResponse.success(roles, "Lấy danh sách roles thành công")
        );
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RoleResponse {
        private Integer id;
        private String roleName;
        private String displayName;
    }
}
