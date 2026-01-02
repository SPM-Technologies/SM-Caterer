package com.smtech.SM_Caterer.exception;

/**
 * Thrown when requested resource is not found.
 * HTTP Status: 404 NOT FOUND
 *
 * Use Cases:
 * - Entity not found by ID
 * - Entity not found by unique field
 * - Resource doesn't exist in database
 *
 * @author SM Tech
 * @version 1.0
 * @since 2025-12-12
 */
public class ResourceNotFoundException extends BaseException {

    /**
     * Constructs exception with formatted message.
     *
     * @param resource Resource name (e.g., "Tenant", "User")
     * @param field Field name (e.g., "id", "email")
     * @param value Field value
     */
    public ResourceNotFoundException(String resource, String field, Object value) {
        super(String.format("%s not found with %s: '%s'", resource, field, value),
              "RESOURCE_NOT_FOUND");
    }

    /**
     * Constructs exception with custom message.
     *
     * @param message Custom error message
     */
    public ResourceNotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND");
    }

    /**
     * Constructs exception with message and cause.
     *
     * @param message Error message
     * @param cause The cause of the exception
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, "RESOURCE_NOT_FOUND", cause);
    }
}
