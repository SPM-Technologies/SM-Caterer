package com.smtech.SM_Caterer.domain.repository;

import com.smtech.SM_Caterer.domain.entity.UnitTranslation;
import com.smtech.SM_Caterer.domain.enums.LanguageCode;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for UnitTranslation entity.
 */
@Repository
public interface UnitTranslationRepository extends BaseRepository<UnitTranslation, Long> {

    /**
     * Finds translation by unit and language.
     * @param unitId Unit ID
     * @param languageCode Language code
     * @return Translation if found
     */
    Optional<UnitTranslation> findByUnitIdAndLanguageCode(Long unitId, LanguageCode languageCode);

    /**
     * Finds all translations for unit.
     * @param unitId Unit ID
     * @return List of translations
     */
    List<UnitTranslation> findByUnitId(Long unitId);

    /**
     * Finds translations by language.
     * @param languageCode Language code
     * @return List of translations
     */
    List<UnitTranslation> findByLanguageCode(LanguageCode languageCode);

    /**
     * Checks if translation exists for unit and language.
     */
    boolean existsByUnitIdAndLanguageCode(Long unitId, LanguageCode languageCode);

    /**
     * Deletes all translations for unit.
     */
    void deleteByUnitId(Long unitId);
}
