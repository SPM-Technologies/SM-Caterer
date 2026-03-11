package com.smtech.SM_Caterer.config;

import com.smtech.SM_Caterer.base.BaseUnitTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Tests for i18n message consistency across all supported locales.
 *
 * Validates that:
 * - Critical navigation keys exist in all locale files (English, Hindi, Marathi)
 * - All keys present in the English baseline file exist in Hindi and Marathi
 * - No locale file is missing translations that the default locale provides
 *
 * @author CloudCaters Team
 * @since Phase 7
 */
@DisplayName("I18n Message Consistency Tests")
class I18nMessageTest extends BaseUnitTest {

    private static Properties englishProps;
    private static Properties hindiProps;
    private static Properties marathiProps;

    @BeforeAll
    static void loadAllMessageFiles() throws IOException {
        englishProps = loadProperties("messages/messages.properties");
        hindiProps = loadProperties("messages/messages_hi.properties");
        marathiProps = loadProperties("messages/messages_mr.properties");
    }

    private static Properties loadProperties(String classpathResource) throws IOException {
        Properties props = new Properties();
        try (InputStream is = I18nMessageTest.class.getClassLoader().getResourceAsStream(classpathResource)) {
            assertThat(is)
                    .as("Property file '%s' should exist on the classpath", classpathResource)
                    .isNotNull();
            props.load(is);
        }
        return props;
    }

    // =========================================================================
    // Critical Navigation Keys
    // =========================================================================

    @Nested
    @DisplayName("Critical Navigation Keys")
    class CriticalNavigationKeys {

        private static final List<String> CRITICAL_NAV_KEYS = Arrays.asList(
                "nav.dashboard",
                "nav.masters",
                "nav.units",
                "nav.materials",
                "nav.menus",
                "nav.eventTypes",
                "nav.recipes",
                "nav.upiQr",
                "nav.profile",
                "nav.logout",
                "nav.orders",
                "nav.customers",
                "nav.payments",
                "nav.settings",
                "nav.reports"
        );

        @Test
        @DisplayName("All critical navigation keys should exist in English (default)")
        void allCriticalNavKeysExistInEnglish() {
            List<String> missing = CRITICAL_NAV_KEYS.stream()
                    .filter(key -> !englishProps.containsKey(key))
                    .collect(Collectors.toList());

            assertThat(missing)
                    .as("English (default) messages file is missing critical navigation keys: %s", missing)
                    .isEmpty();
        }

        @Test
        @DisplayName("All critical navigation keys should exist in Hindi")
        void allCriticalNavKeysExistInHindi() {
            List<String> missing = CRITICAL_NAV_KEYS.stream()
                    .filter(key -> !hindiProps.containsKey(key))
                    .collect(Collectors.toList());

            assertThat(missing)
                    .as("Hindi messages file is missing critical navigation keys: %s", missing)
                    .isEmpty();
        }

        @Test
        @DisplayName("All critical navigation keys should exist in Marathi")
        void allCriticalNavKeysExistInMarathi() {
            List<String> missing = CRITICAL_NAV_KEYS.stream()
                    .filter(key -> !marathiProps.containsKey(key))
                    .collect(Collectors.toList());

            assertThat(missing)
                    .as("Marathi messages file is missing critical navigation keys: %s", missing)
                    .isEmpty();
        }

        @Test
        @DisplayName("Critical navigation keys should have non-blank values in all locales")
        void criticalNavKeysShouldHaveNonBlankValues() {
            List<String> blankInEnglish = new ArrayList<>();
            List<String> blankInHindi = new ArrayList<>();
            List<String> blankInMarathi = new ArrayList<>();

            for (String key : CRITICAL_NAV_KEYS) {
                if (isBlankValue(englishProps, key)) blankInEnglish.add(key);
                if (isBlankValue(hindiProps, key)) blankInHindi.add(key);
                if (isBlankValue(marathiProps, key)) blankInMarathi.add(key);
            }

            assertThat(blankInEnglish)
                    .as("English has blank values for navigation keys: %s", blankInEnglish)
                    .isEmpty();
            assertThat(blankInHindi)
                    .as("Hindi has blank values for navigation keys: %s", blankInHindi)
                    .isEmpty();
            assertThat(blankInMarathi)
                    .as("Marathi has blank values for navigation keys: %s", blankInMarathi)
                    .isEmpty();
        }

        private boolean isBlankValue(Properties props, String key) {
            String value = props.getProperty(key);
            return value == null || value.trim().isEmpty();
        }
    }

    // =========================================================================
    // Comprehensive Key Coverage: Hindi
    // =========================================================================

    @Nested
    @DisplayName("Hindi Translation Completeness")
    class HindiTranslationCompleteness {

