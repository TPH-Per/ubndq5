package com.example.demo.controller;

import com.example.demo.dto.response.ApiResponse;
import com.example.demo.entity.Application;
import com.example.demo.entity.Staff;
import com.example.demo.repository.ApplicationHistoryRepository;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.StaffRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Staff Dashboard Controller
 * API cho trang tổng quan của nhân viên
 */
@RestController
@RequestMapping("/api/staff/dashboard")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('Staff') or hasRole('Admin')")
@Transactional
public class StaffDashboardController {

        private final ApplicationRepository applicationRepository;
        private final ApplicationHistoryRepository applicationHistoryRepository;
        private final StaffRepository staffRepository;

        @Data
        @Builder
        public static class StaffDashboardData {
                private String tenNhanVien;
                private String maNhanVien;
                private String tenQuay;
                private String maQuay;
                private Integer tongSoChoHomNay;
                private Integer daXuLyHomNay;
                private Integer dangXuLy;
                private Integer tongHoSoDangXuLy;
                private Integer hoSoTreHan;
        }

        /**
         * Lấy thông tin tổng quan dashboard
         * GET /api/staff/dashboard
         */
        @GetMapping
        public ResponseEntity<ApiResponse<StaffDashboardData>> getDashboard(Authentication authentication) {
                Staff staff = getCurrentStaff(authentication);
                LocalDate today = LocalDate.now();

                // Thống kê lịch hẹn hôm nay - dùng applicationHistoryRepository
                List<Application> todayApps = applicationHistoryRepository.findApplicationsByAppointmentDate(today);

                int tongSoChoHomNay = (int) todayApps.stream()
                                .filter(a -> a.getCurrentPhase() == Application.PHASE_QUEUE)
                                .count();

                int daXuLyHomNay = (int) todayApps.stream()
                                .filter(a -> a.getCurrentPhase() == Application.PHASE_COMPLETED)
                                .count();

                int dangXuLy = (int) todayApps.stream()
                                .filter(a -> a.getCurrentPhase() == Application.PHASE_PROCESSING)
                                .count();

                // Thống kê hồ sơ
                List<Application> allApps = applicationRepository.findAll();

                int tongHoSoDangXuLy = (int) allApps.stream()
                                .filter(a -> a.getCurrentPhase() == Application.PHASE_PENDING ||
                                                a.getCurrentPhase() == Application.PHASE_PROCESSING)
                                .count();

                int hoSoTreHan = (int) allApps.stream()
                                .filter(a -> a.getDeadline() != null &&
                                                a.getDeadline().isBefore(today) &&
                                                a.getCurrentPhase() != Application.PHASE_COMPLETED &&
                                                a.getCurrentPhase() != Application.PHASE_CANCELLED)
                                .count();

                StaffDashboardData dashboard = StaffDashboardData.builder()
                                .tenNhanVien(staff.getFullName())
                                .maNhanVien(staff.getStaffCode())
                                .tenQuay(staff.getCounter() != null ? staff.getCounter().getCounterName() : null)
                                .maQuay(staff.getCounter() != null ? staff.getCounter().getCounterCode() : null)
                                .tongSoChoHomNay(tongSoChoHomNay)
                                .daXuLyHomNay(daXuLyHomNay)
                                .dangXuLy(dangXuLy)
                                .tongHoSoDangXuLy(tongHoSoDangXuLy)
                                .hoSoTreHan(hoSoTreHan)
                                .build();

                return ResponseEntity.ok(ApiResponse.success(dashboard));
        }

        private Staff getCurrentStaff(Authentication authentication) {
                String staffCode = authentication.getName();
                return staffRepository.findByStaffCode(staffCode)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));
        }
}
