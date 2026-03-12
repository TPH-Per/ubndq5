package com.example.demo.repository;

import com.example.demo.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

    // Lấy các slot đã book (chỉ status SCHEDULED)
    @Query("SELECT a.appointmentTime FROM Appointment a WHERE a.appointmentDate = :date AND a.status = 0")
    List<LocalTime> findBookedTimes(@Param("date") LocalDate date);

    // Tìm lịch active của hồ sơ
    @Query("SELECT a FROM Appointment a WHERE a.application.id = :appId AND a.status = 0")
    List<Appointment> findActiveByApplicationId(@Param("appId") Integer appId);

    // Issue #8: count bookings per slot for capacity calculation (max 5 per slot)
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.appointmentDate = :date AND a.appointmentTime = :time AND a.status = 0")
    long countBookedByDateAndTime(@Param("date") LocalDate date, @Param("time") java.time.LocalTime time);

    // Tìm tất cả lịch hẹn của 1 tài khoản Zalo (Zalo info nằm trong Application)
    @Query("SELECT a FROM Appointment a WHERE a.application.zaloAccount.zaloId = :zaloId ORDER BY a.appointmentDate DESC, a.appointmentTime DESC")
    List<Appointment> findByZaloIdOrderByDateDesc(@Param("zaloId") String zaloId);
}
