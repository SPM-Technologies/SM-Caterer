package com.smtech.SM_Caterer.service;

import com.smtech.SM_Caterer.domain.enums.LanguageCode;
import com.smtech.SM_Caterer.service.base.BaseService;
import com.smtech.SM_Caterer.service.dto.UnitTranslationDTO;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for UnitTranslation operations.
 */
public interface UnitTranslationService extends BaseService<UnitTranslationDTO, Long> {

    Optional<UnitTranslationDTO> findByUnitIdAndLanguageCode(Long unitId, LanguageCode languageCode);

    List<UnitTranslationDTO> findByUnitId(Long unitId);
}
