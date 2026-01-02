package com.smtech.SM_Caterer.exception;

/**
 * Thrown when a business rule violation occurs.
 * HTTP Status: 400 BAD REQUEST
 *
 * Use Cases:
 * - Invalid state transition (e.g., cancelling a completed order)
 * - Business validation failure
 * - Operation not allowed in current state
 * - Invalid data relationship
 * - Stock insufficient for operation
 * - Date validation failure
 *
 * @author SM Tech
 * @version 1.0
 * @since 2025-12-12
 */
public class InvalidOperationException extends BaseException {

    /**
     * Constructs exception with custom message.
     *
     * @param message Error message describing the business rule violation
     */
    public InvalidOperationException(String message) {
        super(message, "INVALID_OPERATION");
    }

    /**
     * Constructs exception with message and cause.
     *
     * @param message Error message
     * @param cause The cause of the exception
     */
    public InvalidOperationException(String message, Throwable cause) {
        super(message, "INVALID_OPERATION", cause);
    }

    /**
     * Factory method for invalid state transition.
     *
     * @param entity Entity name
     * @param currentState Current state
     * @param attemptedState Attempted state
     * @return InvalidOperationException
     */
    public static InvalidOperationException invalidStateTransition(String entity, String currentState, String attemptedState) {
        return new InvalidOperationException(
            String.format("Cannot change %s from %s to %s", entity, currentState, attemptedState));
    }

    /**
     * Factory method for operation not allowed.
     *
     * @param operation Operation name
     * @param reason Reason why not allowed
     * @return InvalidOperationException
     */
    public static InvalidOperationException notAllowed(String operation, String reason) {
        return new InvalidOperationException(
            String.format("Operation '%s' not allowed: %s", operation, reason));
    }

    /**
     * Factory method for insufficient stock.
     *
     * @param materialName Material name
     * @param required Required quantity
     * @param available Available quantity
     * @return InvalidOperationException
     */
    public static InvalidOperationException insufficientStock(String materialName, Object required, Object available) {
        return new InvalidOperationException(
            String.format("Insufficient stock for %s. Required: %s, Available: %s",
                         materialName, required, available));
    }

    /**
     * Factory method for date validation failure.
     *
     * @param message Date validation error message
     * @return InvalidOperationException
     */
    public static InvalidOperationException invalidDate(String message) {
        return new InvalidOperationException(
            String.format("Invalid date: %s", message));
    }
}
