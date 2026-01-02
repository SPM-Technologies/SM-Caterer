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
 * DTO for MaterialTranslation entity.
 * Provides multi-language support for material names.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialTranslationDTO extends BaseDTO {

    private Long materialId;

    @NotNull(message = "Language code is required")
    private LanguageCode languageCode;

    @NotBlank(message = "Material name is required")
    @Size(max = 200, message = "Material name must not exceed 200 characters")
    private String materialName;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
}
