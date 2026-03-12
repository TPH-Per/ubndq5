package com.example.demo.config;

import com.example.demo.entity.Role;
import com.example.demo.entity.Staff;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Khởi tạo dữ liệu hệ thống mỗi khi backend startup.
 * Đảm bảo luôn có tài khoản Admin để quản trị.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final StaffRepository staffRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Checking system data initialization...");

        // 1. Đảm bảo Role Admin tồn tại
        Role adminRole = roleRepository.findByRoleName("Admin")
                .orElseGet(() -> {
                    log.info("Role 'Admin' not found. Creating...");
                    Role role = Role.builder()
                            .roleName("Admin")
                            .displayName("Quản trị viên")
                            .description("Quyền quản trị cao nhất hệ thống")
                            .build();
                    return roleRepository.save(role);
                });

        // 2. Đảm bảo Role Staff tồn tại
        roleRepository.findByRoleName("Staff")
                .orElseGet(() -> {
                    log.info("Role 'Staff' not found. Creating...");
                    Role role = Role.builder()
                            .roleName("Staff")
                            .displayName("Nhân viên")
                            .description("Quyền nhân viên nghiệp vụ")
                            .build();
                    return roleRepository.save(role);
                });

        // 3. Khởi tạo tài khoản ADMIN hệ thống
        final String ADMIN_CODE = "ADMIN";
        final String DEFAULT_PASSWORD = "123456";

        Staff existingAdmin = staffRepository.findByStaffCode(ADMIN_CODE).orElse(null);

        if (existingAdmin == null) {
            // Tài khoản chưa tồn tại — tạo mới với BCrypt hash từ backend
            String hashedPassword = passwordEncoder.encode(DEFAULT_PASSWORD);

            Staff admin = Staff.builder()
                    .staffCode(ADMIN_CODE)
                    .fullName("System Administrator")
                    .email("admin@ubndq5.gov.vn")
                    .passwordHash(hashedPassword)
                    .role(adminRole)
                    .isActive(true)
                    .build();

            staffRepository.save(admin);
            log.info("====================================================");
            log.info("ADMIN ACCOUNT CREATED");
            log.info("Username: {}", ADMIN_CODE);
            log.info("Password: {}", DEFAULT_PASSWORD);
            log.info("====================================================");
        } else {
            // Tài khoản đã tồn tại — đảm bảo active và hash đúng
            boolean needsUpdate = false;

            // Kiểm tra password có khớp không (phòng trường hợp hash cũ/hardcoded)
            if (!passwordEncoder.matches(DEFAULT_PASSWORD, existingAdmin.getPasswordHash())) {
                log.warn("ADMIN password hash mismatch (may have been set manually). Resetting to default...");
                existingAdmin.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
                needsUpdate = true;
            }

            if (!existingAdmin.getIsActive()) {
                existingAdmin.setIsActive(true);
                needsUpdate = true;
                log.info("Re-activated ADMIN account.");
            }

            if (existingAdmin.getRole() == null || !adminRole.getId().equals(existingAdmin.getRole().getId())) {
                existingAdmin.setRole(adminRole);
                needsUpdate = true;
            }

            if (needsUpdate) {
                staffRepository.save(existingAdmin);
                log.info("ADMIN account updated.");
            } else {
                log.info("ADMIN account OK.");
            }
        }
    }
}
