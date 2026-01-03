package com.smtech.SM_Caterer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when rate limit is exceeded.
 * Results in 429 Too Many Requests response.
 *
 * @author CloudCaters Team
 * @version 1.0
 * @since Phase 2
 */
@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class RateLimitExceededException extends BaseException {

    private static final String ERROR_CODE = "RATE_001";

    public RateLimitExceededException(String message) {
        super(message, ERROR_CODE);
    }

    public RateLimitExceededException() {
        super("Rate limit exceeded. Please try again later.", ERROR_CODE);
    }
}
