package com.example.demo.service;

import com.example.demo.entity.Application;
import com.example.demo.entity.ApplicationHistory;
import com.example.demo.repository.ApplicationHistoryRepository;
import com.example.demo.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Background scheduler for automatic application management tasks.
 * Issue #4 fix: auto-cancel moved here from getDashboard() GET handler.
 * Issue #5: daily queue number sequence reset at midnight.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationSchedulerService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationHistoryRepository applicationHistoryRepository;

    @Value("${app.auto-cancel.enabled:true}")
    private boolean autoCancelEnabled;

    /**
     * Issue #4 fix: auto-cancel queue entries > 24 min past their expected time.
     * Runs every 5 minutes. Previously this was side-effecting inside getDashboard() GET.
     */
    @Scheduled(fixedRate = 300000)
    @Transactional
    public void autoCancelLateApplications() {
        if (!autoCancelEnabled) return;

        LocalDate today = LocalDate.now();
        LocalTime cutoff = LocalTime.now().minusMinutes(24);

        List<ApplicationHistory> queueHistories =
                applicationHistoryRepository.findActiveQueueHistories(today, Application.PHASE_QUEUE);

        int cancelled = 0;
        for (ApplicationHistory h : queueHistories) {
            if (h.getExpectedTime() != null && h.getExpectedTime().isBefore(cutoff)) {
                Application app = h.getApplication();
                // Guard: only cancel if still in QUEUE phase
                if (app.getCurrentPhase() != Application.PHASE_QUEUE) continue;

                app.setCurrentPhase(Application.PHASE_CANCELLED);
                app.setCancelReason("Tự động hủy do trễ hẹn quá 24 phút");
                app.setCancelType(Application.CANCEL_NO_SHOW);
                applicationRepository.save(app);

                ApplicationHistory cancelHistory = ApplicationHistory.builder()
                        .application(app)
                        .action(ApplicationHistory.ACTION_CANCEL_NO_SHOW)
                        .phaseFrom(Application.PHASE_QUEUE)
                        .phaseTo(Application.PHASE_CANCELLED)
                        .content("Tự động hủy do quá giờ hẹn (scheduled job)")
                        .createdAt(LocalDateTime.now())
                        .build();
                applicationHistoryRepository.save(cancelHistory);
                cancelled++;
            }
        }

        if (cancelled > 0) {
            log.info("[Scheduler] Auto-cancelled {} late applications", cancelled);
        }
    }

    /**
     * Issue #5: reset PostgreSQL daily queue number sequence at midnight.
     * Ensures queue numbers restart from 1 each day.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void resetDailyQueueSequence() {
        applicationRepository.resetQueueSequence();
        log.info("[Scheduler] Daily queue number sequence reset");
    }
}
