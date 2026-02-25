package com.example.demo.repository;

import com.example.demo.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Integer> {

    @Query("SELECT r FROM Report r WHERE r.citizen.citizenId = :citizenId ORDER BY r.createdAt DESC")
    List<Report> findByCitizenId(String citizenId); // citizenId is now String (CCCD)

    @Query("SELECT r FROM Report r WHERE r.application.id = :applicationId ORDER BY r.createdAt DESC")
    List<Report> findByApplicationId(Integer applicationId);

    @Query("SELECT r FROM Report r WHERE r.status = :status ORDER BY r.createdAt DESC")
    List<Report> findByStatus(Integer status);

    @Query("SELECT r FROM Report r WHERE r.status < 2 ORDER BY r.createdAt DESC")
    List<Report> findUnresolved();
}
