# Phase 2 -- Logic Bug Fixes (HIGH)

## Context Links
- [plan.md](./plan.md)
- [StaffQueueController.java](../../backend/src/main/java/com/example/demo/controller/StaffQueueController.java) (592 LOC -- must split)
- [CitizenController.java](../../backend/src/main/java/com/example/demo/controller/CitizenController.java)
- [ApplicationRepository.java](../../backend/src/main/java/com/example/demo/repository/ApplicationRepository.java)
- [ApplicationHistoryRepository.java](../../backend/src/main/java/com/example/demo/repository/ApplicationHistoryRepository.java)
- [AppointmentRepository.java](../../backend/src/main/java/com/example/demo/repository/AppointmentRepository.java)
- [Application.java](../../backend/src/main/java/com/example/demo/entity/Application.java)
- [StaffHoSoController.java](../../backend/src/main/java/com/example/demo/controller/StaffHoSoController.java)

## Overview
- **Priority**: P1 -- HIGH
- **Status**: Completed
- **Effort**: ~5h
- **Description**: 7 logic bugs: side-effects in GET, race conditions, missing counter filter, wrong phase, slot waste, no pagination
- **Prerequisite**: Phase 1 (security) must be complete

---

## Pre-work: Split StaffQueueController (592 LOC)

StaffQueueController.java is 592 lines. Per project rules, max 200 LOC per file. Split into 3 files before fixing bugs.

### Split Plan

**File 1: `StaffQueueDashboardController.java`** (~120 LOC)
- `getDashboard()` -- the main dashboard endpoint
- `getWaitingList()` -- waiting list
- `getCurrentProcessing()` -- current processing
- `getSlots()` -- slot availability
- Slot constants (MORNING_SLOTS, AFTERNOON_SLOTS)

**File 2: `StaffQueueActionController.java`** (~120 LOC)
- `callNext()` -- call next in queue
- `complete()` -- mark complete
- `receive()` -- receive documents
- `cancel()` -- cancel/no-show
- `supplement()` -- schedule supplement

**File 3: `service/StaffQueueService.java`** (~100 LOC)
- `getCurrentStaff()` -- shared helper
- `saveHistory()` -- shared helper
- `mapToResponse()` -- shared mapper

### Implementation Steps
1. Create `StaffQueueService.java` with shared methods extracted from controller
2. Create `StaffQueueDashboardController.java` -- inject StaffQueueService
3. Create `StaffQueueActionController.java` -- inject StaffQueueService
4. Delete original `StaffQueueController.java`
5. Both controllers keep `@RequestMapping("/api/staff/queue")` and `@PreAuthorize`
6. Compile check

---

## Issue #4: Auto-cancel in GET Handler

### Problem
`getDashboard()` (lines 100-123) auto-cancels applications > 24 mins late during a GET request. This:
- Violates HTTP semantics (GET must be safe/idempotent)
- Side-effect depends on when staff refreshes dashboard
- Attributes cancel to the staff who happened to refresh

### Fix
Move auto-cancel logic to a `@Scheduled` job.

### Related Code Files
| Action | File |
|--------|------|
| CREATE | `backend/src/main/java/com/example/demo/service/ApplicationSchedulerService.java` |
| MODIFY | `StaffQueueDashboardController.java` (after split) |
| MODIFY | `backend/src/main/resources/application.properties` |

### Implementation Steps

