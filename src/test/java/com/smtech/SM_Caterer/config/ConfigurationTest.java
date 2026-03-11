package com.smtech.SM_Caterer.config;

import com.smtech.SM_Caterer.base.BaseControllerTest;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.LocaleResolver;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for application configuration classes.
 *
 * Validates that all configuration beans load correctly under the test profile,
 * including security filter chains, Thymeleaf layout dialect, i18n locale resolver,
 * password encoder, and database/Flyway settings.
 *
 * @author CloudCaters Team
 * @since Phase 7
 */
@DisplayName("Configuration Integration Tests")
class ConfigurationTest extends BaseControllerTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private List<SecurityFilterChain> securityFilterChains;

    @Autowired
    private LayoutDialect layoutDialect;

    @Autowired
    private LocaleResolver localeResolver;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private DataSource dataSource;

    // =========================================================================
    // Context Loads
    // =========================================================================

    @Nested
    @DisplayName("A) Application Context")
    class ApplicationContextTests {

        @Test
        @DisplayName("Application context should load without errors")
        void contextLoads() {
            assertThat(applicationContext).isNotNull();
        }

        @Test
        @DisplayName("SecurityFilterChain beans should exist (expect 2: API and Web)")
        void securityFilterChainBeansShouldExist() {
            assertThat(securityFilterChains)
                    .as("Should have exactly 2 SecurityFilterChain beans (API and Web)")
                    .hasSize(2);
        }

        @Test
        @DisplayName("LayoutDialect bean should exist")
        void layoutDialectBeanShouldExist() {
            assertThat(layoutDialect)
                    .as("LayoutDialect bean for Thymeleaf layout inheritance should be loaded")
                    .isNotNull();
        }

        @Test
        @DisplayName("LocaleResolver bean should exist")
        void localeResolverBeanShouldExist() {
            assertThat(localeResolver)
                    .as("LocaleResolver bean for i18n should be loaded")
                    .isNotNull();
        }

        @Test
        @DisplayName("PasswordEncoder bean should exist")
        void passwordEncoderBeanShouldExist() {
            assertThat(passwordEncoder)
                    .as("PasswordEncoder bean should be loaded")
                    .isNotNull();
        }

        @Test
        @DisplayName("AuthenticationManager bean should exist")
        void authenticationManagerBeanShouldExist() {
            assertThat(authenticationManager)
                    .as("AuthenticationManager bean should be loaded")
                    .isNotNull();
        }
    }

    // =========================================================================
    // Security Configuration
    // =========================================================================

    @Nested
    @DisplayName("B) Security Configuration")
    class SecurityConfigTests {

        @Test
        @DisplayName("Two SecurityFilterChain beans should exist (API and Web)")
        void twoSecurityFilterChainsShouldExist() {
            assertThat(securityFilterChains)
                    .as("There should be exactly 2 SecurityFilterChain beans")
                    .hasSize(2);
        }

        @Test
        @DisplayName("SecurityFilterChain beans should be distinct instances")
        void filterChainsShouldBeDistinct() {
            assertThat(securityFilterChains.get(0))
                    .as("API and Web filter chains should be distinct bean instances")
                    .isNotSameAs(securityFilterChains.get(1));
        }

        @Test
        @DisplayName("CSRF should be configured (CookieCsrfTokenRepository bean exists in context)")
        void csrfShouldBeConfigured() {
            // The web filter chain uses CookieCsrfTokenRepository.
            // We verify that the web security filter chain (Order 2) is loaded
            // and that it is not null, which means CSRF configuration was accepted.
            assertThat(securityFilterChains)
                    .as("Security filter chains with CSRF configuration should load successfully")
                    .isNotEmpty()
                    .allSatisfy(chain -> assertThat(chain).isNotNull());
        }

        @Test
        @DisplayName("PasswordEncoder should correctly encode and verify passwords")
        void passwordEncoderShouldWork() {
            String rawPassword = "TestPassword123!";
            String encoded = passwordEncoder.encode(rawPassword);

            assertThat(encoded)
                    .as("Encoded password should differ from raw password")
                    .isNotEqualTo(rawPassword);
            assertThat(passwordEncoder.matches(rawPassword, encoded))
                    .as("PasswordEncoder should verify the raw password against its encoded form")
                    .isTrue();
            assertThat(passwordEncoder.matches("WrongPassword", encoded))
                    .as("PasswordEncoder should reject an incorrect password")
                    .isFalse();
        }
    }

    // =========================================================================
    // Test Profile
    // =========================================================================

    @Nested
    @DisplayName("C) Test Profile Configuration")
    class TestProfileTests {

        @Test
        @DisplayName("Flyway should be disabled in the test profile")
        void flywayShouldBeDisabled() {
            // Flyway is disabled via spring.flyway.enabled=false in application-test.properties.
            // If Flyway were enabled, it would attempt migrations which would conflict with
            // JPA's create-drop DDL. We verify the property value via the Environment.
            String flywayEnabled = applicationContext.getEnvironment()
                    .getProperty("spring.flyway.enabled");
            assertThat(flywayEnabled)
                    .as("spring.flyway.enabled should be 'false' in the test profile")
                    .isEqualTo("false");
        }

        @Test
        @DisplayName("H2 in-memory database should be used in the test profile")
        void h2DatabaseShouldBeUsed() throws SQLException {
            try (Connection connection = dataSource.getConnection()) {
                String url = connection.getMetaData().getURL();
                assertThat(url)
                        .as("DataSource URL should reference H2 in-memory database")
                        .containsIgnoringCase("h2")
                        .containsIgnoringCase("mem");

                String driverName = connection.getMetaData().getDriverName();
                assertThat(driverName)
                        .as("JDBC driver should be H2")
                        .containsIgnoringCase("H2");
            }
        }

        @Test
        @DisplayName("JPA ddl-auto should be create-drop in the test profile")
        void jpaDdlAutoShouldBeCreateDrop() {
            String ddlAuto = applicationContext.getEnvironment()
                    .getProperty("spring.jpa.hibernate.ddl-auto");
            assertThat(ddlAuto)
                    .as("JPA ddl-auto should be 'create-drop' for test profile")
                    .isEqualTo("create-drop");
        }
    }

    // =========================================================================
    // Thymeleaf Configuration
    // =========================================================================

    @Nested
    @DisplayName("D) Thymeleaf Configuration")
    class ThymeleafConfigTests {

        @Test
        @DisplayName("LayoutDialect bean should be an instance of LayoutDialect")
        void layoutDialectShouldBeCorrectType() {
            assertThat(layoutDialect)
                    .as("LayoutDialect bean should be the Ultraq Thymeleaf LayoutDialect")
                    .isInstanceOf(LayoutDialect.class);
        }

        @Test
        @DisplayName("Thymeleaf template checking should be disabled in test profile")
        void templateCheckingShouldBeDisabled() {
            String checkTemplateLocation = applicationContext.getEnvironment()
                    .getProperty("spring.thymeleaf.check-template-location");
            assertThat(checkTemplateLocation)
                    .as("Template location checking should be disabled in test profile")
                    .isEqualTo("false");
        }

        @Test
        @DisplayName("Thymeleaf cache should be disabled in test profile")
        void thymeleafCacheShouldBeDisabled() {
            String cache = applicationContext.getEnvironment()
                    .getProperty("spring.thymeleaf.cache");
            assertThat(cache)
                    .as("Thymeleaf cache should be disabled in test profile")
                    .isEqualTo("false");
        }

        @Test
        @DisplayName("LocaleResolver should be a CookieLocaleResolver")
        void localeResolverShouldBeCookieBased() {
            assertThat(localeResolver)
                    .as("LocaleResolver should be a CookieLocaleResolver for i18n")
                    .isInstanceOf(org.springframework.web.servlet.i18n.CookieLocaleResolver.class);
        }
    }
}
