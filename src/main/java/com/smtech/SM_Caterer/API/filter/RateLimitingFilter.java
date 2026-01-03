package com.smtech.SM_Caterer.API.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smtech.SM_Caterer.config.RateLimitProperties;
import com.smtech.SM_Caterer.security.CustomUserDetails;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate Limiting Filter using Bucket4j.
 * Limits requests per IP for unauthenticated users and per user for authenticated.
 *
 * Filter Order:
 * - JwtAuthenticationFilter (no @Order, added to SecurityFilterChain)
 * - TenantContextFilter (@Order(2))
 * - RateLimitingFilter (@Order(3)) - runs AFTER authentication to access user context
 *
 * @author CloudCaters Team
 * @version 1.0
 * @since Phase 2
 */
@Slf4j
@Component
@Order(3) // Run AFTER JwtAuthenticationFilter and TenantContextFilter to access user context
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimitProperties rateLimitProperties;
    private final ObjectMapper objectMapper;

    // Cache for rate limit buckets
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        if (!rateLimitProperties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = resolveKey(request);
        Bucket bucket = resolveBucket(key);

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for key: {}", key);
            sendRateLimitExceededResponse(response, request.getRequestURI());
        }
    }

    /**
     * Resolves the rate limiting key.
     * Uses user ID for authenticated requests, IP address for unauthenticated.
     */
    private String resolveKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() &&
                authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return "user:" + userDetails.getId();
        }

        // Use IP address for unauthenticated requests
        String ip = getClientIP(request);
        return "ip:" + ip;
    }

    /**
     * Gets or creates a rate limiting bucket for the given key.
     */
    private Bucket resolveBucket(String key) {
        return buckets.computeIfAbsent(key, this::createBucket);
    }

    /**
     * Creates a new bucket based on user type.
     */
    private Bucket createBucket(String key) {
        int limit;

        if (key.startsWith("user:")) {
            // Check if admin (simplified - in production, check role from cache)
            limit = rateLimitProperties.getRequestsPerMinute().getAuthenticated();
        } else {
            limit = rateLimitProperties.getRequestsPerMinute().getUnauthenticated();
        }

        Bandwidth bandwidth = Bandwidth.classic(
                limit,
                Refill.greedy(limit, Duration.ofMinutes(1))
        );

        return Bucket.builder()
                .addLimit(bandwidth)
                .build();
    }

    /**
     * Extracts client IP address from request.
     */
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }

        return request.getRemoteAddr();
    }

    /**
     * Sends 429 Too Many Requests response.
     */
    private void sendRateLimitExceededResponse(HttpServletResponse response, String path) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
        body.put("error", "Too Many Requests");
        body.put("message", "Rate limit exceeded. Please try again later.");
        body.put("path", path);
        body.put("timestamp", LocalDateTime.now().toString());

        objectMapper.writeValue(response.getOutputStream(), body);
    }

    /**
     * Skip rate limiting for static resources and health endpoints.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator/health") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/api-docs") ||
               path.startsWith("/v3/api-docs") ||
               path.endsWith(".css") ||
               path.endsWith(".js") ||
               path.endsWith(".ico");
    }
}
