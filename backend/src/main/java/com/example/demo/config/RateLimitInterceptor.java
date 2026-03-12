package com.example.demo.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simple in-memory rate limiter for citizen endpoints.
 * Limits: 30 requests/minute per client IP on /api/citizen/** paths.
 * Resets counter every 60 seconds per IP.
 */
@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final int MAX_REQUESTS_PER_MINUTE = 30;
    private static final long WINDOW_MILLIS = 60_000L;

    // Fix #3: bounded LRU map — evicts oldest entry when > 10_000 unique IPs tracked.
    // Prevents unbounded memory growth under sustained distributed load.
    // Key: client IP, Value: [requestCount, windowStartEpochMs]
    private final Map<String, long[]> requestCounts = Collections.synchronizedMap(
            new LinkedHashMap<>(1024, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, long[]> eldest) {
                    return size() > 10_000;
                }
            });

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String ip = resolveClientIp(request);
        long now = Instant.now().toEpochMilli();

        // synchronizedMap does not expose atomic compute; synchronize explicitly
        long[] entry;
        synchronized (requestCounts) {
            long[] existing = requestCounts.get(ip);
            if (existing == null || (now - existing[1]) > WINDOW_MILLIS) {
                entry = new long[]{1, now};
            } else {
                existing[0]++;
                entry = existing;
            }
            requestCounts.put(ip, entry);
        }

        if (entry[0] > MAX_REQUESTS_PER_MINUTE) {
            log.warn("Rate limit exceeded for IP={} on path={}", ip, request.getRequestURI());
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(
                "{\"success\":false,\"message\":\"Quá nhiều yêu cầu. Vui lòng thử lại sau 1 phút.\"}");
            return false;
        }

        return true;
    }

    /**
     * Prefer X-Forwarded-For (set by Nginx reverse proxy) over REMOTE_ADDR.
     */
    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            // X-Forwarded-For may contain comma-separated chain; take first (client IP)
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
