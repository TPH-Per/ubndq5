package com.example.demo.controller;

import com.example.demo.dto.request.CreateUserRequest;
import com.example.demo.dto.request.UpdateUserRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.entity.Counter;
import com.example.demo.entity.Role;
import com.example.demo.entity.Staff;
import com.example.demo.repository.CounterRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.StaffRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * User Management Controller cho Admin
 * Quản lý tài khoản nhân viên
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('Admin')")
public class UserController {

    private final StaffRepository staffRepository;
    private final RoleRepository roleRepository;
    private final CounterRepository counterRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Lấy danh sách users (phân trang)
     * GET /api/admin/users?page=0&size=50
     */
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        log.info("Getting users page={} size={}", page, size);

        if (size > 200) size = 200;
        if (page < 0)   page = 0;

        Page<UserResponse> users = staffRepository.findAll(
                PageRequest.of(page, size, Sort.by("staffCode").ascending()))
                .map(this::mapToUserResponse);

        return ResponseEntity.ok(ApiResponse.success(users));
    }

    /**
     * Lấy user theo ID
     * GET /api/admin/users/{id}
     */
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Integer id) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + id));

        return ResponseEntity.ok(ApiResponse.success(mapToUserResponse(staff)));
    }

    /**
     * Tạo user mới
     * POST /api/admin/users
     */
    @PostMapping
    @Transactional
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("Creating user: {}", request.getMaNhanVien());

        // Check if staff code exists
        if (staffRepository.existsByStaffCode(request.getMaNhanVien())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("DUPLICATE_CODE", "Mã nhân viên đã tồn tại: " + request.getMaNhanVien()));
        }

        // Check if email exists
        if (request.getEmail() != null && staffRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("DUPLICATE_EMAIL", "Email đã tồn tại: " + request.getEmail()));
        }

        // Get role
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò với ID: " + request.getRoleId()));

        // Get counter if provided
        Counter counter = null;
        if (request.getQuayId() != null) {
            counter = counterRepository.findById(request.getQuayId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy quầy với ID: " + request.getQuayId()));
        }

        // Create staff
        Staff staff = Staff.builder()
                .staffCode(request.getMaNhanVien())
                .fullName(request.getHoTen())
                .email(request.getEmail())
                .phone(request.getSoDienThoai())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .counter(counter)
                .isActive(true)
                .build();

        staff = staffRepository.save(staff);
        log.info("User created: {}", staff.getStaffCode());

        return ResponseEntity.ok(ApiResponse.success(mapToUserResponse(staff), "Tạo tài khoản thành công"));
    }

    /**
     * Cập nhật user
     * PUT /api/admin/users/{id}
     */
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Integer id,
            @RequestBody UpdateUserRequest request) {

        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + id));

        log.info("Updating user: {}", staff.getStaffCode());

        // Update fields if provided
        if (request.getHoTen() != null) {
            staff.setFullName(request.getHoTen());
        }
        if (request.getEmail() != null) {
            if (!request.getEmail().equals(staff.getEmail()) && staffRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("DUPLICATE_EMAIL", "Email đã tồn tại"));
            }
            staff.setEmail(request.getEmail());
        }
        if (request.getSoDienThoai() != null) {
            staff.setPhone(request.getSoDienThoai());
        }
        if (request.getRoleId() != null) {
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò"));
            staff.setRole(role);
        }
        if (request.getQuayId() != null) {
            Counter counter = counterRepository.findById(request.getQuayId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy quầy"));
            staff.setCounter(counter);
        }
        if (request.getTrangThai() != null) {
            staff.setIsActive(request.getTrangThai());
        }
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            staff.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        staff = staffRepository.save(staff);
        log.info("User updated: {}", staff.getStaffCode());

        return ResponseEntity.ok(ApiResponse.success(mapToUserResponse(staff), "Cập nhật thành công"));
    }

    /**
     * Xóa user (soft delete - đặt inactive)
     * DELETE /api/admin/users/{id}
     */
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Integer id) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + id));

        log.info("Deleting (deactivating) user: {}", staff.getStaffCode());

        staff.setIsActive(false);
        staffRepository.save(staff);

        return ResponseEntity.ok(ApiResponse.success(null, "Đã khóa tài khoản"));
    }

    /**
     * Reset password cho user
     * POST /api/admin/users/{id}/reset-password
     */
    @PostMapping("/{id}/reset-password")
    @Transactional
    public ResponseEntity<ApiResponse<UserResponse>> resetPassword(
            @PathVariable Integer id,
            @RequestBody Map<String, String> request) {

        String newPassword = request.get("newPassword");
        if (newPassword == null || newPassword.length() < 6) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("INVALID_PASSWORD", "Mật khẩu phải có ít nhất 6 ký tự"));
        }

        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + id));

        log.info("Resetting password for: {}", staff.getStaffCode());

        staff.setPasswordHash(passwordEncoder.encode(newPassword));
        staffRepository.save(staff);

        return ResponseEntity.ok(ApiResponse.success(mapToUserResponse(staff), "Đã đặt lại mật khẩu"));
    }

    /**
     * Map Staff entity to UserResponse DTO
     */
    private UserResponse mapToUserResponse(Staff staff) {
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
