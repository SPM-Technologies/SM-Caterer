package com.smtech.SM_Caterer.domain.repository;

import com.smtech.SM_Caterer.domain.entity.EventTypeTranslation;
import com.smtech.SM_Caterer.domain.enums.LanguageCode;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for EventTypeTranslation entity.
 */
@Repository
public interface EventTypeTranslationRepository extends BaseRepository<EventTypeTranslation, Long> {

    /**
     * Finds translation by event type and language.
     * @param eventTypeId Event type ID
     * @param languageCode Language code
     * @return Translation if found
     */
    Optional<EventTypeTranslation> findByEventTypeIdAndLanguageCode(Long eventTypeId, LanguageCode languageCode);

    /**
     * Finds all translations for event type.
     * @param eventTypeId Event type ID
     * @return List of translations
     */
    List<EventTypeTranslation> findByEventTypeId(Long eventTypeId);

    /**
     * Finds translations by language.
     * @param languageCode Language code
     * @return List of translations
     */
    List<EventTypeTranslation> findByLanguageCode(LanguageCode languageCode);

    /**
     * Checks if translation exists for event type and language.
     */
    boolean existsByEventTypeIdAndLanguageCode(Long eventTypeId, LanguageCode languageCode);

    /**
     * Deletes all translations for event type.
     */
    void deleteByEventTypeId(Long eventTypeId);
}
