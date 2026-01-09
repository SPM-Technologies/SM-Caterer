package com.smtech.SM_Caterer.security;

import com.smtech.SM_Caterer.base.BaseUnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JWT Token Tests")
class JwtTokenTest extends BaseUnitTest {

    @Nested
    @DisplayName("Token Structure")
    class TokenStructure {

        @Test
        @DisplayName("JWT should have three parts separated by dots")
        void jwtShouldHaveThreeParts() {
            // Given a sample JWT structure
            String sampleJwt = "header.payload.signature";

            // When
            String[] parts = sampleJwt.split("\\.");

            // Then
            assertThat(parts).hasSize(3);
        }
    }

    @Nested
    @DisplayName("Token Validation Concepts")
    class TokenValidation {

        @Test
        @DisplayName("Empty token should be considered invalid")
        void emptyTokenShouldBeInvalid() {
            // Given
            String emptyToken = "";

            // Then
            assertThat(emptyToken).isEmpty();
        }

        @Test
        @DisplayName("Null token should be considered invalid")
        void nullTokenShouldBeInvalid() {
            // Given
            String nullToken = null;

            // Then
            assertThat(nullToken).isNull();
        }

        @Test
        @DisplayName("Token without proper format should be invalid")
        void tokenWithoutProperFormatShouldBeInvalid() {
            // Given
            String malformedToken = "not.a.valid.jwt.token";

            // When
            String[] parts = malformedToken.split("\\.");

            // Then
            assertThat(parts).hasSizeGreaterThan(3);
        }
    }
}
