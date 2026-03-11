package com.smtech.SM_Caterer.web.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for tenant branding settings.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandingSettingsDTO {

    @Size(max = 200, message = "Display name must not exceed 200 characters")
    private String displayName;

    @Size(max = 200, message = "Tagline must not exceed 200 characters")
    private String tagline;

    @Pattern(regexp = "^$|^#[0-9A-Fa-f]{6}$", message = "Primary color must be a valid hex color (e.g., #3498db)")
    private String primaryColor;

    private String logoPath;

    private String businessName; // Read-only, for display

    private boolean hasLogo;
}
