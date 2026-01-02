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
 * DTO for EventTypeTranslation entity.
 * Provides multi-language support for event type names.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventTypeTranslationDTO extends BaseDTO {

    private Long eventTypeId;

    @NotNull(message = "Language code is required")
    private LanguageCode languageCode;

    @NotBlank(message = "Event type name is required")
    @Size(max = 100, message = "Event type name must not exceed 100 characters")
    private String eventTypeName;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
}