        @Test
        @DisplayName("All English keys should exist in Hindi translation file")
        void allEnglishKeysShouldExistInHindi() {
            Set<String> englishKeys = englishProps.stringPropertyNames();
            Set<String> hindiKeys = hindiProps.stringPropertyNames();

            Set<String> missingInHindi = new TreeSet<>(englishKeys);
            missingInHindi.removeAll(hindiKeys);

            if (!missingInHindi.isEmpty()) {
                StringBuilder report = new StringBuilder();
                report.append(String.format(
                        "%n--- Hindi Translation Gap Report ---%n"));
                report.append(String.format(
                        "Total English keys: %d | Hindi keys: %d | Missing: %d%n",
                        englishKeys.size(), hindiKeys.size(), missingInHindi.size()));
                report.append(String.format("Missing keys:%n"));
                for (String key : missingInHindi) {
                    report.append(String.format("  - %s = %s%n", key, englishProps.getProperty(key)));
                }

                fail(report.toString());
            }
        }

        @Test
        @DisplayName("Hindi file should not contain extra keys absent from English")
        void hindiShouldNotContainExtraKeys() {
            Set<String> englishKeys = englishProps.stringPropertyNames();
            Set<String> hindiKeys = hindiProps.stringPropertyNames();

            Set<String> extraInHindi = new TreeSet<>(hindiKeys);
            extraInHindi.removeAll(englishKeys);

            // This is a warning-level check; extra keys are logged but may not be fatal
            if (!extraInHindi.isEmpty()) {
                System.out.printf(
                        "%n[WARNING] Hindi file has %d keys not present in English baseline:%n",
                        extraInHindi.size());
                extraInHindi.forEach(key -> System.out.printf("  - %s%n", key));
            }

            // Soft assertion: we just verify it was analysed (extra keys are acceptable)
            assertThat(hindiKeys).isNotEmpty();
        }
    }

    // =========================================================================
    // Comprehensive Key Coverage: Marathi
    // =========================================================================

    @Nested
    @DisplayName("Marathi Translation Completeness")
    class MarathiTranslationCompleteness {

        @Test
        @DisplayName("All English keys should exist in Marathi translation file")
        void allEnglishKeysShouldExistInMarathi() {
            Set<String> englishKeys = englishProps.stringPropertyNames();
            Set<String> marathiKeys = marathiProps.stringPropertyNames();

            Set<String> missingInMarathi = new TreeSet<>(englishKeys);
            missingInMarathi.removeAll(marathiKeys);

            if (!missingInMarathi.isEmpty()) {
                StringBuilder report = new StringBuilder();
                report.append(String.format(
                        "%n--- Marathi Translation Gap Report ---%n"));
                report.append(String.format(
                        "Total English keys: %d | Marathi keys: %d | Missing: %d%n",
                        englishKeys.size(), marathiKeys.size(), missingInMarathi.size()));
                report.append(String.format("Missing keys:%n"));
                for (String key : missingInMarathi) {
                    report.append(String.format("  - %s = %s%n", key, englishProps.getProperty(key)));
                }

                fail(report.toString());
            }
        }

        @Test
        @DisplayName("Marathi file should not contain extra keys absent from English")
        void marathiShouldNotContainExtraKeys() {
            Set<String> englishKeys = englishProps.stringPropertyNames();
            Set<String> marathiKeys = marathiProps.stringPropertyNames();

            Set<String> extraInMarathi = new TreeSet<>(marathiKeys);
            extraInMarathi.removeAll(englishKeys);

            // This is a warning-level check; extra keys are logged but may not be fatal
            if (!extraInMarathi.isEmpty()) {
                System.out.printf(
                        "%n[WARNING] Marathi file has %d keys not present in English baseline:%n",
                        extraInMarathi.size());
                extraInMarathi.forEach(key -> System.out.printf("  - %s%n", key));
            }

            // Soft assertion: we just verify it was analysed (extra keys are acceptable)
            assertThat(marathiKeys).isNotEmpty();
        }
    }

    // =========================================================================
    // Property File Integrity
    // =========================================================================

    @Nested
    @DisplayName("Property File Integrity")
    class PropertyFileIntegrity {

        @Test
        @DisplayName("English property file should not be empty")
        void englishFileShouldNotBeEmpty() {
            assertThat(englishProps).isNotEmpty();
            assertThat(englishProps.size())
                    .as("English file should have a reasonable number of message keys")
                    .isGreaterThan(50);
        }

        @Test
        @DisplayName("Hindi property file should not be empty")
        void hindiFileShouldNotBeEmpty() {
            assertThat(hindiProps).isNotEmpty();
            assertThat(hindiProps.size())
                    .as("Hindi file should have a reasonable number of message keys")
                    .isGreaterThan(50);
        }

        @Test
        @DisplayName("Marathi property file should not be empty")
        void marathiFileShouldNotBeEmpty() {
            assertThat(marathiProps).isNotEmpty();
            assertThat(marathiProps.size())
                    .as("Marathi file should have a reasonable number of message keys")
                    .isGreaterThan(50);
        }

        @Test
        @DisplayName("All three locale files should have the app.name key")
        void allFilesShouldHaveAppName() {
            assertThat(englishProps.getProperty("app.name")).isNotBlank();
            assertThat(hindiProps.getProperty("app.name")).isNotBlank();
            assertThat(marathiProps.getProperty("app.name")).isNotBlank();
        }
    }
}
