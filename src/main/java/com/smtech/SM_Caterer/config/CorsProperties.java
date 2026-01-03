package com.smtech.SM_Caterer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * CORS Configuration Properties.
 * Binds to cors.* properties in application.properties.
 *
 * @author CloudCaters Team
 * @version 1.0
 * @since Phase 2
 */
@Data
@Component
@ConfigurationProperties(prefix = "cors")
public class CorsProperties {

    /**
     * Allowed origins for CORS.
     * Comma-separated list in properties file.
     */
    private List<String> allowedOrigins = List.of("http://localhost:3000", "http://localhost:8080");

    /**
     * Allowed HTTP methods.
     */
    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");

    /**
     * Allowed headers in requests.
     */
    private List<String> allowedHeaders = List.of("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin");

    /**
     * Whether to allow credentials (cookies, auth headers).
     */
    private boolean allowCredentials = true;

    /**
     * Max age for preflight cache in seconds.
     */
    private long maxAge = 3600;
}
