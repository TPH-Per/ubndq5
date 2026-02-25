package com.example.demo.repository;

import com.example.demo.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Integer> {

    Optional<Staff> findByStaffCode(String staffCode);

    boolean existsByStaffCode(String staffCode);

    boolean existsByEmail(String email);

    List<Staff> findByIsActiveTrueOrderByStaffCode();

    @Query("SELECT s FROM Staff s WHERE s.isActive = true ORDER BY s.staffCode")
    List<Staff> findAllActive();

    @Query("SELECT s FROM Staff s WHERE s.counter.id = :counterId AND s.isActive = true")
    List<Staff> findByCounterId(Integer counterId);

    @Query("SELECT s FROM Staff s WHERE s.role.roleName = :roleName AND s.isActive = true")
    List<Staff> findByRoleName(String roleName);

    @Query("SELECT COUNT(s) FROM Staff s WHERE s.counter.id = :counterId")
    long countByCounterId(Integer counterId);
}
