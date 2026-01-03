package com.smtech.SM_Caterer.API.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login request DTO.
 * Contains credentials for authentication.
 *
 * @author CloudCaters Team
 * @version 1.0
 * @since Phase 2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    /**
     * Username or email address.
     */
    @NotBlank(message = "Username or email is required")
    private String username;

    /**
     * User password.
     */
    @NotBlank(message = "Password is required")
    private String password;
}
