package com.smtech.SM_Caterer.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Flyway configuration to handle failed migrations gracefully.
 * Runs repair before migrate to fix any failed migration entries.
 */
@Configuration
public class FlywayConfig {

    /**
     * Custom migration strategy that repairs failed migrations before running migrate.
     * This handles the case where a previous migration failed partially.
     */
    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return (Flyway flyway) -> {
            // Repair first to fix failed migration checksums and remove failed entries
            flyway.repair();
            // Then run migrate
            flyway.migrate();
        };
    }
}
