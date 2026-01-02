package com.smtech.SM_Caterer.exception;

/**
 * Thrown when a cross-tenant access attempt is detected.
 * HTTP Status: 403 FORBIDDEN
 *
 * Use Cases:
 * - User trying to access another tenant's data
 * - Tenant ID mismatch in operations
 * - Security violation in multi-tenant context
 *
 * CRITICAL: This exception indicates a serious security issue
 * and should be logged and monitored.
 *
 * @author SM Tech
 * @version 1.0
 * @since 2025-12-12
 */
public class TenantIsolationViolationException extends BaseException {

    /**
     * Constructs exception with formatted message.
     *
     * @param currentTenantId Current tenant ID
     * @param attemptedTenantId Attempted tenant ID
     * @param resource Resource being accessed
     */
    public TenantIsolationViolationException(Long currentTenantId, Long attemptedTenantId, String resource) {
        super(String.format("Tenant isolation violation: Tenant %d attempted to access %s belonging to Tenant %d",
                           currentTenantId, resource, attemptedTenantId),
              "TENANT_ISOLATION_VIOLATION");
    }

    /**
     * Constructs exception with custom message.
     *
     * @param message Custom error message
     */
    public TenantIsolationViolationException(String message) {
        super(message, "TENANT_ISOLATION_VIOLATION");
    }

    /**
     * Constructs exception with message and cause.
     *
     * @param message Error message
     * @param cause The cause of the exception
     */
    public TenantIsolationViolationException(String message, Throwable cause) {
        super(message, "TENANT_ISOLATION_VIOLATION", cause);
    }

    /**
     * Factory method for tenant context not set.
     *
     * @return TenantIsolationViolationException
     */
    public static TenantIsolationViolationException contextNotSet() {
        return new TenantIsolationViolationException("Tenant context is not set. Cannot perform operation.");
    }

    /**
     * Factory method for missing tenant ID.
     *
     * @param operation Operation being performed
     * @return TenantIsolationViolationException
     */
    public static TenantIsolationViolationException missingTenantId(String operation) {
        return new TenantIsolationViolationException(
            String.format("Tenant ID is required for operation: %s", operation));
    }
}
