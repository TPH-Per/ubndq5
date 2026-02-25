package com.example.demo.service;

import com.example.demo.dto.request.CreateStaffRequest;
import com.example.demo.dto.request.UpdateProfileRequest;
import com.example.demo.dto.response.StaffResponse;
import com.example.demo.entity.Counter;
import com.example.demo.entity.Role;
import com.example.demo.entity.Staff;
import com.example.demo.repository.CounterRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.StaffRepository;
import com.example.demo.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaffService {

    private final StaffRepository staffRepository;
    private final RoleRepository roleRepository;
    private final CounterRepository counterRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Create new staff account
     */
    @Transactional
    public StaffResponse createStaff(CreateStaffRequest request) {
        log.info("Creating staff with code: {}", request.getStaffCode());

        // Check if staff code already exists
        if (staffRepository.existsByStaffCode(request.getStaffCode())) {
            throw new RuntimeException("Staff code already exists: " + request.getStaffCode());
        }

        // Check if email already exists
        if (request.getEmail() != null && staffRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }

        // Get role
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + request.getRoleId()));

        // Get counter if provided
        Counter counter = null;
        if (request.getCounterId() != null) {
            counter = counterRepository.findById(request.getCounterId())
                    .orElseThrow(() -> new RuntimeException("Counter not found with id: " + request.getCounterId()));
        }

        // Hash password
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // Create staff
        Staff staff = Staff.builder()
                .staffCode(request.getStaffCode())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(hashedPassword)
                .role(role)
                .counter(counter)
                .isActive(true)
                .build();

        staff = staffRepository.save(staff);
        log.info("Staff created successfully: {}", staff.getStaffCode());

        return mapToStaffResponse(staff);
    }

    /**
     * Get all staff
     */
    @Transactional(readOnly = true)
    public List<StaffResponse> getAllStaff() {
        return staffRepository.findAll().stream()
                .map(this::mapToStaffResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all active staff
     */
    @Transactional(readOnly = true)
    public List<StaffResponse> getAllActiveStaff() {
        return staffRepository.findByIsActiveTrueOrderByStaffCode().stream()
                .map(this::mapToStaffResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get staff by ID
     */
    @Transactional(readOnly = true)
    public StaffResponse getStaffById(Integer id) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Staff not found with id: " + id));
        return mapToStaffResponse(staff);
    }

    /**
     * Get staff by staff code
     */
    @Transactional(readOnly = true)
    public StaffResponse getStaffByCode(String staffCode) {
        Staff staff = staffRepository.findByStaffCode(staffCode)
                .orElseThrow(() -> new RuntimeException("Staff not found with code: " + staffCode));
        return mapToStaffResponse(staff);
    }

    /**
     * Change password for current logged in staff
     */
    @Transactional
    public void changePassword(String oldPassword, String newPassword) {
        // Get current staff from security context
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();

        Staff staff = staffRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        // Verify old password
        if (!passwordEncoder.matches(oldPassword, staff.getPasswordHash())) {
            throw new RuntimeException("Old password is incorrect");
        }

        // Hash new password and save
        String hashedPassword = passwordEncoder.encode(newPassword);
        staff.setPasswordHash(hashedPassword);
        staffRepository.save(staff);

        log.info("Password changed for staff: {}", staff.getStaffCode());
    }

    /**
     * Admin reset password for any staff
     */
    @Transactional
    public void resetPassword(Integer staffId, String newPassword) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found with id: " + staffId));

        String hashedPassword = passwordEncoder.encode(newPassword);
        staff.setPasswordHash(hashedPassword);
        staffRepository.save(staff);

        log.info("Password reset for staff: {} by admin", staff.getStaffCode());
    }

    /**
     * Update current staff profile
     */
    @Transactional
    public StaffResponse updateProfile(UpdateProfileRequest request) {
        // Get current staff from security context
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();

        Staff staff = staffRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        // Update fields if provided
        if (request.getFullName() != null) {
            staff.setFullName(request.getFullName());
        }
        if (request.getEmail() != null) {
            // Check email unique
            if (!request.getEmail().equals(staff.getEmail()) &&
                    staffRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email already exists: " + request.getEmail());
            }
            staff.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            staff.setPhone(request.getPhone());
        }

        // If password change is requested
        if (request.getNewPassword() != null && request.getOldPassword() != null) {
            if (!passwordEncoder.matches(request.getOldPassword(), staff.getPasswordHash())) {
                throw new RuntimeException("Old password is incorrect");
            }
            staff.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        }

        staff = staffRepository.save(staff);
        log.info("Profile updated for staff: {}", staff.getStaffCode());

        return mapToStaffResponse(staff);
    }

    /**
     * Toggle staff active status
     */
    @Transactional
    public StaffResponse toggleActive(Integer staffId) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found with id: " + staffId));

        staff.setIsActive(!staff.getIsActive());
        staff = staffRepository.save(staff);

        log.info("Staff {} is now {}", staff.getStaffCode(), staff.getIsActive() ? "active" : "inactive");
        return mapToStaffResponse(staff);
    }

    /**
     * Delete staff (soft delete - set inactive)
     */
    @Transactional
    public void deleteStaff(Integer staffId) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found with id: " + staffId));

        staff.setIsActive(false);
        staffRepository.save(staff);

        log.info("Staff deleted (deactivated): {}", staff.getStaffCode());
    }

    /**
     * Map Staff entity to StaffResponse DTO
     */
    private StaffResponse mapToStaffResponse(Staff staff) {
        return StaffResponse.builder()
                .id(staff.getId())
                .staffCode(staff.getStaffCode())
                .fullName(staff.getFullName())
                .email(staff.getEmail())
                .phone(staff.getPhone())
                .roleId(staff.getRole() != null ? staff.getRole().getId() : null)
                .roleName(staff.getRole() != null ? staff.getRole().getRoleName() : null)
                .roleDisplayName(staff.getRole() != null ? staff.getRole().getDisplayName() : null)
                .counterId(staff.getCounter() != null ? staff.getCounter().getId() : null)
                .counterName(staff.getCounter() != null ? staff.getCounter().getCounterName() : null)
                .isActive(staff.getIsActive())
                .createdAt(staff.getCreatedAt())
                .build();
    }
}
