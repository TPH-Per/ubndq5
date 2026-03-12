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

/**
 * Staff Dashboard Controller
 * API cho trang tổng quan của nhân viên
 */
@RestController
@RequestMapping("/api/staff/dashboard")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('Staff') or hasRole('Admin')")
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
         * Lấy thông tin tổng quan dashboard.
         * Dùng COUNT queries — không load toàn bộ DB vào RAM.
         * GET /api/staff/dashboard
         */
        @GetMapping
        @Transactional(readOnly = true)
        public ResponseEntity<ApiResponse<StaffDashboardData>> getDashboard(Authentication authentication) {
                Staff staff = getCurrentStaff(authentication);
                LocalDate today = LocalDate.now();

                // Today's appointment counts via COUNT queries (no entity loading)
                int tongSoChoHomNay = (int) applicationHistoryRepository
                                .countByAppointmentDateAndPhase(today, Application.PHASE_QUEUE);
                int daXuLyHomNay = (int) applicationHistoryRepository
                                .countByAppointmentDateAndPhase(today, Application.PHASE_COMPLETED);
                int dangXuLy = (int) applicationHistoryRepository
                                .countByAppointmentDateAndPhase(today, Application.PHASE_PROCESSING);

                // Global stats via COUNT queries (countByPhase + countOverdue)
                long pendingCount    = applicationRepository.countByPhase(Application.PHASE_PENDING);
                long processingCount = applicationRepository.countByPhase(Application.PHASE_PROCESSING);
                int tongHoSoDangXuLy = (int) (pendingCount + processingCount);
                int hoSoTreHan       = (int) applicationRepository.countOverdue(today);

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
