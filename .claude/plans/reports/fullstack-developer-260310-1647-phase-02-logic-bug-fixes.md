# Phase Implementation Report

## Executed Phase
- Phase: phase-02-logic-bug-fixes
- Plan: /home/cena/Documents/HANHCHINHCONGQ5CHOLON/plans/260310-1635-security-and-logic-fixes/
- Status: completed

## Files Modified

### Created (new)
| File | LOC | Purpose |
|------|-----|---------|
| `service/StaffQueueService.java` | 95 | Shared helper: getCurrentStaff(), saveHistory(), mapToResponse() |
| `controller/StaffQueueDashboardController.java` | 195 | GET endpoints: dashboard, waiting, current, slots |
| `controller/StaffQueueActionController.java` | 200 | POST endpoints: call-next, complete, receive, cancel, supplement |
| `service/ApplicationSchedulerService.java` | 82 | @Scheduled auto-cancel (Issue #4) + midnight sequence reset (Issue #5) |
| `db/migration/V24__add_queue_number_sequence.sql` | 11 | PostgreSQL sequence for atomic queue numbers (Issue #5) |

### Modified
| File | Changes |
|------|---------|
| `controller/StaffQueueController.java` | **DELETED** — replaced by 3 split files |
| `repository/ApplicationRepository.java` | +getNextQueueNumber(), +resetQueueSequence(), +findOldestPendingForCounter() (@Lock), +findByCurrentPhase(Pageable), +findAllPaged(), +countGroupByPhase(), +countOverdue(), +countPeopleAhead() |
| `repository/ApplicationHistoryRepository.java` | +findActiveQueueHistoriesByCounter(), +findApplicationsByAppointmentDateAndPhaseAndCounter(), +countByAppointmentDateAndPhaseAndCounter() |
| `repository/AppointmentRepository.java` | +countBookedByDateAndTime() |
| `controller/CitizenAppointmentController.java` | Issue #8: slot capacity fixed; Issue #5: queue number via sequence; Issue #10: countPeopleAhead() replaces findAll().stream() |
| `controller/CitizenController.java` | Issue #10: countPeopleAhead() replaces findAll().stream().filter() in getQueueStatus() |
| `controller/StaffHoSoController.java` | Issue #10: getDashboard() → countGroupByPhase(); getList() → paginated (Page<HoSoResponse>); added Page/Pageable/Sort imports |
| `DemoApplication.java` | +@EnableScheduling (Issue #4) |
| `resources/application.properties` | +app.auto-cancel.enabled=true (Issue #4) |

## Tasks Completed

- [x] Pre-work: Split StaffQueueController (592 LOC) into 3 files
- [x] Issue #4: Create ApplicationSchedulerService with @Scheduled auto-cancel (5-min interval)
- [x] Issue #4: Remove auto-cancel block from getDashboard() — now read-only GET
- [x] Issue #4: Add @EnableScheduling to DemoApplication
- [x] Issue #4: Add app.auto-cancel.enabled config toggle
- [x] Issue #5: Create V24 migration — queue_number_seq PostgreSQL sequence
- [x] Issue #5: Add getNextQueueNumber() + resetQueueSequence() to ApplicationRepository
- [x] Issue #5: Update CitizenAppointmentController.createAppointment() to use sequence
- [x] Issue #5: Add midnight sequence reset to ApplicationSchedulerService
- [x] Issue #6: Add 3 counter-filtered queries to ApplicationHistoryRepository
- [x] Issue #6: StaffQueueDashboardController uses counter-filtered queries for all dashboard/waiting/current/stats
- [x] Issue #7: Add findOldestPendingForCounter() with @Lock(PESSIMISTIC_WRITE) to ApplicationRepository
- [x] Issue #7: StaffQueueActionController.callNext() uses locked query for auto-select path
- [x] Issue #8: Add countBookedByDateAndTime() to AppointmentRepository
- [x] Issue #8: Fix CitizenAppointmentController.getAvailableSlots() — count-based capacity (max 5 per slot)
- [x] Issue #8: Fix StaffQueueDashboardController.getSlots() — same count-based approach
- [x] Issue #9: supplement() sets PHASE_SUPPLEMENT (6) not PHASE_QUEUE (1); history phaseTo corrected
- [x] Issue #10: StaffHoSoController.getDashboard() uses countGroupByPhase() + countOverdue()
- [x] Issue #10: StaffHoSoController.getList() returns Page<HoSoResponse> with page/size params (max 100)
- [x] Issue #10: Add countPeopleAhead() to ApplicationRepository
- [x] Issue #10: Replace findAll().stream() in CitizenAppointmentController + CitizenController

## Tests Status
- Type check: not run (instructions say DO NOT run build)
- Unit tests: not run
- Integration tests: not run

## Adaptations from Plan

1. **StaffQueueDashboardController LOC**: ended at ~195 LOC (plan target ~120). Kept together because splitting getWaitingList/getCurrentProcessing further would scatter closely related read endpoints. Under 200 LOC limit ✓

2. **StaffQueueActionController LOC**: ended at ~200 LOC (plan target ~120). All 5 action methods have significant error-handling bodies; splitting further would hurt readability. At boundary ✓

3. **`getAvailableSlots` query approach**: Used per-slot `countBookedByDateAndTime()` calls in a loop rather than a single grouped query. Simpler to implement and correct. 20 slots per request = 20 queries, acceptable for this use case. Could be optimized later with a grouped-by-time query if needed.

4. **`findOldestPendingForCounter` JPQL**: Used subquery against `ApplicationHistory.counter.id` since `Application` has no direct `counter` FK. Matches actual entity relationships.

5. **CitizenController.getQueueStatus()**: `findApplicationsByAppointmentDate()` load removed; replaced with `countPeopleAhead()`. The `applicationHistoryRepository` field is retained (still used for `findApplicationsByAppointmentDateAndPhase()`).

6. **`StaffQueueDashboardController.getWaitingList()`**: Now accepts `Authentication` to extract `counterId` and filter by counter. Gracefully falls back to unfiltered query if staff has no counter assigned.

## Issues Encountered
- None blocking. All entity relationships matched assumptions (ApplicationHistory has counter FK, Application has no direct counter FK).

## Next Steps
- Run `mvn compile` to verify no import/type errors before deploy
- Frontend (AdminStaff Vue): `getList()` now returns `Page<HoSoResponse>` wrapper — frontend must handle `.content` array and `.totalElements`/`.totalPages` pagination metadata
- Consider adding grouped slot query to replace per-slot loop in `getAvailableSlots` if load increases
