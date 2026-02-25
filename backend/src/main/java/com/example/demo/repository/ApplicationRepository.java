package com.example.demo.repository;

import com.example.demo.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Integer> {

    Optional<Application> findByApplicationCode(String applicationCode);

    // Lấy applications theo phase
    List<Application> findByCurrentPhaseOrderByQueueNumberAsc(Integer phase);

    // Lấy applications theo citizen
    @Query("SELECT a FROM Application a WHERE a.citizen.citizenId = :citizenId ORDER BY a.createdAt DESC")
    List<Application> findByCitizenId(String citizenId);

    @Query("SELECT a FROM Application a WHERE a.procedure.id = :procedureId ORDER BY a.createdAt DESC")
    List<Application> findByProcedureId(Integer procedureId);

    // Đếm theo phase
    @Query("SELECT COUNT(a) FROM Application a WHERE a.currentPhase = :phase")
    Long countByPhase(Integer phase);

    @Query("SELECT COUNT(a) FROM Application a WHERE a.procedure.id = :procedureId")
    long countByProcedureId(Integer procedureId);

    // Lấy max queue number (vẫn giữ ở application vì queue_number vẫn ở đây)
    @Query("SELECT COALESCE(MAX(a.queueNumber), 0) FROM Application a WHERE a.queuePrefix = :prefix")
    Integer findMaxQueueNumber(String prefix);

    // Citizen API methods
    @Query("SELECT a FROM Application a WHERE a.citizen.citizenId = :citizenId ORDER BY a.createdAt DESC")
    List<Application> findByCitizenCitizenId(String citizenId);

    @Query("SELECT COUNT(a) FROM Application a WHERE CAST(a.createdAt AS LocalDate) = :date")
    int countByCreatedAtDate(java.time.LocalDate date);

    @Query("SELECT a FROM Application a WHERE CONCAT(a.queuePrefix, '-', LPAD(CAST(a.queueNumber AS string), 3, '0')) = :display")
    Optional<Application> findByQueueDisplay(String display);

    // Find by Zalo account ID
    @Query("SELECT a FROM Application a WHERE a.zaloAccount.id = :zaloAccountId ORDER BY a.createdAt DESC")
    List<Application> findByZaloAccountId(Integer zaloAccountId);
}
