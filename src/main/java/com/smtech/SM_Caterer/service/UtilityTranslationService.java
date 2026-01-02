package com.smtech.SM_Caterer.service;

import com.smtech.SM_Caterer.domain.enums.LanguageCode;
import com.smtech.SM_Caterer.service.base.BaseService;
import com.smtech.SM_Caterer.service.dto.UtilityTranslationDTO;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for UtilityTranslation operations.
 */
public interface UtilityTranslationService extends BaseService<UtilityTranslationDTO, Long> {

    Optional<UtilityTranslationDTO> findByUtilityIdAndLanguageCode(Long utilityId, LanguageCode languageCode);

    List<UtilityTranslationDTO> findByUtilityId(Long utilityId);
}
