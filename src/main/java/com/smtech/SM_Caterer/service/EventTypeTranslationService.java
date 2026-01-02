package com.smtech.SM_Caterer.service;

import com.smtech.SM_Caterer.domain.enums.LanguageCode;
import com.smtech.SM_Caterer.service.base.BaseService;
import com.smtech.SM_Caterer.service.dto.EventTypeTranslationDTO;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for EventTypeTranslation operations.
 */
public interface EventTypeTranslationService extends BaseService<EventTypeTranslationDTO, Long> {

    Optional<EventTypeTranslationDTO> findByEventTypeIdAndLanguageCode(Long eventTypeId, LanguageCode languageCode);

    List<EventTypeTranslationDTO> findByEventTypeId(Long eventTypeId);
}
