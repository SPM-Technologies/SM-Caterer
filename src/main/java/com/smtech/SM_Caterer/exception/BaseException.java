package com.smtech.SM_Caterer.exception;

import lombok.Getter;

/**
 * Base exception for all custom exceptions.
 * Provides error code for API responses.
 *
 * @author SM Tech
 * @version 1.0
 * @since 2025-12-12
 */
@Getter
public abstract class BaseException extends RuntimeException {

    private final String errorCode;

    /**
     * Constructs a new BaseException with message and error code.
     *
     * @param message Error message
     * @param errorCode Error code for API response
     */
    public BaseException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Constructs a new BaseException with message, error code, and cause.
     *
     * @param message Error message
     * @param errorCode Error code for API response
     * @param cause The cause of the exception
     */
    public BaseException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
