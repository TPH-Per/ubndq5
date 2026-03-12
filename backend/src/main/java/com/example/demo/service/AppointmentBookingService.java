package com.example.demo.service;

import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.AppointmentRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Xử lý atomic slot reservation để tránh race condition khi đặt lịch.
 *
 * Vấn đề: 2 request đồng thời cùng đọc count=4, cùng pass check,
 * cùng insert → slot vượt capacity 5/5.
 *
 * Giải pháp: PostgreSQL advisory lock per (date, time) slot.
 * - pg_advisory_xact_lock: transaction-scoped, tự release khi commit/rollback
 * - Caller phải có active @Transactional — lock giữ đến hết transaction đó
 */
@Service
@RequiredArgsConstructor
public class AppointmentBookingService {

    public static final int MAX_SLOT_CAPACITY = 1;

    private final AppointmentRepository appointmentRepository;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Acquire advisory lock cho slot (date, time) và kiểm tra còn chỗ không.
     *
     * Phải được gọi trong một active transaction — lock giữ đến khi transaction kết thúc,
     * đảm bảo mọi write sau đó (Application + Appointment) nằm trong vùng được bảo vệ.
     *
     * @throws AppException(SLOT_FULL) nếu slot đã đủ MAX_SLOT_CAPACITY người
     */
    @Transactional
    public void acquireSlotLock(LocalDate date, LocalTime time) {
        // Lock key: unique per (date, time) slot — fits in PostgreSQL bigint
        // Công thức: epochDay * 10000 + HH * 100 + mm
        // Ví dụ: 2026-03-10 09:30 → 20524 * 10000 + 930 = 205240930
        long lockKey = date.toEpochDay() * 10000L + time.getHour() * 100L + time.getMinute();

        // Blocks other transactions đang cố book cùng slot — serializes concurrent requests
        entityManager.createNativeQuery("SELECT pg_advisory_xact_lock(:key)")
                .setParameter("key", lockKey)
                .getSingleResult();

        // Re-count sau khi đã hold lock — guaranteed chính xác, không có concurrent write
        long bookedCount = appointmentRepository.countBookedByDateAndTime(date, time);
        if (bookedCount >= MAX_SLOT_CAPACITY) {
            throw new AppException(ErrorCode.SLOT_FULL);
        }
    }
}
