# Code Review: Security & Logic Bug Fixes
**Date:** 2026-03-10 | **Branch:** main | **Reviewer:** code-reviewer agent

---

## Scope
- Phase 1 (Security): DevToolController removal, SecurityConfig, JwtUtils, RateLimitInterceptor, CitizenAppointmentController, CitizenApplicationController
- Phase 2 (Logic): StaffQueueActionController, StaffQueueDashboardController, StaffQueueService, ApplicationSchedulerService, ApplicationRepository, ApplicationHistoryRepository, AppointmentRepository, V24 migration

---

## Overall Assessment

The security and logic changes are **substantially correct** and represent a major improvement. The critical DevTool backdoor is gone, CCCD no longer leaks via URL/logs, the queue race condition is properly addressed with a PostgreSQL sequence, and the auto-cancel side-effect in a GET handler is fixed. Three issues warrant prompt attention: a CCCD enumeration risk, a race condition window in the pessimistic lock path, and the rate limiter's unbounded memory map.

---

## Critical Issues

### 1. CCCD Enumeration via Timing Attack — CitizenApplicationController / CitizenAppointmentController

`getApplicationDetail()` throws `RuntimeException("Không tìm thấy hồ sơ")` (404-equivalent) if the ID doesn't exist, but returns `400 UNAUTHORIZED` if the CCCD doesn't match. An attacker can enumerate valid application IDs by observing which response code they get without knowing any CCCD. Once an ID is confirmed valid, they can brute-force a 12-digit CCCD (though impractical, the differentiation of 404 vs 400 is still an information leak).

Fix: return the same generic 404 for both "not found" and "wrong CCCD" cases on the view/cancel endpoints. Never reveal whether the resource exists to an unauthenticated caller.

```java
// Before (leaks existence):
if (app == null) return 404 NOT_FOUND
if (!app.getCitizenCccd().equals(cccd)) return 400 UNAUTHORIZED

// After (uniform response):
if (app == null || !app.getCitizenCccd().equals(cccd))
    return 404 NOT_FOUND with generic message
```

Affected files:
- `CitizenAppointmentController.java` lines 267–273 (`cancelAppointment`), 322–331 (`getAppointmentById`)
- `CitizenApplicationController.java` lines 97–103 (`getApplicationDetail`), 138–143 (`getApplicationHistory`)

---

### 2. Pessimistic Lock Not Scoped to Transaction Boundary — StaffQueueActionController

`StaffQueueActionController` is annotated `@Transactional` at class level. However, the check for `currentProcessingList` (lines 61–68) and the `findOldestPendingForCounter` lock acquisition (line 87) happen in the **same transaction**, which is correct in isolation. The problem: `findOldestPendingForCounter` only locks rows that already match `PHASE_QUEUE` at the time of the query. If two staff members call `/call-next` simultaneously and neither has a currently processing application (the guard check at line 65 passes for both), they may both pass the guard before either acquires the lock, because `currentProcessingList` is read without a lock and both reads can see the same empty state. The pessimistic lock on the `findOldestPendingForCounter` query will then correctly serialize the actual row acquisition, so the second caller will get the next item in queue. This is mostly safe — the lock on the application row prevents double-calling the same number — but the guard check itself is not locked, so in theory both callers see "no processing" and proceed, which is a benign double-call scenario only if there are at least 2 queued items. If there is only 1 item, one caller gets it and the other gets an empty list response. This is acceptable but document the known gap.

More actionable: the `@Lock` annotation on a `List<Application>` query **requires** the repository call to be inside an active `@Transactional` context with the correct propagation. Since `StaffQueueActionController` is `@Transactional` at class level, this is satisfied. Confirm that `StaffQueueService.getCurrentStaff()` (called before the lock query) does not start a new transaction that could leave the lock scope prematurely — it does not annotate with `@Transactional`, so the outer transaction is maintained. This is fine.

**Net verdict:** The locking approach is architecturally sound. Low risk.

---

### 3. Unbounded Memory in RateLimitInterceptor — CorsConfig / RateLimitInterceptor

`RateLimitInterceptor` holds a `ConcurrentHashMap<String, long[]> requestCounts` that is never pruned. Entries for IPs whose window has expired are only cleaned when that IP makes a new request. Under sustained load from many unique IPs (e.g., a distributed attack), this map grows without bound and can cause OOM or GC pressure.

