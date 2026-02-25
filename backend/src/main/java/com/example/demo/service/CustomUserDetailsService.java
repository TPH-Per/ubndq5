package com.example.demo.service;

import com.example.demo.entity.Staff;
import com.example.demo.repository.StaffRepository;
import com.example.demo.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service to load Staff info from database
 * 
 * UserDetailsService is Spring Security interface, called when:
 * - Staff login (to verify password)
 * - JWT token is validated (to load staff info)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final StaffRepository staffRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Finding staff with code: {}", username);

        Staff staff = staffRepository.findByStaffCode(username)
                .orElseThrow(() -> {
                    log.warn("Staff not found with code: {}", username);
                    return new UsernameNotFoundException(
                            "Staff not found with code: " + username);
                });

        if (staff.getIsActive() == null || !staff.getIsActive()) {
            log.warn("Account {} is disabled", username);
            throw new UsernameNotFoundException("Account is disabled: " + username);
        }

        log.debug("Found staff: {} with role: {}", staff.getFullName(), staff.getRole().getRoleName());

        return new CustomUserDetails(staff);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(Integer id) {
        log.debug("Finding staff with ID: {}", id);

        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Staff not found with ID: {}", id);
                    return new UsernameNotFoundException("Staff not found with ID: " + id);
                });

        return new CustomUserDetails(staff);
    }
}
