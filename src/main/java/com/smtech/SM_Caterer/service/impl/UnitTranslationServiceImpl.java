package com.smtech.SM_Caterer.service.impl;

import com.smtech.SM_Caterer.domain.entity.Unit;
import com.smtech.SM_Caterer.domain.entity.UnitTranslation;
import com.smtech.SM_Caterer.domain.enums.LanguageCode;
import com.smtech.SM_Caterer.domain.repository.UnitRepository;
import com.smtech.SM_Caterer.domain.repository.UnitTranslationRepository;
import com.smtech.SM_Caterer.exception.DuplicateResourceException;
import com.smtech.SM_Caterer.exception.ResourceNotFoundException;
import com.smtech.SM_Caterer.service.UnitTranslationService;
import com.smtech.SM_Caterer.service.base.BaseServiceImpl;
import com.smtech.SM_Caterer.service.dto.UnitTranslationDTO;
import com.smtech.SM_Caterer.service.mapper.EntityMapper;
import com.smtech.SM_Caterer.service.mapper.UnitTranslationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for UnitTranslation operations.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UnitTranslationServiceImpl extends BaseServiceImpl<UnitTranslation, UnitTranslationDTO, Long>
        implements UnitTranslationService {

    private final UnitTranslationRepository unitTranslationRepository;
    private final UnitTranslationMapper unitTranslationMapper;
    private final UnitRepository unitRepository;

    @Override
    protected JpaRepository<UnitTranslation, Long> getRepository() {
        return unitTranslationRepository;
    }

    @Override
    protected EntityMapper<UnitTranslationDTO, UnitTranslation> getMapper() {
        return unitTranslationMapper;
    }

    @Override
    protected String getEntityName() {
        return "UnitTranslation";
    }

    @Override
    @Transactional
    public UnitTranslationDTO create(UnitTranslationDTO dto) {
        log.debug("Creating new unit translation for unit ID: {} in language: {}",
                dto.getUnitId(), dto.getLanguageCode());

        // Validate unique constraint
        if (unitTranslationRepository.existsByUnitIdAndLanguageCode(
                dto.getUnitId(), dto.getLanguageCode())) {
            throw new DuplicateResourceException("UnitTranslation",
                    "unitId+languageCode", dto.getUnitId() + "+" + dto.getLanguageCode());
        }

        UnitTranslation entity = unitTranslationMapper.toEntity(dto);

        // Set unit reference
        if (dto.getUnitId() != null) {
            Unit unit = unitRepository.findById(dto.getUnitId())
                    .orElseThrow(() -> new ResourceNotFoundException("Unit", "id", dto.getUnitId()));
            entity.setUnit(unit);
        }

        UnitTranslation saved = unitTranslationRepository.save(entity);
        log.info("UnitTranslation created (ID: {})", saved.getId());

        return unitTranslationMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UnitTranslationDTO> findByUnitIdAndLanguageCode(Long unitId, LanguageCode languageCode) {
        return unitTranslationRepository.findByUnitIdAndLanguageCode(unitId, languageCode)
                .map(unitTranslationMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitTranslationDTO> findByUnitId(Long unitId) {
        return unitTranslationMapper.toDto(unitTranslationRepository.findByUnitId(unitId));
    }

    @Transactional(readOnly = true)
    public List<UnitTranslationDTO> findByLanguageCode(LanguageCode languageCode) {
        return unitTranslationMapper.toDto(unitTranslationRepository.findByLanguageCode(languageCode));
    }
}
