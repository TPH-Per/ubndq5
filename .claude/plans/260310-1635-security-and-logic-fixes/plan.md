---
title: "Security & Logic Bug Fixes"
description: "Fix critical security vulnerabilities (Phase 0) and logic bugs (Phase 1) in the backend"
status: completed
priority: P1
effort: 8h
branch: main
tags: [security, bugfix, backend, critical]
created: 2026-03-10
completed: 2026-03-10
---

# Security & Logic Bug Fixes

## Overview

Fix 3 critical security vulnerabilities and 7 logic bugs in the Spring Boot backend. Security fixes are blocking; logic fixes are high priority.

## Phases

| # | Phase | Issues | Status | Effort |
|---|-------|--------|--------|--------|
| 1 | [Security Fixes](./phase-01-security-fixes.md) | #1 DevTool removal, #2 CCCD in URL, #3 JWT hardcoded | Completed | 3h |
| 2 | [Logic Bug Fixes](./phase-02-logic-bug-fixes.md) | #4-#10: auto-cancel, race conditions, queue filter, slots, supplement, pagination | Completed | 5h |

## Dependencies

- Phase 1 (security) must complete before Phase 2
- Migration files: V24+ (V1-V23 exist)
- StaffQueueController.java (592 LOC) must be split during Phase 2

## Key Files

**Backend (modify)**
- `controller/DevToolController.java` -- DELETE
- `controller/StaffQueueController.java` -- refactor + split
- `controller/CitizenController.java` -- CCCD endpoint change
- `controller/StaffController.java` -- already has reset-password (no change needed)
- `security/JwtUtils.java` -- env var for secret
- `config/SecurityConfig.java` -- remove /api/dev/** permitAll
- `repository/ApplicationRepository.java` -- add sequence query, pessimistic lock
- `repository/AppointmentRepository.java` -- add countByDateAndTime

**Frontend (modify)**
- `client/src/services/citizenApi.ts` -- POST for appointments lookup

**Config (modify)**
- `application.properties` -- remove jwt.secret, add scheduler config
- `.env.example` -- already has JWT_SECRET (update comment)
- `docker-compose.prod.yml` -- pass JWT_SECRET env var

**New files**
- `service/ApplicationSchedulerService.java` -- scheduled auto-cancel job
- `controller/StaffQueueDashboardController.java` -- extracted from StaffQueueController
- `controller/StaffQueueActionController.java` -- extracted from StaffQueueController
- `db/migration/V24__add_queue_number_sequence.sql`

## Risk Assessment

- **JWT secret change**: Existing tokens invalidated on deploy. Acceptable for alpha.
- **CCCD endpoint change**: Breaking change for client. Must deploy frontend + backend together.
- **Sequence migration**: Must handle existing data; sequence start = MAX(queue_number) + 1.
