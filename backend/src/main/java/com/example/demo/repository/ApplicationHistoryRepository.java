package com.example.demo.repository;

import com.example.demo.entity.Application;
import com.example.demo.entity.ApplicationHistory;
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
        Long countByAppointmentDateAndPhase(
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
}
