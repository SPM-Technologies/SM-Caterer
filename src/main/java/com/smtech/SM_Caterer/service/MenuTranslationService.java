package com.smtech.SM_Caterer.service;

import com.smtech.SM_Caterer.domain.enums.LanguageCode;
import com.smtech.SM_Caterer.service.base.BaseService;
import com.smtech.SM_Caterer.service.dto.MenuTranslationDTO;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for MenuTranslation operations.
 */
public interface MenuTranslationService extends BaseService<MenuTranslationDTO, Long> {

    Optional<MenuTranslationDTO> findByMenuIdAndLanguageCode(Long menuId, LanguageCode languageCode);

    List<MenuTranslationDTO> findByMenuId(Long menuId);
}
