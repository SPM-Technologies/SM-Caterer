package com.smtech.SM_Caterer.service.impl;

import com.smtech.SM_Caterer.domain.entity.Utility;
import com.smtech.SM_Caterer.domain.enums.LanguageCode;
import com.smtech.SM_Caterer.domain.entity.UtilityTranslation;
import com.smtech.SM_Caterer.domain.repository.UtilityRepository;
import com.smtech.SM_Caterer.domain.repository.UtilityTranslationRepository;
import com.smtech.SM_Caterer.exception.DuplicateResourceException;
import com.smtech.SM_Caterer.exception.ResourceNotFoundException;
import com.smtech.SM_Caterer.service.UtilityTranslationService;
import com.smtech.SM_Caterer.service.base.BaseServiceImpl;
import com.smtech.SM_Caterer.service.dto.UtilityTranslationDTO;
import com.smtech.SM_Caterer.service.mapper.EntityMapper;
import com.smtech.SM_Caterer.service.mapper.UtilityTranslationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for UtilityTranslation operations.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UtilityTranslationServiceImpl extends BaseServiceImpl<UtilityTranslation, UtilityTranslationDTO, Long>
        implements UtilityTranslationService {

    private final UtilityTranslationRepository utilityTranslationRepository;
    private final UtilityTranslationMapper utilityTranslationMapper;
    private final UtilityRepository utilityRepository;

    @Override
    protected JpaRepository<UtilityTranslation, Long> getRepository() {
        return utilityTranslationRepository;
    }

    @Override
    protected EntityMapper<UtilityTranslationDTO, UtilityTranslation> getMapper() {
        return utilityTranslationMapper;
    }

    @Override
    protected String getEntityName() {
        return "UtilityTranslation";
    }

    @Override
    @Transactional
    public UtilityTranslationDTO create(UtilityTranslationDTO dto) {
        log.debug("Creating new utility translation for utility ID: {} in language: {}",
                dto.getUtilityId(), dto.getLanguageCode());

        // Validate unique constraint
        if (utilityTranslationRepository.existsByUtilityIdAndLanguageCode(
                dto.getUtilityId(), dto.getLanguageCode())) {
            throw new DuplicateResourceException("UtilityTranslation",
                    "utilityId+languageCode", dto.getUtilityId() + "+" + dto.getLanguageCode());
        }

        UtilityTranslation entity = utilityTranslationMapper.toEntity(dto);

        // Set utility reference
        if (dto.getUtilityId() != null) {
            Utility utility = utilityRepository.findById(dto.getUtilityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Utility", "id", dto.getUtilityId()));
            entity.setUtility(utility);
        }

        UtilityTranslation saved = utilityTranslationRepository.save(entity);
        log.info("UtilityTranslation created (ID: {})", saved.getId());

        return utilityTranslationMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UtilityTranslationDTO> findByUtilityIdAndLanguageCode(Long utilityId, LanguageCode languageCode) {
        return utilityTranslationRepository.findByUtilityIdAndLanguageCode(utilityId, languageCode)
                .map(utilityTranslationMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UtilityTranslationDTO> findByUtilityId(Long utilityId) {
        return utilityTranslationMapper.toDto(utilityTranslationRepository.findByUtilityId(utilityId));
    }

    @Transactional(readOnly = true)
    public List<UtilityTranslationDTO> findByLanguageCode(LanguageCode languageCode) {
        return utilityTranslationMapper.toDto(utilityTranslationRepository.findByLanguageCode(languageCode));
    }
}
