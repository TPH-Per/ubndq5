package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service để load thông tin User từ database
 * 
 * UserDetailsService là interface của Spring Security, được gọi khi:
 * - User đăng nhập (để verify password)
 * - JWT token được validate (để load user info)
 * 
 * Spring Security sẽ tự động tìm và sử dụng bean này
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Đang tìm user với mã nhân viên: {}", username);
        
        User user = userRepository.findByMaNhanVienWithRole(username)
                .orElseThrow(() -> {
                    log.warn("Không tìm thấy user với mã nhân viên: {}", username);
                    return new UsernameNotFoundException(
                            "Không tìm thấy người dùng với mã nhân viên: " + username
                    );
                });
        
        if (user.getTrangThai() == null || !user.getTrangThai()) {
            log.warn("Tài khoản {} đã bị khóa", username);
            throw new UsernameNotFoundException("Tài khoản đã bị khóa: " + username);
        }
        
        log.debug("Đã tìm thấy user: {} với role: {}", user.getHoTen(), user.getRole().getRoleName());
        
        return new CustomUserDetails(user);
    }
    
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Integer id) {
        log.debug("Đang tìm user với ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Không tìm thấy user với ID: {}", id);
                    return new UsernameNotFoundException("Không tìm thấy người dùng với ID: " + id);
                });
        
        return new CustomUserDetails(user);
    }
}