1. **Create `ApplicationSchedulerService.java`**:
   ```java
   package com.example.demo.service;

   @Service
   @Slf4j
   @RequiredArgsConstructor
   @ConditionalOnProperty(name = "app.auto-cancel.enabled", havingValue = "true", matchIfMissing = true)
   public class ApplicationSchedulerService {

       private final ApplicationRepository applicationRepository;
       private final ApplicationHistoryRepository applicationHistoryRepository;

       /**
        * Run every 5 minutes. Cancel applications in QUEUE phase
        * whose expected time is > 24 mins past.
        */
       @Scheduled(fixedRate = 300000) // 5 minutes
       @Transactional
       public void autoCancelLateApplications() {
           LocalDate today = LocalDate.now();
           LocalTime cutoff = LocalTime.now().minusMinutes(24);

           List<ApplicationHistory> queueHistories =
               applicationHistoryRepository.findActiveQueueHistories(today, Application.PHASE_QUEUE);

           int cancelled = 0;
           for (ApplicationHistory h : queueHistories) {
               if (h.getExpectedTime() != null && h.getExpectedTime().isBefore(cutoff)) {
                   Application app = h.getApplication();
                   app.setCurrentPhase(Application.PHASE_CANCELLED);
                   app.setCancelReason("Tu dong huy do tre hen qua 24 phut");
                   app.setCancelType(Application.CANCEL_NO_SHOW);
                   applicationRepository.save(app);

                   ApplicationHistory cancelHistory = ApplicationHistory.builder()
                       .application(app)
                       .action(ApplicationHistory.ACTION_CANCEL_NO_SHOW)
                       .phaseFrom(Application.PHASE_QUEUE)
                       .phaseTo(Application.PHASE_CANCELLED)
                       .content("Tu dong huy do qua gio hen (scheduled)")
                       .createdAt(LocalDateTime.now())
                       .build();
                   applicationHistoryRepository.save(cancelHistory);
                   cancelled++;
               }
           }
           if (cancelled > 0) {
               log.info("Auto-cancelled {} late applications", cancelled);
           }
       }
   }
   ```

2. **Remove auto-cancel block from getDashboard()** -- The for-loop over `waitingHistories` (lines 100-123) should just skip cancelled items without modifying them:
   ```java
   for (ApplicationHistory h : waitingHistories) {
       // No more auto-cancel here -- handled by ApplicationSchedulerService
       ApplicationResponse res = mapToResponse(h.getApplication());
       res.setAppointmentDate(h.getAppointmentDate());
       res.setExpectedTime(h.getExpectedTime());
       waitingResponses.add(res);
   }
   ```

3. **Remove `@Transactional` from dashboard controller class** -- getDashboard is read-only now

4. **application.properties** -- Add config:
   ```properties
   # Auto-cancel scheduler
   app.auto-cancel.enabled=true
   ```

5. **Ensure `@EnableScheduling`** on `DemoApplication.java`:
   ```java
   @SpringBootApplication
   @EnableScheduling
   public class DemoApplication { ... }
   ```

### Success Criteria
- [x] getDashboard() has no write operations
- [x] Scheduler runs every 5 min and cancels late apps
- [x] Config toggle works (`app.auto-cancel.enabled=false` disables it)

---

## Issue #5: Race Condition -- Queue Number

### Problem
`CitizenController.createAppointment()` line 205:
```java
int queueNumber = applicationRepository.countByCreatedAtDate(LocalDate.now()) + 1;
```
Two concurrent requests can get the same count, producing duplicate queue numbers.

### Fix
Use a PostgreSQL daily sequence. Reset daily via the scheduler.

### Related Code Files
| Action | File |
|--------|------|
| CREATE | `backend/src/main/resources/db/migration/V24__add_queue_number_sequence.sql` |
| MODIFY | `backend/src/main/java/com/example/demo/repository/ApplicationRepository.java` |
| MODIFY | `backend/src/main/java/com/example/demo/controller/CitizenController.java` |
| MODIFY | `backend/src/main/java/com/example/demo/service/ApplicationSchedulerService.java` |

### Implementation Steps

1. **Create migration `V24__add_queue_number_sequence.sql`**:
   ```sql
   -- Daily queue number sequence
   CREATE SEQUENCE IF NOT EXISTS queue_number_seq START WITH 1 INCREMENT BY 1;

   -- Set sequence to current max to avoid collision with existing data
   SELECT setval('queue_number_seq',
       COALESCE((SELECT MAX(queue_number) FROM application), 0) + 1,
       false
   );
   ```

2. **ApplicationRepository.java** -- Add native query for next queue number:
   ```java
   @Query(value = "SELECT nextval('queue_number_seq')", nativeQuery = true)
   int getNextQueueNumber();
   ```