Fix: add periodic cleanup. Since this is a simple interceptor without Spring lifecycle, a basic approach is to prune stale entries inline — e.g., on every N-th request, iterate and remove entries where `now - entry[1] > WINDOW_MILLIS`.

Alternatively, replace with a `Caffeine` cache with `expireAfterWrite(1, MINUTES)` which handles eviction automatically.

---

## High Priority

### 4. DB Credentials in application.properties (Plaintext)

`application.properties` contains:
```
spring.datasource.username=myuser
spring.datasource.password=secret
```

These are hardcoded, not env-var-resolved like JWT. Apply the same pattern used for JWT:
```
spring.datasource.password=${DB_PASSWORD:secret}
```

And document in `.env.example`. At minimum, the password `secret` in a government service repo should not be the actual production value — confirm this is dev-only.

---

### 5. Missing Input Validation on `createAppointment` — CitizenAppointmentController (line 122–123)

`LocalDate.parse(appointmentDateStr)` and `LocalTime.parse(appointmentTimeStr)` will throw `DateTimeParseException` if malformed, which propagates as a 500. There is no try/catch and no constraint that `appointmentDate` is in the future. A citizen can book appointments in the past without restriction.

Fix: wrap in try/catch with a 400 response, and validate `appointmentDate.isAfter(LocalDate.now().minusDays(1))`.

---

### 6. `cancelAppointment` Allows Cancellation of PHASE_SUPPLEMENT Applications

`cancelAppointment()` (line 275–279) only allows cancellation for `PHASE_PENDING` or `PHASE_QUEUE`. A supplement-scheduled application (`PHASE_SUPPLEMENT`) cannot be self-cancelled by the citizen. This may be intentional but should be documented — if a citizen books a supplement appointment and wants to cancel, they have no self-service path.

---

### 7. Auto-Cancel Scheduler Does Not Check for PHASE_SUPPLEMENT

`autoCancelLateApplications()` in `ApplicationSchedulerService` queries `findActiveQueueHistories(today, PHASE_QUEUE)` — only phase QUEUE. Supplement appointments (phase 6) that are overdue are never auto-cancelled. This may be intentional (staff-scheduled supplements need human review) but is undocumented.

---

## Medium Priority

### 8. `getNextQueueNumber()` Sequence Is Not Day-Aware

The PostgreSQL sequence in `V24` is a single global counter reset by the cron job at midnight. If the reset job fails (exception, scheduler downtime, restart), the sequence will not reset and queue numbers will carry over from the previous day. The `applicationCode` would then be `HS-20260310-xxx` with numbers continuing from the prior day, creating duplicate-looking codes across days (same number, different date component).

Consider adding a fallback: if `getNextQueueNumber()` returns a suspiciously high value (e.g., > 999), log a warning so ops can investigate.

### 9. `callNext` Falls Back to Global Queue When Staff Has No Counter

In `StaffQueueActionController.callNext()` (lines 86–88): when `counterId == null`, the fallback uses `findByCurrentPhaseOrderByQueueNumberAsc` (global, no lock). If a staff member has no counter assigned, they can call any application from any counter — bypassing the counter-scoped filtering. The dashboard already returns 400 for staff with no counter, but `callNext` does not. Apply the same guard:

```java
if (counterId == null) {
    return ResponseEntity.badRequest()
        .body(ApiResponse.error("NO_COUNTER", "Bạn chưa được phân công quầy"));
}
```

### 10. `@Transactional(readOnly = true)` on CitizenApplicationController, But Class-Level

`CitizenApplicationController` is annotated `@Transactional(readOnly = true)` at class level. This is correct for the read endpoints. However, if any write method is added in the future without an explicit `@Transactional`, it will silently run in read-only mode and fail at flush time. Low risk now, but fragile design.

### 11. `searchAppointments` Loads All Applications by CCCD Into Memory

`applicationRepository.findByCitizenCccd(cccd)` returns all applications for a CCCD then filters in Java (lines 218–228). For a frequent user with many past applications this is N+1 in history queries (one per app, line 239). Apply JPA filtering + pagination at DB level.

### 12. `supplement()` Does Not Check Application Phase

`supplement()` in `StaffQueueActionController` (line 254) does not validate that the application is in a cancellable/supplementable phase. Any application — including already COMPLETED or CANCELLED ones — can be supplemented if staff provides a valid ID. Add a phase guard.

