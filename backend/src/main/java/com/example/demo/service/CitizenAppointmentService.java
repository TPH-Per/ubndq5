package com.example.demo.service;

import com.example.demo.controller.CitizenHelperUtils;
import com.example.demo.dto.request.CreateAppointmentRequest;
import com.example.demo.entity.*;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Business logic for citizen appointment operations.
 * Controller is responsible only for HTTP plumbing; all domain rules live here.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CitizenAppointmentService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationHistoryRepository applicationHistoryRepository;
    private final AppointmentRepository appointmentRepository;
    private final ProcedureRepository procedureRepository;
    private final ZaloAccountRepository zaloAccountRepository;
    private final AppointmentBookingService appointmentBookingService;

    // ======================== BOOK ========================

    @Transactional
    public Map<String, Object> createAppointment(CreateAppointmentRequest req) {
        // Parse and validate dates
        LocalDate appointmentDate;
        LocalTime appointmentTime;
        try {
            appointmentDate = LocalDate.parse(req.getAppointmentDate());
            appointmentTime = LocalTime.parse(req.getAppointmentTime());
        } catch (DateTimeParseException e) {
            throw new AppException(ErrorCode.INVALID_FORMAT);
        }

        if (appointmentDate.isBefore(LocalDate.now())) {
            throw new AppException(ErrorCode.VALIDATION_ERROR);
        }

        LocalDateTime appointmentDateTime = LocalDateTime.of(appointmentDate, appointmentTime);
        if (LocalDateTime.now().plusHours(2).isAfter(appointmentDateTime)) {
            throw new AppException(ErrorCode.APPOINTMENT_TOO_SOON_TO_BOOK);
        }

        Procedure procedure = procedureRepository.findById(req.getProcedureId())
                .orElseThrow(() -> new AppException(ErrorCode.LOAITHUTUC_NOT_FOUND));

        // Atomic slot reservation via PostgreSQL advisory lock
        appointmentBookingService.acquireSlotLock(appointmentDate, appointmentTime);

        // Resolve or create Zalo account
        ZaloAccount zaloAccount = zaloAccountRepository.findByZaloId(req.getZaloId())
                .orElseGet(() -> zaloAccountRepository.save(
                        ZaloAccount.builder()
                                .zaloId(req.getZaloId())
                                .zaloName(req.getZaloName())
                                .isActive(true)
                                .build()));
        if (req.getZaloName() != null && !req.getZaloName().equals(zaloAccount.getZaloName())) {
            zaloAccount.setZaloName(req.getZaloName());
            zaloAccountRepository.save(zaloAccount);
        }

        int queueNumber = applicationRepository.getNextQueueNumber();
        String prefix = procedure.getProcedureCode().substring(0, Math.min(2, procedure.getProcedureCode().length()));

        Application app = applicationRepository.save(Application.builder()
                .applicationCode(
                        "HS-" + LocalDate.now().toString().replace("-", "") + "-" + String.format("%03d", queueNumber))
                .procedure(procedure)
                .citizenCccd(req.getCitizenCccd())
                .citizenName(req.getCitizenName())
                .citizenPhone(req.getCitizenPhone())
                .citizenEmail(req.getCitizenEmail())
                .zaloAccount(zaloAccount)
                .currentPhase(Application.PHASE_PENDING)
                .priority(Application.PRIORITY_NORMAL)
                .queueNumber(queueNumber)
                .queuePrefix(prefix)
                .build());

        appointmentRepository.save(Appointment.builder()
                .application(app)
                .zaloAccount(zaloAccount)
                .appointmentDate(appointmentDate)
                .appointmentTime(appointmentTime)
                .status(Appointment.STATUS_SCHEDULED)
                .build());

        applicationHistoryRepository.save(ApplicationHistory.builder()
                .application(app)
                .action("ĐẶT LỊCH")
                .phaseFrom(null)
                .phaseTo(Application.PHASE_PENDING)
                .content((req.getNotes() != null && !req.getNotes().trim().isEmpty())
                        ? "Đặt lịch hẹn qua Zalo Mini App. Ghi chú: " + req.getNotes()
                        : "Đặt lịch hẹn qua Zalo Mini App")
                .appointmentDate(appointmentDate)
                .expectedTime(appointmentTime)
                .createdAt(LocalDateTime.now())
                .build());

        log.info("Appointment booked: zaloId={} procedure={}", req.getZaloId(), procedure.getProcedureCode());

        Map<String, Object> result = new HashMap<>();
        result.put("id", app.getId());
        result.put("code", app.getApplicationCode());
        result.put("procedureName", procedure.getProcedureName());
        result.put("appointmentDate", appointmentDate.toString());
        result.put("appointmentTime", appointmentTime.toString());
        result.put("queueDisplay", app.getQueueDisplay());
        result.put("status", "SCHEDULED");
        result.put("zaloLinked", true);
        return result;
    }

    // ======================== SEARCH ========================

    @Transactional(readOnly = true)
    public List<Map<String, Object>> searchAppointments(String zaloId, String status) {
        if (zaloId == null || zaloId.isEmpty()) {
            throw new AppException(ErrorCode.MISSING_ZALO_AUTH);
        }

        List<Application> apps = applicationRepository.findByZaloAccount_ZaloIdOrderByCreatedAtDesc(zaloId);

        if (status != null) {
            apps = apps.stream().filter(app -> switch (status.toUpperCase()) {
                case "UPCOMING" -> app.getCurrentPhase() == Application.PHASE_PENDING
                        || app.getCurrentPhase() == Application.PHASE_QUEUE;
                case "COMPLETED" -> app.getCurrentPhase() == Application.PHASE_COMPLETED;
                case "CANCELLED" -> app.getCurrentPhase() == Application.PHASE_CANCELLED;
                default -> true;
            }).collect(Collectors.toList());
        }

        return apps.stream().map(app -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", app.getId());
            map.put("code", app.getApplicationCode());
            map.put("procedureName", app.getProcedure().getProcedureName());
            map.put("status", CitizenHelperUtils.getStatusName(app.getCurrentPhase()));
            map.put("queueDisplay", app.getQueueDisplay());
            map.put("createdAt", app.getCreatedAt());

            List<ApplicationHistory> histories = applicationHistoryRepository.findLatestAppointmentHistory(app.getId());
            if (!histories.isEmpty()) {
                map.put("appointmentDate", histories.get(0).getAppointmentDate());
                map.put("appointmentTime", histories.get(0).getExpectedTime());
            }
            return map;
        }).collect(Collectors.toList());
    }

    // ======================== CANCEL ========================

    @Transactional
    public void cancelAppointment(Integer id, String zaloId) {
        if (zaloId == null || zaloId.isEmpty()) {
            throw new AppException(ErrorCode.MISSING_ZALO_AUTH);
        }

        // Uniform 404 — prevents resource enumeration
        Application app = applicationRepository.findById(id).orElse(null);
        if (app == null || app.getZaloAccount() == null
                || !app.getZaloAccount().getZaloId().equals(zaloId)) {
            throw new AppException(ErrorCode.APPLICATION_NOT_FOUND);
        }

        if (app.getCurrentPhase() != Application.PHASE_PENDING
                && app.getCurrentPhase() != Application.PHASE_QUEUE) {
            throw new AppException(ErrorCode.CANNOT_CANCEL_IN_CURRENT_STATUS);
        }

        List<Appointment> activeAppointments = appointmentRepository.findActiveByApplicationId(app.getId());
        if (!activeAppointments.isEmpty()) {
            Appointment apt = activeAppointments.get(0);
            LocalDateTime appointmentDateTime = LocalDateTime.of(apt.getAppointmentDate(), apt.getAppointmentTime());
            if (LocalDateTime.now().plusHours(2).isAfter(appointmentDateTime)) {
                throw new AppException(ErrorCode.APPOINTMENT_TOO_LATE_TO_CANCEL);
            }
        }

        int oldPhase = app.getCurrentPhase();
        app.setCurrentPhase(Application.PHASE_CANCELLED);
        app.setCancelReason("Công dân tự hủy");
        app.setCancelType(Application.CANCEL_SELF);
        applicationRepository.save(app);

        for (Appointment a : activeAppointments) {
            a.setStatus(Appointment.STATUS_CANCELLED);
            appointmentRepository.save(a);
        }

        applicationHistoryRepository.save(ApplicationHistory.builder()
                .application(app)
                .action("HỦY LỊCH")
                .phaseFrom(oldPhase)
                .phaseTo(Application.PHASE_CANCELLED)
                .content("Công dân tự hủy lịch hẹn")
                .createdAt(LocalDateTime.now())
                .build());
    }

    // ======================== VIEW ========================

    @Transactional(readOnly = true)
    public Map<String, Object> getAppointmentDetail(Integer id, String zaloId) {
        if (zaloId == null || zaloId.isEmpty()) {
            throw new AppException(ErrorCode.MISSING_ZALO_AUTH);
        }

        // Uniform 404 — prevents resource enumeration
        Application app = applicationRepository.findById(id).orElse(null);
        if (app == null || app.getZaloAccount() == null
                || !app.getZaloAccount().getZaloId().equals(zaloId)) {
            throw new AppException(ErrorCode.APPLICATION_NOT_FOUND);
        }

        boolean inQueue = app.getCurrentPhase() == Application.PHASE_QUEUE
                || app.getCurrentPhase() == Application.PHASE_PENDING;
        int queuePosition = 0;
        if (inQueue && app.getQueueNumber() != null) {
            queuePosition = applicationRepository.countPeopleAhead(LocalDate.now(), app.getQueueNumber());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", app.getId());
        result.put("code", app.getApplicationCode());
        result.put("procedureName", app.getProcedure().getProcedureName());
        result.put("procedureCode", app.getProcedure().getProcedureCode());
        result.put("status", CitizenHelperUtils.getStatusName(app.getCurrentPhase()));
        result.put("queueDisplay", app.getQueueDisplay());
        result.put("queueNumber", app.getQueueNumber());
        result.put("citizenName", app.getCitizenName());
        // §6.2 CCCD handling — never return raw CCCD to citizen-facing API
        result.put("citizenCccd", CitizenHelperUtils.maskCccd(app.getCitizenCccd()));
        result.put("createdAt", app.getCreatedAt());
        result.put("deadline", app.getDeadline());
        result.put("peopleAhead", queuePosition);
        result.put("estimatedWaitMinutes", queuePosition * 15);

        Procedure proc = app.getProcedure();
        result.put("description", proc != null ? proc.getDescription() : null);
        String rawDocs = proc != null ? proc.getRequiredDocuments() : null;
        List<String> docsList = (rawDocs != null && !rawDocs.isBlank())
                ? Arrays.stream(rawDocs.split("\n"))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList())
                : List.of();
        result.put("requiredDocuments", docsList);

        List<ApplicationHistory> histories = applicationHistoryRepository.findLatestAppointmentHistory(app.getId());
        if (!histories.isEmpty()) {
            result.put("appointmentDate", histories.get(0).getAppointmentDate());
            result.put("appointmentTime", histories.get(0).getExpectedTime());
        }
        return result;
    }
}
