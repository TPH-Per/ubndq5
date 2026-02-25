package com.example.demo.service;

import com.example.demo.dto.request.ChangePasswordRequest;
import com.example.demo.dto.request.LoginRequest;
import com.example.demo.dto.request.UpdateProfileRequest;
import com.example.demo.dto.response.LoginResponse;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.entity.Staff;
import com.example.demo.repository.StaffRepository;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Login with staff code and password
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for staff: {}", request.getStaffCode());

        // Authenticate using Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getStaffCode(),
                        request.getPassword()));

        // Set authentication to SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Get staff info from authentication
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Staff staff = userDetails.getStaff();

        // Update last login time
        staff.setUpdatedAt(LocalDateTime.now());
        staffRepository.save(staff);

        // Generate JWT token
        String accessToken = jwtUtils.generateToken(authentication);

        // Map to response
        UserResponse userResponse = mapToUserResponse(staff);

        log.info("Login successful for staff: {}", staff.getStaffCode());

        return LoginResponse.builder()
                .token(accessToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtils.getExpirationTime())
                .user(userResponse)
                .build();
    }

    /**
     * Get current logged in staff info
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentStaff() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No authenticated staff found");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return mapToUserResponse(userDetails.getStaff());
    }

    /**
     * Update current user's profile
     */
    @Transactional
    public UserResponse updateProfile(UpdateProfileRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No authenticated staff found");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Staff staff = staffRepository.findById(userDetails.getStaff().getId())
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        // Update fields if provided
        if (request.getHoTen() != null && !request.getHoTen().isBlank()) {
            staff.setFullName(request.getHoTen());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            staff.setEmail(request.getEmail());
        }
        if (request.getSoDienThoai() != null && !request.getSoDienThoai().isBlank()) {
            staff.setPhone(request.getSoDienThoai());
        }

        staff.setUpdatedAt(LocalDateTime.now());
        staffRepository.save(staff);

        log.info("Profile updated for staff: {}", staff.getStaffCode());
        return mapToUserResponse(staff);
    }

    /**
     * Change current user's password
     */
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No authenticated staff found");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Staff staff = staffRepository.findById(userDetails.getStaff().getId())
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), staff.getPasswordHash())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không đúng");
        }

        // Update password
        staff.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        staff.setUpdatedAt(LocalDateTime.now());
        staffRepository.save(staff);

        log.info("Password changed for staff: {}", staff.getStaffCode());
    }

    /**
     * Map Staff entity to UserResponse DTO (Vietnamese field names)
     */
    public UserResponse mapToUserResponse(Staff staff) {
        return UserResponse.builder()
                .id(staff.getId())
                .maNhanVien(staff.getStaffCode())
                .hoTen(staff.getFullName())
                .email(staff.getEmail())
                .soDienThoai(staff.getPhone())
                .roleId(staff.getRole() != null ? staff.getRole().getId() : null)
                .roleName(staff.getRole() != null ? staff.getRole().getRoleName() : null)
                .roleDisplayName(staff.getRole() != null ? staff.getRole().getDisplayName() : null)
                .quayId(staff.getCounter() != null ? staff.getCounter().getId() : null)
                .tenQuay(staff.getCounter() != null ? staff.getCounter().getCounterName() : null)
                .trangThai(staff.getIsActive())
                .ngayTao(staff.getCreatedAt())
                .build();
    }
}
