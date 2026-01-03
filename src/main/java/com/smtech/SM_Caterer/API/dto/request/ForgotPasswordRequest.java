package com.smtech.SM_Caterer.API.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Forgot password request DTO.
 * Contains email for password reset request.
 *
 * @author CloudCaters Team
 * @version 1.0
 * @since Phase 2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordRequest {

    /**
     * Email address for password reset.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
}
