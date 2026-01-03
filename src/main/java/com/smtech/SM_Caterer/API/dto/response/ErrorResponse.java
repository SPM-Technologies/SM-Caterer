package com.smtech.SM_Caterer.API.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Error response structure.
 * Contains detailed error information for API errors.
 *
 * @author CloudCaters Team
 * @version 1.0
 * @since Phase 2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /**
     * HTTP status code.
     */
    private int status;

    /**
     * Error type (e.g., "Not Found", "Bad Request").
     */
    private String error;

    /**
     * Human-readable error message.
     */
    private String message;

    /**
     * Request path that caused the error.
     */
    private String path;

    /**
     * Error timestamp.
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Validation errors (field -> error message).
     */
    private Map<String, String> validationErrors;

    /**
     * Error code for client-side handling.
     */
    private String errorCode;

    /**
     * Creates error response for validation errors.
     *
     * @param status           HTTP status
     * @param message          Error message
     * @param path             Request path
     * @param validationErrors Field validation errors
     * @return ErrorResponse
     */
    public static ErrorResponse validationError(int status, String message, String path,
                                                  Map<String, String> validationErrors) {
        return ErrorResponse.builder()
                .status(status)
                .error("Validation Failed")
                .message(message)
                .path(path)
                .validationErrors(validationErrors)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates simple error response.
     *
     * @param status  HTTP status
     * @param error   Error type
     * @param message Error message
     * @param path    Request path
     * @return ErrorResponse
     */
    public static ErrorResponse of(int status, String error, String message, String path) {
        return ErrorResponse.builder()
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
