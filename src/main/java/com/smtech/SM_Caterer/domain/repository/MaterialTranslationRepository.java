package com.smtech.SM_Caterer.domain.repository;

import com.smtech.SM_Caterer.domain.entity.MaterialTranslation;
import com.smtech.SM_Caterer.domain.enums.LanguageCode;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for MaterialTranslation entity.
 */
@Repository
public interface MaterialTranslationRepository extends BaseRepository<MaterialTranslation, Long> {

    /**
     * Finds translation by material and language.
     * @param materialId Material ID
     * @param languageCode Language code
     * @return Translation if found
     */
    Optional<MaterialTranslation> findByMaterialIdAndLanguageCode(Long materialId, LanguageCode languageCode);

    /**
     * Finds all translations for material.
     * @param materialId Material ID
     * @return List of translations
     */
    List<MaterialTranslation> findByMaterialId(Long materialId);

    /**
     * Finds translations by language.
     * @param languageCode Language code
     * @return List of translations
     */
    List<MaterialTranslation> findByLanguageCode(LanguageCode languageCode);

    /**
     * Checks if translation exists for material and language.
     */
    boolean existsByMaterialIdAndLanguageCode(Long materialId, LanguageCode languageCode);

    /**
     * Deletes all translations for material.
     */
    void deleteByMaterialId(Long materialId);
}
