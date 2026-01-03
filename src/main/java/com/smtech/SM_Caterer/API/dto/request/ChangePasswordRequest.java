package com.smtech.SM_Caterer.API.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Change password request DTO.
 * Contains current and new password for password change.
 *
 * @author CloudCaters Team
 * @version 1.0
 * @since Phase 2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {

    /**
     * Current password for verification.
     */
    @NotBlank(message = "Current password is required")
    private String currentPassword;

    /**
     * New password.
     */
    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 100, message = "Password must be 8-100 characters")
    private String newPassword;

    /**
     * New password confirmation.
     */
    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;
}
