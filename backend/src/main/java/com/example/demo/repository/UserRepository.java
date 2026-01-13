package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository cho Entity User
 * 
 * JpaRepository<User, Integer>:
 * - User: Entity class
 * - Integer: Kiểu dữ liệu của Primary Key (id)
 * 
 * Spring Data JPA sẽ tự động implement các method CRUD cơ bản:
 * - findAll(), findById(), save(), delete(), count()...
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    
    /**
     * Tìm User theo mã nhân viên (dùng cho đăng nhập)
     * 
     * Spring Data JPA sẽ tự động tạo query từ tên method:
     * - findBy + MaNhanVien → SELECT * FROM users WHERE manhanvien = ?
     * 
     * @param maNhanVien Mã nhân viên cần tìm
     * @return Optional<User> - có thể có hoặc không có kết quả
     */
    Optional<User> findByMaNhanVien(String maNhanVien);
    
    /**
     * Tìm User theo email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Kiểm tra mã nhân viên đã tồn tại chưa
     * 
     * existsBy + FieldName → trả về boolean
     */
    boolean existsByMaNhanVien(String maNhanVien);
    
    /**
     * Kiểm tra email đã tồn tại chưa
     */
    boolean existsByEmail(String email);
    
    /**
     * Tìm User với Role (eager load để tránh N+1 problem)
     * 
     * @Query: Tự viết JPQL query khi cần logic phức tạp
     * JOIN FETCH: Load luôn relationship Role thay vì lazy load
     */
    @Query("SELECT u FROM User u JOIN FETCH u.role WHERE u.maNhanVien = :maNhanVien")
    Optional<User> findByMaNhanVienWithRole(@Param("maNhanVien") String maNhanVien);
}
