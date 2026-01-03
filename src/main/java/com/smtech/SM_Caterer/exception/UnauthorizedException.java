package com.smtech.SM_Caterer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when user is not authorized to perform an action.
 * Results in 403 Forbidden response.
 *
 * @author CloudCaters Team
 * @version 1.0
 * @since Phase 2
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorizedException extends BaseException {

    private static final String ERROR_CODE = "AUTH_002";

    public UnauthorizedException(String message) {
        super(message, ERROR_CODE);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, ERROR_CODE, cause);
    }
}
