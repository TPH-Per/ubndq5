package com.example.demo.service;

import com.example.demo.dto.response.ApplicationResponse;
import com.example.demo.entity.Application;
import com.example.demo.entity.ApplicationHistory;
import com.example.demo.entity.Staff;
import com.example.demo.repository.ApplicationHistoryRepository;
import com.example.demo.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Shared helper service for Staff Queue controllers.
 * Extracted from StaffQueueController to keep files under 200 LOC.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StaffQueueService {

    private final StaffRepository staffRepository;
    private final ApplicationHistoryRepository applicationHistoryRepository;

    /**
     * Resolve current staff from authentication token.
     */
    public Staff getCurrentStaff(Authentication authentication) {
        String staffCode = authentication.getName();
        return staffRepository.findByStaffCode(staffCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));
    }

    /**
     * Persist an ApplicationHistory entry for an action.
     */
    public void saveHistory(Application app, Staff staff, String action,
            int oldPhase, int newPhase, String content) {
        ApplicationHistory history = ApplicationHistory.builder()
                .application(app)
                .counter(staff.getCounter())
                .staff(staff)
                .action(action)
                .phaseFrom(oldPhase)
                .phaseTo(newPhase)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();
        applicationHistoryRepository.save(history);
    }

    /**
     * Map Application entity to a lightweight response DTO.
     */
    public ApplicationResponse mapToResponse(Application app) {
        List<ApplicationHistory> appointments = applicationHistoryRepository
                .findLatestAppointmentHistory(app.getId());

        LocalDate appointmentDate = null;
        LocalTime expectedTime = null;

        if (!appointments.isEmpty()) {
            ApplicationHistory latest = appointments.get(0);
            appointmentDate = latest.getAppointmentDate();
            expectedTime = latest.getExpectedTime();
        }

        return ApplicationResponse.builder()
                .id(app.getId())
                .applicationCode(app.getApplicationCode())
                .procedureId(app.getProcedure().getId())
                .procedureCode(app.getProcedure().getProcedureCode())
                .procedureName(app.getProcedure().getProcedureName())
                .citizenId(app.getCitizenCccd())
                .citizenName(app.getCitizenName())
                .citizenPhone(app.getCitizenPhone())
                .currentPhase(app.getCurrentPhase())
                .phaseName(ApplicationResponse.getPhaseName(app.getCurrentPhase()))
                .queueNumber(app.getQueueNumber())
                .queuePrefix(app.getQueuePrefix())
                .queueDisplay(app.getQueueDisplay())
                .appointmentDate(appointmentDate)
                .expectedTime(expectedTime)
                .deadline(app.getDeadline())
                .priority(app.getPriority())
                .priorityName(ApplicationResponse.getPriorityName(app.getPriority()))
                .cancelReason(app.getCancelReason())
                .cancelType(app.getCancelType())
                .createdAt(app.getCreatedAt())
                .updatedAt(app.getUpdatedAt())
                .build();
    }
}
