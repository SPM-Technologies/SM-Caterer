package com.smtech.SM_Caterer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

/**
 * JPA Auditing configuration.
 * Automatically populates createdBy, updatedBy, createdAt, updatedAt fields.
 *
 * @author CloudCaters Team
 * @version 1.0
 * @since Phase 1
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {

    /**
     * Provides AuditorAware bean for JPA auditing.
     * Returns the current user ID for audit fields.
     *
     * @return AuditorAware implementation
     */
    @Bean
    public AuditorAware<Long> auditorProvider() {
        return new SpringSecurityAuditorAware();
    }

    /**
     * Provides the current auditor (user ID) for JPA auditing.
     *
     * Phase 1 Implementation:
     * - Returns system user (ID=1) for all operations
     * - This is temporary until Spring Security is implemented
     *
     * Phase 2 Implementation:
     * - Will extract user ID from SecurityContextHolder
     * - Will use actual authenticated user
     *
     * Usage:
     * <pre>
     * // Automatically set by JPA when entity is saved
     * entity.setCreatedBy(auditorProvider.getCurrentAuditor());
     * </pre>
     */
    static class SpringSecurityAuditorAware implements AuditorAware<Long> {

        @Override
        public Optional<Long> getCurrentAuditor() {
            // TODO Phase 2: Get from SecurityContextHolder.getContext().getAuthentication()
            // For now, return system user ID
            return Optional.of(1L);
        }
    }
}
