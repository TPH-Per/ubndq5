package com.example.demo.repository;

import com.example.demo.entity.ChuyenMon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChuyenMonRepository extends JpaRepository<ChuyenMon, Integer> {
    
    // Lấy danh sách chuyên môn đang hoạt động
    List<ChuyenMon> findByTrangThaiTrue();
    
    // Tìm chuyên môn theo mã (dùng để check trùng)
    java.util.Optional<ChuyenMon> findByMaChuyenMon(String maChuyenMon);
}
