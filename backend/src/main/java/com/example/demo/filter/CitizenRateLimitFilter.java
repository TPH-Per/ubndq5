package com.example.demo.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Rate limiting filter for all /api/citizen/** endpoints.
 *
 * Protects against:
 * - Brute-force CCCD enumeration via /reports/search
 * - Appointment spam via POST /appointments
 * - Feedback spam via POST /reports
 *
 * Strategy: fixed 1-minute window per IP + endpoint bucket.
 * Keys are naturally evicted after their minute window passes.
 */
@Component
@Order(10)
@Slf4j
public class CitizenRateLimitFilter extends OncePerRequestFilter {

    // key = "IP|bucket|minuteEpoch" → request count in that window
    private final ConcurrentHashMap<String, AtomicInteger> counters = new ConcurrentHashMap<>();
    private final AtomicLong lastCleanupMinute = new AtomicLong(0);

    // Limits per IP per minute
    private static final int LIMIT_BOOKING      = 5;   // POST /appointments (new booking)
    private static final int LIMIT_CANCEL       = 5;   // POST /appointments/{id}/cancel
    private static final int LIMIT_FEEDBACK_NEW = 5;   // POST /reports
    private static final int LIMIT_FEEDBACK_SRCH = 10; // POST /reports/search (CCCD lookup)
    private static final int LIMIT_OTHER_POST   = 20;
    private static final int LIMIT_GET          = 60;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/citizen/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String method = request.getMethod();
        String uri    = request.getRequestURI();
        String ip     = resolveClientIp(request);

        long currentMinute = System.currentTimeMillis() / 60_000L;
        evictExpiredBuckets(currentMinute);

        String bucket = resolveBucket(method, uri);
        int    limit  = resolveLimit(method, uri);
        String key    = ip + "|" + bucket + "|" + currentMinute;

        int count = counters.computeIfAbsent(key, k -> new AtomicInteger(0)).incrementAndGet();
        if (count > limit) {
            log.warn("Rate limit exceeded: ip={} bucket={} count={} limit={}", ip, bucket, count, limit);
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"success\":false,\"code\":\"RATE_LIMIT_EXCEEDED\"," +
                    "\"message\":\"Qu\\u00e1 nhi\\u1ec1u y\\u00eau c\\u1ea7u. Vui l\\u00f2ng th\\u1eed l\\u1ea1i sau 1 ph\\u00fat.\"}");
            return;
        }

        chain.doFilter(request, response);
    }

    // ---- helpers ----

    private String resolveBucket(String method, String uri) {
        if ("POST".equals(method)) {
            if (uri.matches(".*/citizen/appointments/?$"))                return "book";
            if (uri.matches(".*/citizen/appointments/\\d+/cancel"))        return "cancel";
            if (uri.endsWith("/citizen/reports/search"))                   return "fsearch";
            if (uri.matches(".*/citizen/reports/?$"))                      return "feedback";
        }
        return method + "-general";
    }

    private int resolveLimit(String method, String uri) {
        if ("POST".equals(method)) {
            if (uri.matches(".*/citizen/appointments/?$"))                 return LIMIT_BOOKING;
            if (uri.matches(".*/citizen/appointments/\\d+/cancel"))        return LIMIT_CANCEL;
            if (uri.endsWith("/citizen/reports/search"))                   return LIMIT_FEEDBACK_SRCH;
            if (uri.matches(".*/citizen/reports/?$"))                      return LIMIT_FEEDBACK_NEW;
            return LIMIT_OTHER_POST;
        }
        return LIMIT_GET;
    }

    /**
     * Remove entries from minutes older than current-1 to bound memory.
     * Uses CAS so only one thread per minute performs the sweep.
     */
    private void evictExpiredBuckets(long currentMinute) {
        long prev = lastCleanupMinute.get();
        if (prev < currentMinute && lastCleanupMinute.compareAndSet(prev, currentMinute)) {
            long cutoff = currentMinute - 2;
            counters.entrySet().removeIf(e -> {
                try {
                    String[] parts = e.getKey().split("\\|");
                    return parts.length == 3 && Long.parseLong(parts[2]) < cutoff;
                } catch (Exception ignored) {
                    return false;
                }
            });
        }
    }

    /** Respect X-Forwarded-For from trusted proxy; fall back to direct socket. */
    private String resolveClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        String xri = request.getHeader("X-Real-IP");
        if (xri != null && !xri.isBlank()) return xri.trim();
        return request.getRemoteAddr();
    }
}
