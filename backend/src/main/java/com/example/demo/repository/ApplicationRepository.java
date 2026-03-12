package com.example.demo.repository;

import com.example.demo.entity.Application;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Integer> {

    Optional<Application> findByApplicationCode(String applicationCode);

    // Lấy applications theo phase
    List<Application> findByCurrentPhaseOrderByQueueNumberAsc(Integer phase);

    // Lấy applications theo CCCD (inline field, không FK)
    List<Application> findByCitizenCccdOrderByCreatedAtDesc(String citizenCccd);

    // Alias cho CitizenController
    default List<Application> findByCitizenCccd(String cccd) {
        return findByCitizenCccdOrderByCreatedAtDesc(cccd);
    }

    // Tìm hồ sơ theo tài khoản Zalo (quan hệ chính thay thế CCCD)
    List<Application> findByZaloAccount_ZaloIdOrderByCreatedAtDesc(String zaloId);

    @Query("SELECT a FROM Application a WHERE a.procedure.id = :procedureId ORDER BY a.createdAt DESC")
    List<Application> findByProcedureId(Integer procedureId);

    // Đếm theo phase
    @Query("SELECT COUNT(a) FROM Application a WHERE a.currentPhase = :phase")
    Long countByPhase(Integer phase);

    @Query("SELECT COUNT(a) FROM Application a WHERE a.procedure.id = :procedureId")
    long countByProcedureId(Integer procedureId);

    // Lấy max queue number
    @Query("SELECT COALESCE(MAX(a.queueNumber), 0) FROM Application a WHERE a.queuePrefix = :prefix")
    Integer findMaxQueueNumber(String prefix);

    // Đếm số hồ sơ tạo trong ngày hôm nay (để tạo applicationCode theo thứ tự)
    @Query("SELECT COUNT(a) FROM Application a WHERE CAST(a.createdAt AS LocalDate) = :date")
    int countByCreatedAtDate(java.time.LocalDate date);

    // Tìm application theo queue display (vd: "TT001")
    @Query("SELECT a FROM Application a WHERE CONCAT(a.queuePrefix, LPAD(CAST(a.queueNumber AS string), 3, '0')) = :display")
    Optional<Application> findByQueueDisplay(String display);

    // Tìm theo Zalo account ID
    @Query("SELECT a FROM Application a WHERE a.zaloAccount.id = :zaloAccountId ORDER BY a.createdAt DESC")
    List<Application> findByZaloAccountId(Integer zaloAccountId);

    // ==================== Issue #5: Atomic queue number via PostgreSQL sequence ====================

    // Issue #5: get next queue number atomically from PostgreSQL sequence
    @Query(value = "SELECT nextval('queue_number_seq')", nativeQuery = true)
    int getNextQueueNumber();

    // Issue #5: reset daily sequence at midnight (called by ApplicationSchedulerService)
    @Modifying
    @Query(value = "ALTER SEQUENCE queue_number_seq RESTART WITH 1", nativeQuery = true)
    void resetQueueSequence();

    // ==================== Issue #7: Pessimistic lock for call-next race condition ====================

    // Issue #7: fetch oldest queued application for a specific counter with pessimistic write lock
    // Prevents two staff at different counters grabbing the same application simultaneously
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Application a WHERE a.currentPhase = :phase " +
           "AND a.id IN (SELECT DISTINCT h.application.id FROM ApplicationHistory h " +
           "WHERE h.appointmentDate = :date AND h.counter.id = :counterId) " +
           "ORDER BY a.queueNumber ASC")
    List<Application> findOldestPendingForCounter(
            @Param("date") LocalDate date,
            @Param("phase") Integer phase,
            @Param("counterId") Integer counterId);

    // ==================== Issue #10: Pagination for list endpoints ====================

    // Issue #10: paginated query for StaffHoSoController.getList()
    Page<Application> findByCurrentPhase(Integer phase, Pageable pageable);

    @Query("SELECT a FROM Application a ORDER BY a.createdAt DESC")
    Page<Application> findAllPaged(Pageable pageable);

    // Issue #10: COUNT queries for dashboard stats (avoids loading all entities into memory)
    @Query("SELECT a.currentPhase, COUNT(a) FROM Application a GROUP BY a.currentPhase")
    List<Object[]> countGroupByPhase();

    @Query("SELECT COUNT(a) FROM Application a WHERE a.currentPhase IN (1, 2, 3, 5) AND a.deadline < :today")
    long countOverdue(@Param("today") LocalDate today);

    // Issue #10: count people ahead in queue (replaces findAll().stream().filter() in CitizenController)
    @Query("SELECT COUNT(a) FROM Application a WHERE a.currentPhase IN (1, 2) " +
           "AND CAST(a.createdAt AS LocalDate) = :today " +
           "AND a.queueNumber < :queueNumber")
    int countPeopleAhead(@Param("today") LocalDate today, @Param("queueNumber") Integer queueNumber);
}
