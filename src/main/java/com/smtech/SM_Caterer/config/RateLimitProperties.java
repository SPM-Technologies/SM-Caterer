package com.smtech.SM_Caterer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Rate Limiting Configuration Properties.
 * Binds to rate-limit.* properties in application.properties.
 *
 * @author CloudCaters Team
 * @version 1.0
 * @since Phase 2
 */
@Data
@Component
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {

    /**
     * Whether rate limiting is enabled.
     */
    private boolean enabled = true;

    /**
     * Request limits per minute by user type.
     */
    private RequestsPerMinute requestsPerMinute = new RequestsPerMinute();

    @Data
    public static class RequestsPerMinute {
        /**
         * Requests per minute for unauthenticated users.
         */
        private int unauthenticated = 100;

        /**
         * Requests per minute for authenticated users.
         */
        private int authenticated = 500;

        /**
         * Requests per minute for admin users.
         */
        private int admin = 1000;
    }
}
