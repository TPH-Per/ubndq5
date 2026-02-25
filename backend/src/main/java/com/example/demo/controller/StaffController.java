package com.example.demo.controller;

import com.example.demo.dto.request.CreateStaffRequest;
import com.example.demo.dto.request.UpdateProfileRequest;
import com.example.demo.dto.response.StaffResponse;
import com.example.demo.service.StaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class StaffController {

    private final StaffService staffService;

    // ==================== ADMIN ENDPOINTS ====================

    /**
     * Create new staff account (Admin only)
     * POST /api/admin/staff
     */
    @PostMapping("/admin/staff")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<StaffResponse> createStaff(@Valid @RequestBody CreateStaffRequest request) {
        log.info("Admin creating staff: {}", request.getStaffCode());
        StaffResponse response = staffService.createStaff(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all staff (Admin only)
     * GET /api/admin/staff
     */
    @GetMapping("/admin/staff")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<List<StaffResponse>> getAllStaff() {
        List<StaffResponse> staff = staffService.getAllStaff();
        return ResponseEntity.ok(staff);
    }

    /**
     * Get staff by ID (Admin only)
     * GET /api/admin/staff/{id}
     */
    @GetMapping("/admin/staff/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<StaffResponse> getStaffById(@PathVariable Integer id) {
        StaffResponse staff = staffService.getStaffById(id);
        return ResponseEntity.ok(staff);
    }

    /**
     * Reset password for any staff (Admin only)
     * POST /api/admin/staff/{id}/reset-password
     */
    @PostMapping("/admin/staff/{id}/reset-password")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Map<String, String>> adminResetPassword(
            @PathVariable Integer id,
            @RequestBody Map<String, String> request) {
        String newPassword = request.get("newPassword");
        if (newPassword == null || newPassword.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "newPassword is required"));
        }

        staffService.resetPassword(id, newPassword);
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }

    /**
     * Toggle staff active status (Admin only)
     * POST /api/admin/staff/{id}/toggle-active
     */
    @PostMapping("/admin/staff/{id}/toggle-active")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<StaffResponse> toggleActive(@PathVariable Integer id) {
        StaffResponse staff = staffService.toggleActive(id);
        return ResponseEntity.ok(staff);
    }

    /**
     * Delete staff (Admin only)
     * DELETE /api/admin/staff/{id}
     */
    @DeleteMapping("/admin/staff/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Map<String, String>> deleteStaff(@PathVariable Integer id) {
        staffService.deleteStaff(id);
        return ResponseEntity.ok(Map.of("message", "Staff deleted successfully"));
    }

    // ==================== STAFF ENDPOINTS ====================

    /**
     * Get current staff profile
     * GET /api/staff/profile
     */
    @GetMapping("/staff/profile")
    public ResponseEntity<StaffResponse> getProfile() {
        // This uses SecurityContext to get current user
        StaffResponse staff = staffService.getStaffById(getCurrentStaffId());
        return ResponseEntity.ok(staff);
    }

    /**
     * Update current staff profile
     * PUT /api/staff/profile
     */
    @PutMapping("/staff/profile")
    public ResponseEntity<StaffResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        StaffResponse staff = staffService.updateProfile(request);
        return ResponseEntity.ok(staff);
    }

    /**
     * Change password for current staff
     * POST /api/staff/change-password
     */
    @PostMapping("/staff/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@RequestBody Map<String, String> request) {
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");

        if (oldPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "oldPassword and newPassword are required"));
        }

        if (newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "New password must be at least 6 characters"));
        }

        staffService.changePassword(oldPassword, newPassword);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    /**
     * Get active staff list (for dropdowns, etc)
     * GET /api/staff/list
     */
    @GetMapping("/staff/list")
    public ResponseEntity<List<StaffResponse>> getActiveStaff() {
        List<StaffResponse> staff = staffService.getAllActiveStaff();
        return ResponseEntity.ok(staff);
    }

    // ==================== HELPER METHODS ====================

    private Integer getCurrentStaffId() {
        var auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof com.example.demo.security.CustomUserDetails) {
            return ((com.example.demo.security.CustomUserDetails) auth.getPrincipal()).getId();
        }
        throw new RuntimeException("No authenticated staff found");
    }
}
