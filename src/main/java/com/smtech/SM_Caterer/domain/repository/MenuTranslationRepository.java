package com.smtech.SM_Caterer.domain.repository;

import com.smtech.SM_Caterer.domain.entity.MenuTranslation;
import com.smtech.SM_Caterer.domain.enums.LanguageCode;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for MenuTranslation entity.
 */
@Repository
public interface MenuTranslationRepository extends BaseRepository<MenuTranslation, Long> {

    /**
     * Finds translation by menu and language.
     * @param menuId Menu ID
     * @param languageCode Language code
     * @return Translation if found
     */
    Optional<MenuTranslation> findByMenuIdAndLanguageCode(Long menuId, LanguageCode languageCode);

    /**
     * Finds all translations for menu.
     * @param menuId Menu ID
     * @return List of translations
     */
    List<MenuTranslation> findByMenuId(Long menuId);

    /**
     * Finds translations by language.
     * @param languageCode Language code
     * @return List of translations
     */
    List<MenuTranslation> findByLanguageCode(LanguageCode languageCode);

    /**
     * Checks if translation exists for menu and language.
     */
    boolean existsByMenuIdAndLanguageCode(Long menuId, LanguageCode languageCode);

    /**
     * Deletes all translations for menu.
     */
    void deleteByMenuId(Long menuId);
}