3. **CitizenController.java** -- Replace count-based logic (line 205):
   ```java
   // BEFORE:
   int queueNumber = applicationRepository.countByCreatedAtDate(LocalDate.now()) + 1;

   // AFTER:
   int queueNumber = applicationRepository.getNextQueueNumber();
   ```

4. **ApplicationSchedulerService.java** -- Add daily sequence reset at midnight:
   ```java
   @Scheduled(cron = "0 0 0 * * *") // midnight
   @Transactional
   public void resetDailyQueueSequence() {
       applicationRepository.resetQueueSequence();
       log.info("Daily queue number sequence reset");
   }
   ```

5. **ApplicationRepository.java** -- Add reset method:
   ```java
   @Modifying
   @Query(value = "ALTER SEQUENCE queue_number_seq RESTART WITH 1", nativeQuery = true)
   void resetQueueSequence();
   ```

### Success Criteria
- [x] Queue numbers are unique per day
- [x] Concurrent requests get different numbers
- [x] Sequence resets at midnight

---

## Issue #6: Queue Not Filtered by Counter

### Problem
`getDashboard()` shows ALL queue items across all counters. Staff at counter A sees items for counter B.

Affected queries: `findActiveQueueHistories()`, `findApplicationsByAppointmentDateAndPhase()`, and stats queries.

### Fix
Add `counterId` parameter to all queue queries in the dashboard.

### Related Code Files
| Action | File |
|--------|------|
| MODIFY | `backend/src/main/java/com/example/demo/repository/ApplicationHistoryRepository.java` |
| MODIFY | `StaffQueueDashboardController.java` (after split) |

### Implementation Steps

1. **ApplicationHistoryRepository.java** -- Add counter-filtered variants:
   ```java
   @Query("SELECT h FROM ApplicationHistory h " +
       "WHERE h.appointmentDate = :date " +
       "AND h.phaseTo = :phase " +
       "AND h.application.currentPhase = :phase " +
       "AND h.counter.id = :counterId " +
       "AND h.id IN (SELECT MAX(h2.id) FROM ApplicationHistory h2 GROUP BY h2.application) " +
       "ORDER BY h.expectedTime ASC, h.application.queueNumber ASC")
   List<ApplicationHistory> findActiveQueueHistoriesByCounter(
       @Param("date") LocalDate date,
       @Param("phase") Integer phase,
       @Param("counterId") Integer counterId);

   @Query("SELECT DISTINCT h.application FROM ApplicationHistory h " +
       "WHERE h.appointmentDate = :date " +
       "AND h.application.currentPhase = :phase " +
       "AND h.counter.id = :counterId " +
       "ORDER BY h.application.queueNumber ASC")
   List<Application> findApplicationsByAppointmentDateAndPhaseAndCounter(
       @Param("date") LocalDate date,
       @Param("phase") Integer phase,
       @Param("counterId") Integer counterId);

   @Query("SELECT COUNT(DISTINCT h.application) FROM ApplicationHistory h " +
       "WHERE h.appointmentDate = :date " +
       "AND h.application.currentPhase = :phase " +
       "AND h.counter.id = :counterId")
   Long countByAppointmentDateAndPhaseAndCounter(
       @Param("date") LocalDate date,
       @Param("phase") Integer phase,
       @Param("counterId") Integer counterId);
   ```

2. **StaffQueueDashboardController.java** -- Use counter-filtered queries:
   ```java
   Integer counterId = staff.getCounter().getId();

   List<ApplicationHistory> waitingHistories =
       applicationHistoryRepository.findActiveQueueHistoriesByCounter(today, Application.PHASE_QUEUE, counterId);

   List<Application> pendingApps =
       applicationHistoryRepository.findApplicationsByAppointmentDateAndPhaseAndCounter(today, Application.PHASE_PENDING, counterId);
   // ... same for supplementApps, processingApps, stats
   ```

