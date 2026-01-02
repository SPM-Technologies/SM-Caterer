package com.smtech.SM_Caterer.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smtech.SM_Caterer.domain.enums.LanguageCode;
import com.smtech.SM_Caterer.domain.enums.UserRole;
import com.smtech.SM_Caterer.domain.enums.UserStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for User entity.
 *
 * SECURITY:
 * - Password is write-only (JsonProperty.Access.WRITE_ONLY)
 * - Never returned in API responses
 * - Must be BCrypt encoded before saving
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO extends BaseDTO {

    // Tenant reference (ID only, not full object)
    private Long tenantId;
    private String tenantCode; // For display purposes

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be 3-50 characters")
    @Pattern(regexp = "^[a-z0-9._-]+$",
             message = "Username must be lowercase alphanumeric with . _ -")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    /**
     * Password field - WRITE ONLY.
     * Never returned in responses.
     * Must be BCrypt encoded before persisting.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Size(min = 8, max = 100, message = "Password must be 8-100 characters")
    private String password;

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
    private String phone;

    @NotNull(message = "Role is required")
    private UserRole role;

    @NotNull(message = "Status is required")
    private UserStatus status;

    private LanguageCode languagePreference;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime lastLogin;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer failedLoginAttempts;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime passwordChangedAt;
}
