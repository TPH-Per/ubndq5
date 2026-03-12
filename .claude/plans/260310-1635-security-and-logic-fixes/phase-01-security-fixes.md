# Phase 1 -- Security Fixes (CRITICAL)

## Context Links
- [plan.md](./plan.md)
- [SecurityConfig.java](../../backend/src/main/java/com/example/demo/config/SecurityConfig.java)
- [DevToolController.java](../../backend/src/main/java/com/example/demo/controller/DevToolController.java)
- [JwtUtils.java](../../backend/src/main/java/com/example/demo/security/JwtUtils.java)
- [CitizenController.java](../../backend/src/main/java/com/example/demo/controller/CitizenController.java)
- [citizenApi.ts](../../client/src/services/citizenApi.ts)

## Overview
- **Priority**: P0 -- CRITICAL
- **Status**: Completed
- **Effort**: ~3h
- **Description**: 3 security vulnerabilities: unauthenticated dev endpoints, PII in URL query params, hardcoded JWT secret

## Key Insights
- `DevToolController.java` has 4 completely public endpoints that can reset ANY password without auth
- `SecurityConfig.java` line 60 explicitly permits `/api/dev/**`
- `StaffController.java` already has `POST /api/admin/staff/{id}/reset-password` with `@PreAuthorize("hasRole('Admin')")` -- so DevToolController is entirely redundant
- CCCD (national ID, 12 digits) passed as GET query param -- logged in access logs, browser history, proxy caches
- JWT secret hardcoded in `application.properties` line 60 AND in `.env.example` line 20

---

## Issue #1: Remove DevToolController

### Requirements
- DELETE `DevToolController.java` entirely
- Remove `/api/dev/**` from SecurityConfig permitAll list
- Verify StaffController already covers admin reset-password (it does at `/api/admin/staff/{id}/reset-password`)

### Architecture
No new architecture needed. StaffController already has the secured equivalent.

### Related Code Files
| Action | File |
|--------|------|
| DELETE | `backend/src/main/java/com/example/demo/controller/DevToolController.java` |
| MODIFY | `backend/src/main/java/com/example/demo/config/SecurityConfig.java` |

### Implementation Steps

1. **Delete DevToolController.java**
   ```
   rm backend/src/main/java/com/example/demo/controller/DevToolController.java
   ```

2. **Update SecurityConfig.java** -- Remove `/api/dev/**` from permitAll:
   ```java
   // BEFORE (line 58-65):
   .requestMatchers(
       "/api/auth/**",
       "/api/dev/**",      // <-- REMOVE THIS LINE
       "/api/public/**",
       "/api/citizen/**",
       "/error",
       "/actuator/health"
   ).permitAll()

   // AFTER:
   .requestMatchers(
       "/api/auth/**",
       "/api/public/**",
       "/api/citizen/**",
       "/error",
       "/actuator/health"
   ).permitAll()
   ```

3. **Compile check** -- run `mvn compile` to verify no references to DevToolController exist

### Success Criteria
- [x] DevToolController.java deleted
- [x] `/api/dev/**` removed from SecurityConfig
- [x] `mvn compile` passes
- [x] `curl POST /api/dev/reset-password` returns 401/403

---

## Issue #2: CCCD in URL Query Param

### Requirements
- Change `GET /api/citizen/appointments?cccd=X` to `POST /api/citizen/appointments/search` with CCCD in request body
- Update `citizenApi.ts` client to use POST
- Keep existing `POST /api/citizen/appointments` (create appointment) unchanged
- Add basic rate limiting to prevent CCCD enumeration

### Architecture

**Endpoint change:**
```
BEFORE: GET  /api/citizen/appointments?cccd=012345678901&status=UPCOMING
AFTER:  POST /api/citizen/appointments/search  { "cccd": "012345678901", "status": "UPCOMING" }
```

