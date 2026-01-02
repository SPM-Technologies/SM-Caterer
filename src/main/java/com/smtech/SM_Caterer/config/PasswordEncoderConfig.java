package com.smtech.SM_Caterer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Password encoding configuration using BCrypt.
 *
 * BCrypt Features:
 * - Adaptive hashing (automatically handles salting)
 * - Configurable strength (default: 10)
 * - One-way hashing (cannot be decrypted)
 * - Takes ~100ms per encoding (protects against brute force)
 *
 * Usage:
 * <pre>
 * {@code
 * @Autowired
 * private PasswordEncoder passwordEncoder;
 *
 * // Encoding password
 * String encoded = passwordEncoder.encode("plainPassword");
 *
 * // Verifying password
 * boolean matches = passwordEncoder.matches("plainPassword", encoded);
 * }
 * </pre>
 *
 * @author CloudCaters Team
 * @version 1.0
 * @since Phase 1
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * Creates BCrypt password encoder with default strength (10).
     *
     * Strength explanation:
     * - Strength 10 = 2^10 = 1,024 rounds (~100ms per encoding)
     * - Strength 12 = 2^12 = 4,096 rounds (~400ms per encoding)
     * - Higher strength = more secure but slower
     *
     * Default strength (10) provides good balance between
     * security and performance for most applications.
     *
     * @return PasswordEncoder configured with BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
