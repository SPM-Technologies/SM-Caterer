package com.smtech.SM_Caterer.API.dto.response;

import com.smtech.SM_Caterer.service.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Authentication response containing JWT tokens and user info.
 *
 * @author CloudCaters Team
 * @version 1.0
 * @since Phase 2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    /**
     * JWT access token.
     */
    private String accessToken;

    /**
     * JWT refresh token.
     */
    private String refreshToken;

    /**
     * Token type (always "Bearer").
     */
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * Access token expiration time in seconds.
     */
    private Long expiresIn;

    /**
     * Authenticated user information.
     */
    private UserDTO user;
}
