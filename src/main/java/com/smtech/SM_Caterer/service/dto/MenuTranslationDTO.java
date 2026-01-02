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
 * DTO for MenuTranslation entity.
 * Provides multi-language support for menu names.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuTranslationDTO extends BaseDTO {

    private Long menuId;

    @NotNull(message = "Language code is required")
    private LanguageCode languageCode;

    @NotBlank(message = "Menu name is required")
    @Size(max = 200, message = "Menu name must not exceed 200 characters")
    private String menuName;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
}