---

## Low Priority

### 13. Magic Number `status = 0` in AppointmentRepository Queries

```java
@Query("... AND a.status = 0")
```

`0` is not a named constant. If `Appointment.STATUS_SCHEDULED` changes value, the JPQL query silently breaks. Use a named constant or a `@Where` clause on the entity.

### 14. JWT Signing Key Double-Encodes in JwtUtils

`getSigningKey()` (line 65–67) does:
```java
Decoders.BASE64.decode(Base64.getEncoder().encodeToString(jwtSecret.getBytes()))
```

This base64-encodes the raw secret string, then immediately base64-decodes it — a no-op roundtrip. The actual key material is just `jwtSecret.getBytes()`. This is functional but misleading. Consider using `Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8))` directly if the secret is stored as raw UTF-8, or `Decoders.BASE64.decode(jwtSecret)` if stored as base64.

### 15. `show-sql=true` in Production Properties

`spring.jpa.show-sql=true` and `hibernate.format_sql=true` will print all SQL to stdout in production. This can expose PII (CCCD, names) in logs and reduce performance. Set to `false` or move to a dev profile.

---

## Positive Observations

1. **DevToolController deleted** — critical unauthenticated password reset backdoor is fully removed. Excellent.
2. **CCCD moved from URL/query param to POST body** — correct approach; CCCD will no longer appear in access logs, Nginx logs, or browser history.
3. **PostgreSQL SEQUENCE for queue numbers** — atomic, correct solution. `V24` migration seeds from `MAX(queue_number)` to avoid collisions on existing data. Well done.
4. **Pessimistic write lock on `findOldestPendingForCounter`** — architecturally correct placement. The `@Lock(PESSIMISTIC_WRITE)` on the JPA query, inside a `@Transactional` controller, is the right tool.
5. **Auto-cancel extracted to `@Scheduled` service** — eliminating the write side-effect from a GET handler is a textbook correctness fix.
6. **Issue #9 supplement fix** — `PHASE_SUPPLEMENT (6)` correctly used instead of `PHASE_QUEUE (1)`. Supplement appointments no longer re-enter the regular queue.
7. **Counter-scoped queue filtering** — all dashboard and waiting-list queries now filter by `counterId`, preventing cross-counter data leakage between staff members.
8. **Rate limiter registered correctly** — `CorsConfig.addInterceptors()` registers `RateLimitInterceptor` on `/api/citizen/**` only. Scope is appropriate.
9. **`@PostConstruct` JWT validation** — warns on startup if default secret is used. Forces operators to notice misconfiguration early.
10. **`app.auto-cancel.enabled` toggle** — configurable kill switch for the scheduler without code changes.
11. **StaffQueueController split** — 592-LOC monolith correctly split into focused files, each under 200 LOC.

---

## Recommended Actions (Prioritized)

1. **[Critical]** Normalize 404/403 responses in citizen view/cancel endpoints — do not differentiate "not found" from "wrong CCCD".
2. **[High]** Move `spring.datasource.password` to env-var resolution.
3. **[High]** Add `try/catch` for date/time parsing in `createAppointment`; validate date is in the future.
4. **[High]** Add no-counter guard to `callNext()` (consistent with `getDashboard()`).
5. **[Medium]** Add pruning to `RateLimitInterceptor.requestCounts` map.
6. **[Medium]** Add phase guard to `supplement()` endpoint.
7. **[Medium]** Replace `status = 0` magic number with named constant in `AppointmentRepository`.
8. **[Low]** Set `show-sql=false` in production config; move to dev profile.
9. **[Low]** Fix misleading double-encode in `JwtUtils.getSigningKey()`.

---

## Unresolved Questions

1. Is cancellation of `PHASE_SUPPLEMENT` applications by citizens intentionally blocked, or an oversight?
2. Is `show-sql=true` intentional (developer machine config committed by mistake)?
3. Does the slot capacity check in `createAppointment` enforce the 5-slot limit at booking time, or is it advisory only? Currently no check prevents booking a full slot — `countBookedByDateAndTime` is only displayed, not enforced as a hard constraint in the booking path.
4. Is `spring.datasource.password=secret` a dev-only placeholder or does it match the actual deployment credential?
