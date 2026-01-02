package com.smtech.SM_Caterer.service.impl;

import com.smtech.SM_Caterer.domain.entity.Material;
import com.smtech.SM_Caterer.domain.enums.LanguageCode;
import com.smtech.SM_Caterer.domain.entity.MaterialTranslation;
import com.smtech.SM_Caterer.domain.repository.MaterialRepository;
import com.smtech.SM_Caterer.domain.repository.MaterialTranslationRepository;
import com.smtech.SM_Caterer.exception.DuplicateResourceException;
import com.smtech.SM_Caterer.exception.ResourceNotFoundException;
import com.smtech.SM_Caterer.service.MaterialTranslationService;
import com.smtech.SM_Caterer.service.base.BaseServiceImpl;
import com.smtech.SM_Caterer.service.dto.MaterialTranslationDTO;
import com.smtech.SM_Caterer.service.mapper.EntityMapper;
import com.smtech.SM_Caterer.service.mapper.MaterialTranslationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for MaterialTranslation operations.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MaterialTranslationServiceImpl extends BaseServiceImpl<MaterialTranslation, MaterialTranslationDTO, Long>
        implements MaterialTranslationService {

    private final MaterialTranslationRepository materialTranslationRepository;
    private final MaterialTranslationMapper materialTranslationMapper;
    private final MaterialRepository materialRepository;

    @Override
    protected JpaRepository<MaterialTranslation, Long> getRepository() {
        return materialTranslationRepository;
    }

    @Override
    protected EntityMapper<MaterialTranslationDTO, MaterialTranslation> getMapper() {
        return materialTranslationMapper;
    }

    @Override
    protected String getEntityName() {
        return "MaterialTranslation";
    }

    @Override
    @Transactional
    public MaterialTranslationDTO create(MaterialTranslationDTO dto) {
        log.debug("Creating new material translation for material ID: {} in language: {}",
                dto.getMaterialId(), dto.getLanguageCode());

        // Validate unique constraint
        if (materialTranslationRepository.existsByMaterialIdAndLanguageCode(
                dto.getMaterialId(), dto.getLanguageCode())) {
            throw new DuplicateResourceException("MaterialTranslation",
                    "materialId+languageCode", dto.getMaterialId() + "+" + dto.getLanguageCode());
        }

        MaterialTranslation entity = materialTranslationMapper.toEntity(dto);

        // Set material reference
        if (dto.getMaterialId() != null) {
            Material material = materialRepository.findById(dto.getMaterialId())
                    .orElseThrow(() -> new ResourceNotFoundException("Material", "id", dto.getMaterialId()));
            entity.setMaterial(material);
        }

        MaterialTranslation saved = materialTranslationRepository.save(entity);
        log.info("MaterialTranslation created (ID: {})", saved.getId());

        return materialTranslationMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MaterialTranslationDTO> findByMaterialIdAndLanguageCode(Long materialId, LanguageCode languageCode) {
        return materialTranslationRepository.findByMaterialIdAndLanguageCode(materialId, languageCode)
                .map(materialTranslationMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaterialTranslationDTO> findByMaterialId(Long materialId) {
        return materialTranslationMapper.toDto(
                materialTranslationRepository.findByMaterialId(materialId));
    }

    @Transactional(readOnly = true)
    public List<MaterialTranslationDTO> findByLanguageCode(LanguageCode languageCode) {
        return materialTranslationMapper.toDto(
                materialTranslationRepository.findByLanguageCode(languageCode));
    }
}
