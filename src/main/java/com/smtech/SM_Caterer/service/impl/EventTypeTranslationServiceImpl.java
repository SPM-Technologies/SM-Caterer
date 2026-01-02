package com.smtech.SM_Caterer.service.impl;

import com.smtech.SM_Caterer.domain.entity.EventType;
import com.smtech.SM_Caterer.domain.enums.LanguageCode;
import com.smtech.SM_Caterer.domain.entity.EventTypeTranslation;
import com.smtech.SM_Caterer.domain.repository.EventTypeRepository;
import com.smtech.SM_Caterer.domain.repository.EventTypeTranslationRepository;
import com.smtech.SM_Caterer.exception.DuplicateResourceException;
import com.smtech.SM_Caterer.exception.ResourceNotFoundException;
import com.smtech.SM_Caterer.service.EventTypeTranslationService;
import com.smtech.SM_Caterer.service.base.BaseServiceImpl;
import com.smtech.SM_Caterer.service.dto.EventTypeTranslationDTO;
import com.smtech.SM_Caterer.service.mapper.EntityMapper;
import com.smtech.SM_Caterer.service.mapper.EventTypeTranslationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for EventTypeTranslation operations.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class EventTypeTranslationServiceImpl extends BaseServiceImpl<EventTypeTranslation, EventTypeTranslationDTO, Long>
        implements EventTypeTranslationService {

    private final EventTypeTranslationRepository eventTypeTranslationRepository;
    private final EventTypeTranslationMapper eventTypeTranslationMapper;
    private final EventTypeRepository eventTypeRepository;

    @Override
    protected JpaRepository<EventTypeTranslation, Long> getRepository() {
        return eventTypeTranslationRepository;
    }

    @Override
    protected EntityMapper<EventTypeTranslationDTO, EventTypeTranslation> getMapper() {
        return eventTypeTranslationMapper;
    }

    @Override
    protected String getEntityName() {
        return "EventTypeTranslation";
    }

    @Override
    @Transactional
    public EventTypeTranslationDTO create(EventTypeTranslationDTO dto) {
        log.debug("Creating new event type translation for event type ID: {} in language: {}",
                dto.getEventTypeId(), dto.getLanguageCode());

        // Validate unique constraint
        if (eventTypeTranslationRepository.existsByEventTypeIdAndLanguageCode(
                dto.getEventTypeId(), dto.getLanguageCode())) {
            throw new DuplicateResourceException("EventTypeTranslation",
                    "eventTypeId+languageCode", dto.getEventTypeId() + "+" + dto.getLanguageCode());
        }

        EventTypeTranslation entity = eventTypeTranslationMapper.toEntity(dto);

        // Set event type reference
        if (dto.getEventTypeId() != null) {
            EventType eventType = eventTypeRepository.findById(dto.getEventTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("EventType", "id", dto.getEventTypeId()));
            entity.setEventType(eventType);
        }

        EventTypeTranslation saved = eventTypeTranslationRepository.save(entity);
        log.info("EventTypeTranslation created (ID: {})", saved.getId());

        return eventTypeTranslationMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EventTypeTranslationDTO> findByEventTypeIdAndLanguageCode(Long eventTypeId, LanguageCode languageCode) {
        return eventTypeTranslationRepository.findByEventTypeIdAndLanguageCode(eventTypeId, languageCode)
                .map(eventTypeTranslationMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventTypeTranslationDTO> findByEventTypeId(Long eventTypeId) {
        return eventTypeTranslationMapper.toDto(
                eventTypeTranslationRepository.findByEventTypeId(eventTypeId));
    }

    @Transactional(readOnly = true)
    public List<EventTypeTranslationDTO> findByLanguageCode(LanguageCode languageCode) {
        return eventTypeTranslationMapper.toDto(
                eventTypeTranslationRepository.findByLanguageCode(languageCode));
    }
}
