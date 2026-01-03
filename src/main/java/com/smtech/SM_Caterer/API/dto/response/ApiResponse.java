package com.smtech.SM_Caterer.API.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard API Response wrapper.
 * All API responses are wrapped in this structure for consistency.
 *
 * @param <T> Type of data payload
 * @author CloudCaters Team
 * @version 1.0
 * @since Phase 2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * Indicates if the request was successful.
     */
    private boolean success;

    /**
     * Human-readable message.
     */
    private String message;

    /**
     * Response payload.
     */
    private T data;

    /**
     * Response timestamp.
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Request path (optional).
     */
    private String path;

    /**
     * Creates a successful response with data.
     *
     * @param data Response data
     * @param <T>  Data type
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a successful response with message and data.
     *
     * @param message Success message
     * @param data    Response data
     * @param <T>     Data type
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a successful response with message only.
     *
     * @param message Success message
     * @param <T>     Data type
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates an error response.
     *
     * @param message Error message
     * @param <T>     Data type
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates an error response with path.
     *
     * @param message Error message
     * @param path    Request path
     * @param <T>     Data type
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> error(String message, String path) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