Also affected (CCCD in query param):
- `GET /api/citizen/appointments/{id}?cccd=X` -- change to POST body or keep (path param is less exposed than query, but still in URL). Decision: move CCCD to request body via `POST /api/citizen/appointments/{id}/view`
- `POST /api/citizen/appointments/{id}/cancel?cccd=X` -- already POST, move cccd from query to body
- `GET /api/citizen/applications?cccd=X` -- same pattern, change to `POST /api/citizen/applications/search`
- `GET /api/citizen/applications/{id}?cccd=X` -- change to `POST /api/citizen/applications/{id}/view`
- `GET /api/citizen/applications/{id}/history?cccd=X` -- change to `POST /api/citizen/applications/{id}/history`
- `GET /api/citizen/reports?cccd=X` -- change to `POST /api/citizen/reports/search`

### Related Code Files
| Action | File |
|--------|------|
| MODIFY | `backend/src/main/java/com/example/demo/controller/CitizenController.java` |
| MODIFY | `client/src/services/citizenApi.ts` |

### Implementation Steps

1. **CitizenController.java** -- Change all CCCD-in-query endpoints:

   a. `getAppointments()` (line 267-301):
   ```java
   // BEFORE:
   @GetMapping("/appointments")
   public ResponseEntity<...> getAppointments(@RequestParam String cccd, ...)

   // AFTER:
   @PostMapping("/appointments/search")
   public ResponseEntity<...> searchAppointments(@RequestBody Map<String, String> body) {
       String cccd = body.get("cccd");
       String status = body.get("status");
       if (cccd == null || !cccd.matches("\\d{12}")) {
           return ResponseEntity.badRequest()
               .body(ApiResponse.error("INVALID_CCCD", "CCCD khong hop le"));
       }
       // ... rest same
   ```

   b. Apply same pattern to: `getAppointmentById`, `cancelAppointment`, `getApplications`, `getApplicationDetail`, `getApplicationHistory`, `getReports`

   c. For cancel: move cccd from `@RequestParam` to `@RequestBody`:
   ```java
   // BEFORE:
   @PostMapping("/appointments/{id}/cancel")
   public ResponseEntity<...> cancelAppointment(@PathVariable Integer id, @RequestParam String cccd)

   // AFTER:
   @PostMapping("/appointments/{id}/cancel")
   public ResponseEntity<...> cancelAppointment(@PathVariable Integer id, @RequestBody Map<String, String> body) {
       String cccd = body.get("cccd");
   ```

2. **citizenApi.ts** -- Update all affected functions:
   ```typescript
   // BEFORE:
   export async function getMyAppointments(cccd: string, status?: string) {
       let params = `?cccd=${cccd}`;
       return fetchAPI<...>(`/appointments${params}`);
   }

   // AFTER:
   export async function getMyAppointments(cccd: string, status?: string) {
       return fetchAPI<...>('/appointments/search', {
           method: 'POST',
           body: JSON.stringify({ cccd, status }),
       });
   }
   ```

   Similarly update: `cancelAppointment`, `getAppointmentDetail`, `getMyApplications`, `getApplicationDetail`, `getApplicationHistory`, `getMyFeedbacks`

3. **Rate limiting** -- Add simple in-memory rate limiter:
   - Create `backend/src/main/java/com/example/demo/config/RateLimitInterceptor.java`
   - Use `ConcurrentHashMap<String, AtomicInteger>` keyed by IP
   - Limit: 30 requests/minute per IP for `/api/citizen/**`
   - Register in a `WebMvcConfigurer` bean
   - Keep under 80 lines total

### Success Criteria
- [x] No CCCD appears in any GET query param
- [x] All citizen search endpoints use POST with body
- [x] citizenApi.ts updated to match
- [x] Rate limiter active on citizen endpoints
- [x] Frontend still works end-to-end

---

## Issue #3: JWT Secret Hardcoded

