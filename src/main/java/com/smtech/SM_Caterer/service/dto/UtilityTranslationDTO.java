package com.smtech.SM_Caterer.service.dto;

import com.smtech.SM_Caterer.domain.enums.LanguageCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * DTO for UtilityTranslation entity.
 * Provides multi-language support for utility names.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UtilityTranslationDTO extends BaseDTO {

    private Long utilityId;

    @NotNull(message = "Language code is required")
    private LanguageCode languageCode;

    @NotBlank(message = "Utility name is required")
    @Size(max = 200, message = "Utility name must not exceed 200 characters")
    private String utilityName;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
}
