package com.smtech.SM_Caterer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when authentication fails.
 * Results in 401 Unauthorized response.
 *
 * @author CloudCaters Team
 * @version 1.0
 * @since Phase 2
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class AuthenticationException extends BaseException {

    private static final String ERROR_CODE = "AUTH_001";

    public AuthenticationException(String message) {
        super(message, ERROR_CODE);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, ERROR_CODE, cause);
    }
}
