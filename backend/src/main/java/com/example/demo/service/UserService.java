package com.example.demo.service;

import com.example.demo.dto.request.CreateUserRequest;
import com.example.demo.dto.request.UpdateProfileRequest;
import com.example.demo.dto.request.UpdateUserRequest;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.entity.Quay;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service quản lý Users (CRUD)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;
    
    /**
     * Lấy danh sách tất cả users
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy user theo ID
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return mapToUserResponse(user);
    }
    
    /**
     * Tạo user mới
     */
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Tạo user mới: {}", request.getMaNhanVien());
        
        // Kiểm tra mã nhân viên đã tồn tại
        if (userRepository.findByMaNhanVien(request.getMaNhanVien()).isPresent()) {
            throw new AppException(ErrorCode.USER_ALREADY_EXISTS, 
                    "Mã nhân viên " + request.getMaNhanVien() + " đã tồn tại");
        }
        
        // Kiểm tra email đã tồn tại
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS,
                    "Email " + request.getEmail() + " đã được sử dụng");
        }
        
        // Lấy Role từ database
        Role role = entityManager.find(Role.class, 
                request.getRoleId() != null ? request.getRoleId() : 2); // Default: NhanVien
        if (role == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Role không hợp lệ");
        }
        
        // Lấy Quay nếu có
        Quay quay = null;
        if (request.getQuayId() != null) {
            quay = entityManager.find(Quay.class, request.getQuayId());
        }
        
        // Tạo user mới
        User user = new User();
        user.setMaNhanVien(request.getMaNhanVien());
        user.setHoTen(request.getHoTen());
        user.setEmail(request.getEmail());
        user.setSoDienThoai(request.getSoDienThoai());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setQuay(quay);
        user.setTrangThai(true);
        user.setNgayTao(LocalDateTime.now());
        
        User savedUser = userRepository.save(user);
        log.info("Đã tạo user: {} với role: {}", savedUser.getHoTen(), role.getRoleName());
        
        return mapToUserResponse(savedUser);
    }
    
    /**
     * Cập nhật user
     */
    @Transactional
    public UserResponse updateUser(Integer id, UpdateUserRequest request) {
        log.info("Cập nhật user ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        // Cập nhật các field nếu có
        if (request.getHoTen() != null) {
            user.setHoTen(request.getHoTen());
        }
        
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            // Kiểm tra email mới có bị trùng không
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS,
                        "Email " + request.getEmail() + " đã được sử dụng");
            }
            user.setEmail(request.getEmail());
        }
        
        if (request.getSoDienThoai() != null) {
            user.setSoDienThoai(request.getSoDienThoai());
        }
        
        // Đổi password nếu có
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            log.info("Đã đổi password cho user: {}", user.getMaNhanVien());
        }
        
        // Đổi role nếu có
        if (request.getRoleId() != null) {
            Role role = entityManager.find(Role.class, request.getRoleId());
            if (role != null) {
                user.setRole(role);
            }
        }
        
        // Đổi quầy nếu có
        if (request.getQuayId() != null) {
            Quay quay = entityManager.find(Quay.class, request.getQuayId());
            user.setQuay(quay);
        }
        
        // Đổi trạng thái nếu có
        if (request.getTrangThai() != null) {
            user.setTrangThai(request.getTrangThai());
        }
        
        User savedUser = userRepository.save(user);
        log.info("Đã cập nhật user: {}", savedUser.getMaNhanVien());
        
        return mapToUserResponse(savedUser);
    }
    
    /**
     * Xóa user (soft delete - đổi trạng thái)
     */
    @Transactional
    public void deleteUser(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        user.setTrangThai(false);
        userRepository.save(user);
        log.info("Đã khóa user: {}", user.getMaNhanVien());
    }
    
    /**
     * Lấy thông tin user theo mã nhân viên (username)
     */
    @Transactional(readOnly = true)
    public UserResponse getUserByMaNhanVien(String maNhanVien) {
        User user = userRepository.findByMaNhanVien(maNhanVien)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return mapToUserResponse(user);
    }

    /**
     * Staff cập nhật thông tin cá nhân
     *
     * Staff chỉ được phép cập nhật:
     * - Họ tên, Email, Số điện thoại
     * - Mật khẩu (phải nhập mật khẩu cũ để xác thực)
     *
     * KHÔNG được đổi: Role, Quầy, Trạng thái, Mã nhân viên
     */
    @Transactional
    public UserResponse updateProfile(String maNhanVien, UpdateProfileRequest request) {
        log.info("Staff {} cập nhật thông tin cá nhân", maNhanVien);

        User user = userRepository.findByMaNhanVien(maNhanVien)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Cập nhật họ tên nếu có
        if (request.getHoTen() != null && !request.getHoTen().isEmpty()) {
            user.setHoTen(request.getHoTen());
        }

        // Cập nhật email nếu có và không trùng
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS,
                        "Email " + request.getEmail() + " đã được sử dụng");
            }
            user.setEmail(request.getEmail());
        }

        // Cập nhật số điện thoại nếu có
        if (request.getSoDienThoai() != null) {
            user.setSoDienThoai(request.getSoDienThoai());
        }

        // Đổi password nếu có - YÊU CẦU NHẬP MẬT KHẨU CŨ
        if (request.getNewPassword() != null && !request.getNewPassword().isEmpty()) {
            // Validate old password
            if (request.getOldPassword() == null || request.getOldPassword().isEmpty()) {
                throw new AppException(ErrorCode.VALIDATION_ERROR,
                        "Vui lòng nhập mật khẩu cũ để đổi mật khẩu");
            }

            if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
                throw new AppException(ErrorCode.INVALID_CREDENTIALS,
                        "Mật khẩu cũ không đúng");
            }

            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
            log.info("Staff {} đã đổi mật khẩu thành công", maNhanVien);
        }

        User savedUser = userRepository.save(user);
        log.info("Đã cập nhật profile cho: {}", savedUser.getMaNhanVien());

        return mapToUserResponse(savedUser);
    }

    /**
     * Map User entity sang UserResponse
     */
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .maNhanVien(user.getMaNhanVien())
                .hoTen(user.getHoTen())
                .email(user.getEmail())
                .soDienThoai(user.getSoDienThoai())
                .roleName(user.getRole() != null ? user.getRole().getRoleName() : null)
                .roleDisplayName(user.getRole() != null ? user.getRole().getDisplayName() : null)
                .tenQuay(user.getQuay() != null ? user.getQuay().getTenQuay() : null)
                .quayId(user.getQuay() != null ? user.getQuay().getId() : null)
                .trangThai(user.getTrangThai())
                .lanDangNhapCuoi(user.getLanDangNhapCuoi())
                .build();
    }
}
