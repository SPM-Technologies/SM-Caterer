package com.smtech.SM_Caterer.service.dto;

import com.smtech.SM_Caterer.domain.enums.TenantStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for Tenant entity.
 *
 * Validation:
 * - Field-level validation
 * - Custom validation for subscription dates (see validator)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantDTO extends BaseDTO {

    @NotBlank(message = "Tenant code is required")
    @Size(max = 50, message = "Tenant code must not exceed 50 characters")
    @Pattern(regexp = "^[A-Z0-9_]+$",
             message = "Tenant code must be uppercase alphanumeric with underscores")
    private String tenantCode;

    @NotBlank(message = "Business name is required")
    @Size(max = 200, message = "Business name must not exceed 200 characters")
    private String businessName;

    @Size(max = 100, message = "Contact person must not exceed 100 characters")
    private String contactPerson;

    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
    private String phone;

    private String address;

    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;

    @Pattern(regexp = "^[0-9]{6}$", message = "Pincode must be 6 digits")
    private String pincode;

    @Pattern(regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$",
             message = "Invalid GSTIN format (e.g., 27AABCT1332L1Z5)")
    private String gstin;

    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$",
             message = "Invalid PAN format (e.g., AABCT1332L)")
    private String pan;

    @NotNull(message = "Status is required")
    private TenantStatus status;

    private LocalDate subscriptionStartDate;

    private LocalDate subscriptionEndDate;

    /**
     * Helper method for validation.
     * @return true if subscription dates are valid
     */
    public boolean isSubscriptionValid() {
        if (subscriptionStartDate == null || subscriptionEndDate == null) {
            return true;
        }
        return subscriptionEndDate.isAfter(subscriptionStartDate);
    }
}
