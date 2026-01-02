package com.smtech.SM_Caterer.service;

import com.smtech.SM_Caterer.domain.enums.LanguageCode;
import com.smtech.SM_Caterer.service.base.BaseService;
import com.smtech.SM_Caterer.service.dto.MaterialGroupTranslationDTO;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for MaterialGroupTranslation operations.
 */
public interface MaterialGroupTranslationService extends BaseService<MaterialGroupTranslationDTO, Long> {

    /**
     * Finds translation by material group and language.
     * @param materialGroupId Material group ID
     * @param languageCode Language code
     * @return Translation if found
     */
    Optional<MaterialGroupTranslationDTO> findByMaterialGroupIdAndLanguageCode(Long materialGroupId, LanguageCode languageCode);

    /**
     * Finds all translations for a material group.
     * @param materialGroupId Material group ID
     * @return List of translations
     */
    List<MaterialGroupTranslationDTO> findByMaterialGroupId(Long materialGroupId);
}