3. **getWaitingList()** -- Also needs counter filter. Add `Authentication` param:
   ```java
   @GetMapping("/waiting")
   public ResponseEntity<...> getWaitingList(Authentication authentication) {
       Staff staff = staffQueueService.getCurrentStaff(authentication);
       Integer counterId = staff.getCounter().getId();
       // use counter-filtered queries
   }
   ```

### Success Criteria
- [x] Dashboard only shows items for staff's assigned counter
- [x] Waiting list filtered by counter
- [x] Stats (completed, cancelled) filtered by counter

---

## Issue #7: Race Condition -- Call-Next

### Problem
Two staff at different counters can call `POST /api/staff/queue/call-next` simultaneously and both get the same application (same first item in the waiting list).

### Fix
Add pessimistic write lock to the query that fetches the oldest pending application.

### Related Code Files
| Action | File |
|--------|------|
| MODIFY | `backend/src/main/java/com/example/demo/repository/ApplicationRepository.java` |
| MODIFY | `StaffQueueActionController.java` (after split) |

### Implementation Steps

1. **ApplicationRepository.java** -- Add locked query:
   ```java
   @Lock(LockModeType.PESSIMISTIC_WRITE)
   @Query("SELECT a FROM Application a WHERE a.currentPhase = :phase " +
       "AND a.id IN (SELECT DISTINCT h.application.id FROM ApplicationHistory h " +
       "WHERE h.appointmentDate = :date AND h.counter.id = :counterId) " +
       "ORDER BY a.queueNumber ASC")
   List<Application> findOldestPendingForCounter(
       @Param("date") LocalDate date,
       @Param("phase") Integer phase,
       @Param("counterId") Integer counterId);
   ```

2. **StaffQueueActionController.callNext()** -- Use locked query for auto-select:
   ```java
   // BEFORE:
   List<Application> waitingApps = applicationHistoryRepository
       .findApplicationsByAppointmentDateAndPhase(today, Application.PHASE_QUEUE);
   nextApp = waitingApps.get(0);

   // AFTER:
   List<Application> waitingApps = applicationRepository
       .findOldestPendingForCounter(today, Application.PHASE_QUEUE, counterId);
   if (waitingApps.isEmpty()) {
       return ResponseEntity.ok(ApiResponse.success(null, "Khong con ai trong hang cho"));
   }
   nextApp = waitingApps.get(0);
   ```

3. The `@Transactional` on the controller ensures the lock is held for the duration of the request.

### Success Criteria
- [x] Two concurrent call-next requests get different applications
- [x] Lock released after transaction commit

---

## Issue #8: Slot Capacity Logic Wrong

### Problem
`CitizenController.getAvailableSlots()` line 111:
```java
slot.put("available", bookedTimes.contains(slotTime) ? 0 : 5);
```
If 1 booking exists for a slot, `bookedTimes.contains()` returns true and shows 0 available. But max capacity is 5 -- we waste 4 slots.

### Fix
Count bookings per slot time; available = max(0, 5 - count).

### Related Code Files
| Action | File |
|--------|------|
| MODIFY | `backend/src/main/java/com/example/demo/repository/AppointmentRepository.java` |
| MODIFY | `backend/src/main/java/com/example/demo/controller/CitizenController.java` |

### Implementation Steps

1. **AppointmentRepository.java** -- Add count query:
   ```java
   @Query("SELECT a.appointmentTime, COUNT(a) FROM Appointment a " +
       "WHERE a.appointmentDate = :date AND a.status = 0 " +
       "GROUP BY a.appointmentTime")
   List<Object[]> countBookingsByDateGroupedByTime(@Param("date") LocalDate date);
   ```

2. **CitizenController.getAvailableSlots()** -- Replace contains check:
   ```java
   // Build a map of time -> booked count
   List<Object[]> bookingCounts = appointmentRepository.countBookingsByDateGroupedByTime(targetDate);
   Map<LocalTime, Long> bookedMap = new HashMap<>();
   for (Object[] row : bookingCounts) {
       bookedMap.put((LocalTime) row[0], (Long) row[1]);
   }

   // In slot loop:
   long booked = bookedMap.getOrDefault(slotTime, 0L);
   int available = Math.max(0, 5 - (int) booked);
   slot.put("available", available);
   slot.put("booked", (int) booked);
   slot.put("maxCapacity", 5);
   ```

