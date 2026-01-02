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
 * DTO for UnitTranslation entity.
 * Provides multi-language support for unit names.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnitTranslationDTO extends BaseDTO {

    private Long unitId;

    @NotNull(message = "Language code is required")
    private LanguageCode languageCode;

    @NotBlank(message = "Unit name is required")
    @Size(max = 100, message = "Unit name must not exceed 100 characters")
    private String unitName;

    @Size(max = 20, message = "Symbol must not exceed 20 characters")
    private String symbol;
}
