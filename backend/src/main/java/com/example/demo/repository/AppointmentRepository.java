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
}