3. **Also fix StaffQueueController getSlots()** -- same issue with `bookedTimes.contains()`. After split this is in `StaffQueueDashboardController.java`. Use same count-based approach.

### Success Criteria
- [x] Slot with 1 booking shows 4 available (not 0)
- [x] Slot with 5 bookings shows 0 available
- [x] Both citizen and staff slot endpoints fixed

---

## Issue #9: Supplement Sets Wrong Phase

### Problem
`StaffQueueController.supplement()` line 496:
```java
app.setCurrentPhase(Application.PHASE_QUEUE);  // WRONG
```
Should be `PHASE_SUPPLEMENT`. The citizen needs to come back with additional documents; they are not in the active queue.

Also the history `phaseTo` (line 505) is `PHASE_QUEUE` -- should be `PHASE_SUPPLEMENT`.

### Fix
One-line change (after split, in `StaffQueueActionController.java`).

### Implementation Steps

1. **StaffQueueActionController.supplement()**:
   ```java
   // BEFORE:
   app.setCurrentPhase(Application.PHASE_QUEUE);
   // history: .phaseTo(Application.PHASE_QUEUE)

   // AFTER:
   app.setCurrentPhase(Application.PHASE_SUPPLEMENT);
   // history: .phaseTo(Application.PHASE_SUPPLEMENT)
   ```

### Success Criteria
- [x] supplement() sets phase to PHASE_SUPPLEMENT (6), not PHASE_QUEUE (1)
- [x] History record shows correct phaseTo

---

## Issue #10: Add Pagination

### Problem
`StaffHoSoController.getDashboard()` and `getList()` use `applicationRepository.findAll()` -- loads ALL records into memory. Will fail at scale.

### Fix
Add `Pageable` to list endpoints. Dashboard stats use COUNT queries instead of loading all entities.

### Related Code Files
| Action | File |
|--------|------|
| MODIFY | `backend/src/main/java/com/example/demo/controller/StaffHoSoController.java` |
| MODIFY | `backend/src/main/java/com/example/demo/repository/ApplicationRepository.java` |

### Implementation Steps

1. **ApplicationRepository.java** -- Add paginated + filtered queries:
   ```java
   Page<Application> findByCurrentPhase(Integer phase, Pageable pageable);

   @Query("SELECT a FROM Application a ORDER BY a.createdAt DESC")
   Page<Application> findAllPaged(Pageable pageable);

   // Stats queries (avoid loading all entities)
   @Query("SELECT a.currentPhase, COUNT(a) FROM Application a GROUP BY a.currentPhase")
   List<Object[]> countGroupByPhase();

   @Query("SELECT COUNT(a) FROM Application a WHERE a.currentPhase IN (1,2,3,5) AND a.deadline < :today")
   long countOverdue(@Param("today") LocalDate today);
   ```

2. **StaffHoSoController.getDashboard()** -- Use COUNT queries:
   ```java
   @GetMapping("/dashboard")
   public ResponseEntity<ApiResponse<HoSoResponse.DashboardData>> getDashboard() {
       List<Object[]> phaseCounts = applicationRepository.countGroupByPhase();
       Map<Integer, Long> countMap = new HashMap<>();
       for (Object[] row : phaseCounts) {
           countMap.put((Integer) row[0], (Long) row[1]);
       }

       long choXuLy = countMap.getOrDefault(Application.PHASE_QUEUE, 0L)
                    + countMap.getOrDefault(Application.PHASE_PENDING, 0L);
       long dangXuLy = countMap.getOrDefault(Application.PHASE_PROCESSING, 0L)
                     + countMap.getOrDefault(Application.PHASE_RECEIVED, 0L);
       long hoanThanh = countMap.getOrDefault(Application.PHASE_COMPLETED, 0L);
       long treHan = applicationRepository.countOverdue(LocalDate.now());

       long total = countMap.values().stream().mapToLong(Long::longValue).sum();

       // build response...
   }
   ```