### Requirements
- Remove hardcoded `jwt.secret` from `application.properties`
- Read from environment variable `JWT_SECRET`
- Fail fast on startup if `JWT_SECRET` not set
- Update `.env.example` and `docker-compose.prod.yml`

### Related Code Files
| Action | File |
|--------|------|
| MODIFY | `backend/src/main/resources/application.properties` |
| MODIFY | `backend/src/main/java/com/example/demo/security/JwtUtils.java` |
| MODIFY | `docker-compose.prod.yml` |
| MODIFY | `.env.example` |

### Implementation Steps

1. **application.properties** -- Replace hardcoded secret with env var reference:
   ```properties
   # BEFORE (line 60):
   jwt.secret=MySecretKeyForJWTTokenGeneration2026VerySecure!@#$

   # AFTER:
   jwt.secret=${JWT_SECRET}
   ```
   Spring Boot resolves `${JWT_SECRET}` from environment automatically. No need to use `System.getenv()` in JwtUtils -- `@Value("${jwt.secret}")` already works.

2. **JwtUtils.java** -- Add `@PostConstruct` startup validation:
   ```java
   import jakarta.annotation.PostConstruct;

   @PostConstruct
   void validateSecret() {
       if (jwtSecret == null || jwtSecret.isBlank() || jwtSecret.length() < 32) {
           throw new IllegalStateException(
               "JWT_SECRET env var must be set and at least 32 characters");
       }
   }
   ```

3. **docker-compose.prod.yml** -- Add JWT_SECRET to backend environment (line ~53):
   ```yaml
   environment:
     - SPRING_PROFILES_ACTIVE=prod
     - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/cholon_db
     - SPRING_DATASOURCE_USERNAME=postgres
     - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD:-secret}
     - JWT_SECRET=${JWT_SECRET}
     - JAVA_OPTS=-Xms1g -Xmx3g -XX:+UseG1GC -XX:MaxGCPauseMillis=200
   ```

4. **.env.example** -- Update comment to clarify it is required:
   ```properties
   # JWT Configuration (REQUIRED - app will not start without this)
   # Must be at least 32 characters. Generate with: openssl rand -base64 48
   JWT_SECRET=CHANGE_ME_generate_with_openssl_rand_base64_48
   ```

5. **Local dev** -- For local development, set env var before running:
   ```bash
   export JWT_SECRET="MySecretKeyForJWTTokenGeneration2026VerySecure!@#$"
   mvn spring-boot:run
   ```
   Or add to IDE run configuration.

### Success Criteria
- [x] No hardcoded secret in application.properties
- [x] App fails to start without JWT_SECRET env var
- [x] App starts successfully with JWT_SECRET set
- [x] docker-compose.prod.yml passes JWT_SECRET
- [x] .env.example updated with placeholder

---

## Todo List
- [x] Issue #1: Delete DevToolController.java
- [x] Issue #1: Remove /api/dev/** from SecurityConfig
- [x] Issue #1: Compile check
- [x] Issue #2: Change citizen endpoints to POST with body
- [x] Issue #2: Update citizenApi.ts
- [x] Issue #2: Add rate limiter
- [x] Issue #3: Remove hardcoded JWT secret from application.properties
- [x] Issue #3: Add @PostConstruct validation in JwtUtils
- [x] Issue #3: Update docker-compose.prod.yml
- [x] Issue #3: Update .env.example
- [x] Run full compile + manual test

## Risk Assessment
- **Breaking frontend**: Issue #2 changes API contracts. Frontend and backend must deploy together.
- **JWT invalidation**: Changing the secret value invalidates all existing tokens. Users must re-login. Acceptable for alpha.
- **Rate limiter false positives**: Shared IP (office NAT) could hit limit. 30 req/min is generous enough.

## Security Considerations
- DevToolController removal eliminates unauthenticated password reset attack vector
- CCCD no longer in server access logs, browser history, proxy caches
- JWT secret rotation possible via env var change + restart
