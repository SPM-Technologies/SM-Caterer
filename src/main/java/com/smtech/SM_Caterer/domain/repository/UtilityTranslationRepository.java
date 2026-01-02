package com.smtech.SM_Caterer.domain.repository;

import com.smtech.SM_Caterer.domain.entity.UtilityTranslation;
import com.smtech.SM_Caterer.domain.enums.LanguageCode;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for UtilityTranslation entity.
 */
@Repository
public interface UtilityTranslationRepository extends BaseRepository<UtilityTranslation, Long> {

    /**
     * Finds translation by utility and language.
     * @param utilityId Utility ID
     * @param languageCode Language code
     * @return Translation if found
     */
    Optional<UtilityTranslation> findByUtilityIdAndLanguageCode(Long utilityId, LanguageCode languageCode);

    /**
     * Finds all translations for utility.
     * @param utilityId Utility ID
     * @return List of translations
     */
    List<UtilityTranslation> findByUtilityId(Long utilityId);

    /**
     * Finds translations by language.
     * @param languageCode Language code
     * @return List of translations
     */
    List<UtilityTranslation> findByLanguageCode(LanguageCode languageCode);

    /**
     * Checks if translation exists for utility and language.
     */
    boolean existsByUtilityIdAndLanguageCode(Long utilityId, LanguageCode languageCode);

    /**
     * Deletes all translations for utility.
     */
    void deleteByUtilityId(Long utilityId);
}