3. **StaffHoSoController.getList()** -- Add pagination params:
   ```java
   @GetMapping
   public ResponseEntity<ApiResponse<Page<HoSoResponse>>> getList(
           @RequestParam(required = false) Integer trangThai,
           @RequestParam(defaultValue = "0") int page,
           @RequestParam(defaultValue = "20") int size) {

       Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
       Page<Application> apps;

       if (trangThai != null) {
           int phase = mapTrangThaiToPhase(trangThai);
           apps = applicationRepository.findByCurrentPhase(phase, pageable);
       } else {
           apps = applicationRepository.findAllPaged(pageable);
       }

       Page<HoSoResponse> responses = apps.map(this::mapToHoSoResponse);
       return ResponseEntity.ok(ApiResponse.success(responses));
   }
   ```

4. **CitizenController** -- Also has findAll() in `getAppointmentById()` line 392. Replace with a count query:
   ```java
   // BEFORE (line 392):
   queuePosition = (int) applicationRepository.findAll().stream()
       .filter(a -> ...).count();

   // AFTER -- add repository method:
   @Query("SELECT COUNT(a) FROM Application a WHERE a.currentPhase IN (1,2) " +
       "AND CAST(a.createdAt AS LocalDate) = :today " +
       "AND a.queueNumber < :queueNumber")
   int countPeopleAhead(@Param("today") LocalDate today, @Param("queueNumber") Integer queueNumber);

   // Use in controller:
   queuePosition = applicationRepository.countPeopleAhead(LocalDate.now(), app.getQueueNumber());
   ```

### Success Criteria
- [x] getList() returns paginated results
- [x] getDashboard() uses COUNT queries, not findAll()
- [x] CitizenController queue position uses count query
- [x] Default page size = 20

---

## Todo List (All Issues)
- [x] Pre-work: Split StaffQueueController into 3 files
- [x] Issue #4: Create ApplicationSchedulerService with @Scheduled auto-cancel
- [x] Issue #4: Remove auto-cancel from getDashboard()
- [x] Issue #4: Add @EnableScheduling to DemoApplication
- [x] Issue #5: Create V24 migration for queue_number_seq
- [x] Issue #5: Add getNextQueueNumber() to ApplicationRepository
- [x] Issue #5: Update CitizenController to use sequence
- [x] Issue #5: Add daily sequence reset to scheduler
- [x] Issue #6: Add counter-filtered queries to ApplicationHistoryRepository
- [x] Issue #6: Update dashboard + waiting list to filter by counter
- [x] Issue #7: Add pessimistic lock query to ApplicationRepository
- [x] Issue #7: Update callNext() to use locked query
- [x] Issue #8: Add countBookingsByDateGroupedByTime() to AppointmentRepository
- [x] Issue #8: Fix available slot calculation in CitizenController + StaffQueueDashboardController
- [x] Issue #9: Fix supplement() phase to PHASE_SUPPLEMENT
- [x] Issue #10: Add pagination queries to ApplicationRepository
- [x] Issue #10: Refactor StaffHoSoController dashboard to use COUNT queries
- [x] Issue #10: Add Pageable to StaffHoSoController.getList()
- [x] Issue #10: Replace findAll() in CitizenController queue position
- [x] Full compile + test

## Risk Assessment
- **Controller split**: Renaming introduces risk of broken imports. Compile check after each step.
- **Pessimistic lock**: Could cause contention under high load. Acceptable for government counter system (low concurrency per counter).
- **Sequence reset at midnight**: If app restarts after midnight but before reset runs, sequence continues from last value. The midnight cron handles it. Edge case: if two apps deployed simultaneously, sequence may double-reset. Harmless.
- **Pagination**: Frontend must handle paginated responses. AdminStaff Vue app may need updates to pass page/size params.

## Next Steps
- After both phases complete, run `mvn compile` and `mvn test`
- Deploy frontend + backend together (breaking API changes in Phase 1 Issue #2)
- Update AdminStaff Vue frontend if it calls any changed endpoints
- Consider adding integration tests for race condition scenarios
