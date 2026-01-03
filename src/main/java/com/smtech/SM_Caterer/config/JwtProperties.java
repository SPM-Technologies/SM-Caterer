package com.smtech.SM_Caterer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT Configuration Properties.
 * Binds to jwt.* properties in application.properties.
 *
 * @author CloudCaters Team
 * @version 1.0
 * @since Phase 2
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * Secret key for JWT signing.
     * Must be at least 256 bits for HS512 algorithm.
     */
    private String secret;

    /**
     * Access token expiration time in milliseconds.
     * Default: 86400000 (24 hours)
     */
    private long expiration = 86400000;

    /**
     * Refresh token expiration time in milliseconds.
     * Default: 604800000 (7 days)
     */
    private long refreshExpiration = 604800000;

    /**
     * HTTP header name for JWT token.
     * Default: Authorization
     */
    private String header = "Authorization";

    /**
     * Token prefix in header value.
     * Default: Bearer
     */
    private String prefix = "Bearer";
}
