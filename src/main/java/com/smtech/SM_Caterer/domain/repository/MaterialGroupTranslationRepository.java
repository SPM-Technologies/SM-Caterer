package com.smtech.SM_Caterer.domain.repository;

import com.smtech.SM_Caterer.domain.entity.MaterialGroupTranslation;
import com.smtech.SM_Caterer.domain.enums.LanguageCode;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for MaterialGroupTranslation entity.
 */
@Repository
public interface MaterialGroupTranslationRepository extends BaseRepository<MaterialGroupTranslation, Long> {

    /**
     * Finds translation by material group and language.
     * @param materialGroupId Material group ID
     * @param languageCode Language code
     * @return Translation if found
     */
    Optional<MaterialGroupTranslation> findByMaterialGroupIdAndLanguageCode(Long materialGroupId, LanguageCode languageCode);

    /**
     * Finds all translations for material group.
     * @param materialGroupId Material group ID
     * @return List of translations
     */
    List<MaterialGroupTranslation> findByMaterialGroupId(Long materialGroupId);

    /**
     * Finds translations by language.
     * @param languageCode Language code
     * @return List of translations
     */
    List<MaterialGroupTranslation> findByLanguageCode(LanguageCode languageCode);

    /**
     * Checks if translation exists for material group and language.
     */
    boolean existsByMaterialGroupIdAndLanguageCode(Long materialGroupId, LanguageCode languageCode);

    /**
     * Deletes all translations for material group.
     */
    void deleteByMaterialGroupId(Long materialGroupId);
}
