package com.example.demo.repository;

import com.example.demo.entity.Quay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuayRepository extends JpaRepository<Quay, Integer> {
    
    // Lấy danh sách quầy đang hoạt động
    List<Quay> findByTrangThaiTrue();
    
    // Tìm quầy theo mã quầy
    java.util.Optional<Quay> findByMaQuay(String maQuay);
}
