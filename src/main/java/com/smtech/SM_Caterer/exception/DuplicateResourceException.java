package com.smtech.SM_Caterer.exception;

/**
 * Thrown when attempting to create a duplicate resource.
 * HTTP Status: 409 CONFLICT
 *
 * Use Cases:
 * - Unique constraint violation
 * - Duplicate tenant code
 * - Duplicate username/email
 * - Duplicate material code, menu code, etc.
 *
 * @author SM Tech
 * @version 1.0
 * @since 2025-12-12
 */
public class DuplicateResourceException extends BaseException {

    /**
     * Constructs exception with formatted message.
     *
     * @param resource Resource name (e.g., "Tenant", "User")
     * @param field Field name that caused duplication (e.g., "tenantCode", "email")
     * @param value Field value
     */
    public DuplicateResourceException(String resource, String field, Object value) {
        super(String.format("%s already exists with %s: '%s'", resource, field, value),
              "DUPLICATE_RESOURCE");
    }

    /**
     * Constructs exception with custom message.
     *
     * @param message Custom error message
     */
    public DuplicateResourceException(String message) {
        super(message, "DUPLICATE_RESOURCE");
    }

    /**
     * Constructs exception with message and cause.
     *
     * @param message Error message
     * @param cause The cause of the exception
     */
    public DuplicateResourceException(String message, Throwable cause) {
        super(message, "DUPLICATE_RESOURCE", cause);
    }
}
