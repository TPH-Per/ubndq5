package com.example.demo.repository;

import com.example.demo.entity.Application;
import com.example.demo.entity.ApplicationHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ApplicationHistoryRepository extends JpaRepository<ApplicationHistory, Integer> {

        @Query("SELECT h FROM ApplicationHistory h WHERE h.application.id = :applicationId ORDER BY h.createdAt DESC")
        List<ApplicationHistory> findByApplicationId(Integer applicationId);

        @Query("SELECT h FROM ApplicationHistory h WHERE h.counter.id = :counterId ORDER BY h.createdAt DESC")
        List<ApplicationHistory> findByCounterId(Integer counterId);

        @Query("SELECT h FROM ApplicationHistory h WHERE h.staff.id = :staffId ORDER BY h.createdAt DESC")
        List<ApplicationHistory> findByStaffId(Integer staffId);

        @Query("SELECT h FROM ApplicationHistory h WHERE h.application.id = :applicationId AND h.action = :action ORDER BY h.createdAt DESC")
        List<ApplicationHistory> findByApplicationIdAndAction(Integer applicationId, String action);

        // === Queries cho lịch hẹn (appointment) ===

        // Lấy danh sách application có hẹn ngày hôm nay theo phase
        @Query("SELECT DISTINCT h.application FROM ApplicationHistory h " +
                        "WHERE h.appointmentDate = :date " +
                        "AND h.application.currentPhase = :phase " +
                        "ORDER BY h.application.queueNumber ASC")
        List<Application> findApplicationsByAppointmentDateAndPhase(
                        @Param("date") LocalDate date,
                        @Param("phase") Integer phase);

        // Lấy danh sách application có hẹn ngày hôm nay theo list phases
        @Query("SELECT DISTINCT h.application FROM ApplicationHistory h " +
                        "WHERE h.appointmentDate = :date " +
                        "AND h.application.currentPhase IN :phases " +
                        "ORDER BY h.application.queueNumber ASC")
        List<Application> findApplicationsByAppointmentDateAndPhases(
                        @Param("date") LocalDate date,
                        @Param("phases") List<Integer> phases);

        @Query("SELECT h.expectedTime FROM ApplicationHistory h WHERE h.appointmentDate = :date AND h.expectedTime IS NOT NULL AND h.application.currentPhase NOT IN (0, 4)")
        List<LocalTime> findBookedTimes(@Param("date") LocalDate date);

        // Lấy danh sách application có hẹn ngày hôm nay
        @Query("SELECT DISTINCT h.application FROM ApplicationHistory h " +
                        "WHERE h.appointmentDate = :date " +
                        "ORDER BY h.application.queueNumber ASC")
        List<Application> findApplicationsByAppointmentDate(@Param("date") LocalDate date);

        // Đếm số application theo ngày hẹn và phase
        @Query("SELECT COUNT(DISTINCT h.application) FROM ApplicationHistory h " +
                        "WHERE h.appointmentDate = :date " +
                        "AND h.application.currentPhase = :phase")
        long countByAppointmentDateAndPhase(
                        @Param("date") LocalDate date,
                        @Param("phase") Integer phase);

        // Lấy history record cuối cùng có appointment cho application
        @Query("SELECT h FROM ApplicationHistory h " +
                        "WHERE h.application.id = :applicationId " +
                        "AND h.appointmentDate IS NOT NULL " +
                        "ORDER BY h.createdAt DESC")
        List<ApplicationHistory> findLatestAppointmentHistory(Integer applicationId);

        @Query("SELECT h FROM ApplicationHistory h " +
                        "WHERE h.appointmentDate = :date " +
                        "AND h.phaseTo = :phase " +
                        "AND h.application.currentPhase = :phase " +
                        "AND h.id IN (SELECT MAX(h2.id) FROM ApplicationHistory h2 GROUP BY h2.application) " +
                        "ORDER BY h.expectedTime ASC, h.application.queueNumber ASC")
        List<ApplicationHistory> findActiveQueueHistories(@Param("date") LocalDate date, @Param("phase") Integer phase);

        // ==================== Issue #6: Counter-filtered variants ====================

        // Issue #6: active queue histories for a specific counter only
        @Query("SELECT h FROM ApplicationHistory h " +
                        "WHERE h.appointmentDate = :date " +
                        "AND h.phaseTo = :phase " +
                        "AND h.application.currentPhase = :phase " +
                        "AND h.counter.id = :counterId " +
                        "AND h.id IN (SELECT MAX(h2.id) FROM ApplicationHistory h2 GROUP BY h2.application) " +
                        "ORDER BY h.expectedTime ASC, h.application.queueNumber ASC")
        List<ApplicationHistory> findActiveQueueHistoriesByCounter(
                        @Param("date") LocalDate date,
                        @Param("phase") Integer phase,
                        @Param("counterId") Integer counterId);

        // Issue #6: applications for a given date, phase, and counter
        @Query("SELECT DISTINCT h.application FROM ApplicationHistory h " +
                        "WHERE h.appointmentDate = :date " +
                        "AND h.application.currentPhase = :phase " +
                        "AND h.counter.id = :counterId " +
                        "ORDER BY h.application.queueNumber ASC")
        List<Application> findApplicationsByAppointmentDateAndPhaseAndCounter(
                        @Param("date") LocalDate date,
                        @Param("phase") Integer phase,
                        @Param("counterId") Integer counterId);

        // Issue #6: count for dashboard stats filtered by counter
        @Query("SELECT COUNT(DISTINCT h.application) FROM ApplicationHistory h " +
                        "WHERE h.appointmentDate = :date " +
                        "AND h.application.currentPhase = :phase " +
                        "AND h.counter.id = :counterId")
        long countByAppointmentDateAndPhaseAndCounter(
                        @Param("date") LocalDate date,
                        @Param("phase") Integer phase,
                        @Param("counterId") Integer counterId);

        // ==================== Admin cross-counter queries ====================

        // Total distinct applications scheduled on a given date (all counters)
        @Query("SELECT COUNT(DISTINCT h.application.id) FROM ApplicationHistory h WHERE h.appointmentDate = :date")
        long countByDate(@Param("date") LocalDate date);

        // Per-counter breakdown: [counterId, counterName, count]
        @Query("SELECT h.counter.id, h.counter.counterName, COUNT(DISTINCT h.application.id) " +
                "FROM ApplicationHistory h " +
                "WHERE h.appointmentDate = :date AND h.counter IS NOT NULL " +
                "GROUP BY h.counter.id, h.counter.counterName " +
                "ORDER BY h.counter.counterName ASC")
        List<Object[]> countByDateGroupByCounter(@Param("date") LocalDate date);

        // Paginated: all applications scheduled on date (any counter)
        @Query(value = "SELECT a FROM Application a WHERE a.id IN " +
                "(SELECT DISTINCT h.application.id FROM ApplicationHistory h WHERE h.appointmentDate = :date)",
               countQuery = "SELECT COUNT(DISTINCT h.application.id) FROM ApplicationHistory h WHERE h.appointmentDate = :date")
        Page<Application> findApplicationsByDatePaged(@Param("date") LocalDate date, Pageable pageable);

        // Paginated: applications scheduled on date at a specific counter
        @Query(value = "SELECT a FROM Application a WHERE a.id IN " +
                "(SELECT DISTINCT h.application.id FROM ApplicationHistory h " +
                " WHERE h.appointmentDate = :date AND h.counter.id = :counterId)",
               countQuery = "SELECT COUNT(DISTINCT h.application.id) FROM ApplicationHistory h " +
                "WHERE h.appointmentDate = :date AND h.counter.id = :counterId")
        Page<Application> findApplicationsByDateAndCounterPaged(
                @Param("date") LocalDate date,
                @Param("counterId") Integer counterId,
                Pageable pageable);

        // ==================== ProcedureType-filtered variants ====================

        // Active queue histories scoped to a procedure type (for PHASE_QUEUE)
        @Query("SELECT h FROM ApplicationHistory h " +
                        "WHERE h.appointmentDate = :date " +
                        "AND h.phaseTo = :phase " +
                        "AND h.application.currentPhase = :phase " +
                        "AND h.application.procedure.procedureType.id = :procedureTypeId " +
                        "AND h.id IN (SELECT MAX(h2.id) FROM ApplicationHistory h2 GROUP BY h2.application) " +
                        "ORDER BY h.expectedTime ASC, h.application.queueNumber ASC")
        List<ApplicationHistory> findActiveQueueHistoriesByProcedureType(
                        @Param("date") LocalDate date,
                        @Param("phase") Integer phase,
                        @Param("procedureTypeId") Integer procedureTypeId);

        // Applications scoped to a procedure type (for any phase)
        // Only match applications whose LATEST appointment history has this date
        @Query("SELECT DISTINCT h.application FROM ApplicationHistory h " +
                        "WHERE h.appointmentDate = :date " +
                        "AND h.application.currentPhase = :phase " +
                        "AND h.application.procedure.procedureType.id = :procedureTypeId " +
                        "AND h.id = (SELECT MAX(h2.id) FROM ApplicationHistory h2 " +
                        "           WHERE h2.application = h.application AND h2.appointmentDate IS NOT NULL) " +
                        "ORDER BY h.application.queueNumber ASC")
        List<Application> findApplicationsByAppointmentDateAndPhaseAndProcedureType(
                        @Param("date") LocalDate date,
                        @Param("phase") Integer phase,
                        @Param("procedureTypeId") Integer procedureTypeId);

        // Count scoped to a procedure type (latest appointment only)
        @Query("SELECT COUNT(DISTINCT h.application) FROM ApplicationHistory h " +
                        "WHERE h.appointmentDate = :date " +
                        "AND h.application.currentPhase = :phase " +
                        "AND h.application.procedure.procedureType.id = :procedureTypeId " +
                        "AND h.id = (SELECT MAX(h2.id) FROM ApplicationHistory h2 " +
                        "           WHERE h2.application = h.application AND h2.appointmentDate IS NOT NULL)")
        long countByAppointmentDateAndPhaseAndProcedureType(
                        @Param("date") LocalDate date,
                        @Param("phase") Integer phase,
                        @Param("procedureTypeId") Integer procedureTypeId);
}
