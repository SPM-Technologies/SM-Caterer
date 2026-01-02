package com.smtech.SM_Caterer.service;

import com.smtech.SM_Caterer.domain.enums.LanguageCode;
import com.smtech.SM_Caterer.service.base.BaseService;
import com.smtech.SM_Caterer.service.dto.MaterialTranslationDTO;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for MaterialTranslation operations.
 */
public interface MaterialTranslationService extends BaseService<MaterialTranslationDTO, Long> {

    Optional<MaterialTranslationDTO> findByMaterialIdAndLanguageCode(Long materialId, LanguageCode languageCode);

    List<MaterialTranslationDTO> findByMaterialId(Long materialId);
}
