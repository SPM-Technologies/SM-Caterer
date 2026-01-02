package com.smtech.SM_Caterer.context;

/**
 * Thread-local storage for current tenant ID.
 * Ensures tenant isolation in multi-tenant environment.
 *
 * CRITICAL SECURITY:
 * - Must be set before any database operation
 * - Must be cleared after request completes (prevents memory leaks)
 * - ThreadLocal ensures thread safety in concurrent requests
 *
 * Usage Pattern:
 * <pre>
 * {@code
 * try {
 *     TenantContext.setCurrentTenant(tenantId);
 *     // Perform database operations
 *     // All queries will be filtered by this tenant ID
 * } finally {
 *     TenantContext.clear();  // MUST clear to prevent memory leaks
 * }
 * }
 * </pre>
 *
 * Phase 2 Integration:
 * In Phase 2, this will be automatically set by a Spring interceptor
 * based on JWT token or session data. For now, it must be set manually.
 *
 * @author CloudCaters Team
 * @version 1.0
 * @since Phase 1
 */
public class TenantContext {

    /**
     * ThreadLocal storage for tenant ID.
     * Each thread has its own copy, ensuring thread safety.
     */
    private static final ThreadLocal<Long> CURRENT_TENANT = new ThreadLocal<>();

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private TenantContext() {
        throw new IllegalStateException("Utility class - do not instantiate");
    }

    /**
     * Sets the current tenant ID for this thread.
     *
     * @param tenantId Tenant ID (must not be null)
     * @throws IllegalArgumentException if tenantId is null
     */
    public static void setCurrentTenant(Long tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID cannot be null");
        }
        CURRENT_TENANT.set(tenantId);
    }

    /**
     * Gets the current tenant ID for this thread.
     *
     * @return Current tenant ID, or null if not set
     */
    public static Long getCurrentTenant() {
        return CURRENT_TENANT.get();
    }

    /**
     * Clears the current tenant ID.
     *
     * CRITICAL: Must be called in finally block to prevent memory leaks.
     * If not cleared, ThreadLocal will hold reference even after request completes.
     */
    public static void clear() {
        CURRENT_TENANT.remove();
    }

    /**
     * Checks if tenant context is set.
     *
     * @return true if tenant is set, false otherwise
     */
    public static boolean isSet() {
        return CURRENT_TENANT.get() != null;
    }

    /**
     * Gets current tenant ID or throws exception if not set.
     *
     * Use this method when tenant MUST be set (e.g., in service layer).
     *
     * @return Current tenant ID
     * @throws IllegalStateException if tenant is not set
     */
    public static Long getCurrentTenantOrThrow() {
        Long tenantId = CURRENT_TENANT.get();
        if (tenantId == null) {
            throw new IllegalStateException(
                "Tenant context is not set. " +
                "Call TenantContext.setCurrentTenant() before accessing tenant-specific data."
            );
        }
        return tenantId;
    }
}
