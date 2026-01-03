package com.smtech.SM_Caterer.API.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Refresh token request DTO.
 * Contains refresh token for obtaining new access token.
 *
 * @author CloudCaters Team
 * @version 1.0
 * @since Phase 2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequest {

    /**
     * JWT refresh token.
     */
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
