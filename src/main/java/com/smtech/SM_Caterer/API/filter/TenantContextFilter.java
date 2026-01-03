package com.smtech.SM_Caterer.API.filter;

import com.smtech.SM_Caterer.context.TenantContext;
import com.smtech.SM_Caterer.security.CustomUserDetails;
import jakarta.persistence.EntityManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Tenant Context Filter.
 * Sets tenant context from authenticated user and enables Hibernate tenant filter.
 *
 * This filter runs after JwtAuthenticationFilter to ensure user is authenticated.
 *
 * Flow:
 * 1. Get authenticated user from SecurityContext
 * 2. Extract tenant ID from user
 * 3. Set TenantContext.setCurrentTenant(tenantId)
 * 4. Enable Hibernate tenant filter
 * 5. Process request
 * 6. Clear tenant context in finally block
 *
 * @author CloudCaters Team
 * @version 1.0
 * @since Phase 2
 */
@Slf4j
@Component
@Order(2) // Run after JwtAuthenticationFilter
@RequiredArgsConstructor
public class TenantContextFilter extends OncePerRequestFilter {

    private final EntityManager entityManager;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated() &&
                    authentication.getPrincipal() instanceof CustomUserDetails userDetails) {

                Long tenantId = userDetails.getTenantId();

                if (tenantId != null) {
                    // Set tenant context for this thread
                    TenantContext.setCurrentTenant(tenantId);

                    // Enable Hibernate tenant filter
                    enableTenantFilter(tenantId);

                    log.debug("Tenant context set: tenantId={}, user={}",
                            tenantId, userDetails.getUsername());
                } else if (!userDetails.isSuperAdmin()) {
                    // Non-super-admin users must have a tenant
                    log.warn("User has no tenant ID assigned (ID: {})", userDetails.getId());
                    log.debug("User {} has no tenant ID assigned", userDetails.getUsername());
                }
            }

            filterChain.doFilter(request, response);

        } finally {
            // Always clear tenant context after request completes
            TenantContext.clear();
            log.trace("Tenant context cleared");
        }
    }

    /**
     * Enables Hibernate tenant filter for current session.
     *
     * @param tenantId Tenant ID to filter by
     */
    private void enableTenantFilter(Long tenantId) {
        try {
            Session session = entityManager.unwrap(Session.class);
            session.enableFilter("tenantFilter")
                   .setParameter("tenantId", tenantId);
            log.trace("Hibernate tenant filter enabled for tenantId={}", tenantId);
        } catch (Exception e) {
            log.warn("Failed to enable Hibernate tenant filter: {}", e.getMessage());
        }
    }

    /**
     * Skip filter for public endpoints.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/v1/auth/login") ||
               path.startsWith("/api/v1/auth/refresh") ||
               path.startsWith("/actuator/") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/api-docs") ||
               path.startsWith("/v3/api-docs");
    }
}
