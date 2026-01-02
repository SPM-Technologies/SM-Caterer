package com.smtech.SM_Caterer.service.impl;

import com.smtech.SM_Caterer.domain.entity.MaterialGroup;
import com.smtech.SM_Caterer.domain.entity.MaterialGroupTranslation;
import com.smtech.SM_Caterer.domain.enums.LanguageCode;
import com.smtech.SM_Caterer.domain.repository.MaterialGroupRepository;
import com.smtech.SM_Caterer.domain.repository.MaterialGroupTranslationRepository;
import com.smtech.SM_Caterer.exception.ResourceNotFoundException;
import com.smtech.SM_Caterer.service.MaterialGroupTranslationService;
import com.smtech.SM_Caterer.service.base.BaseServiceImpl;
import com.smtech.SM_Caterer.service.dto.MaterialGroupTranslationDTO;
import com.smtech.SM_Caterer.service.mapper.EntityMapper;
import com.smtech.SM_Caterer.service.mapper.MaterialGroupTranslationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for MaterialGroupTranslation operations.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MaterialGroupTranslationServiceImpl extends BaseServiceImpl<MaterialGroupTranslation, MaterialGroupTranslationDTO, Long>
        implements MaterialGroupTranslationService {

    private final MaterialGroupTranslationRepository translationRepository;
    private final MaterialGroupTranslationMapper translationMapper;
    private final MaterialGroupRepository materialGroupRepository;

    @Override
    protected JpaRepository<MaterialGroupTranslation, Long> getRepository() {
        return translationRepository;
    }

    @Override
    protected EntityMapper<MaterialGroupTranslationDTO, MaterialGroupTranslation> getMapper() {
        return translationMapper;
    }

    @Override
    protected String getEntityName() {
        return "MaterialGroupTranslation";
    }

    @Override
    @Transactional
    public MaterialGroupTranslationDTO create(MaterialGroupTranslationDTO dto) {
        MaterialGroupTranslation entity = translationMapper.toEntity(dto);

        // Set material group
        if (dto.getMaterialGroupId() != null) {
            MaterialGroup materialGroup = materialGroupRepository.findById(dto.getMaterialGroupId())
                    .orElseThrow(() -> new ResourceNotFoundException("MaterialGroup", "id", dto.getMaterialGroupId()));
            entity.setMaterialGroup(materialGroup);
        }

        MaterialGroupTranslation saved = translationRepository.save(entity);
        return translationMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MaterialGroupTranslationDTO> findByMaterialGroupIdAndLanguageCode(Long materialGroupId, LanguageCode languageCode) {
        return translationRepository.findByMaterialGroupIdAndLanguageCode(materialGroupId, languageCode)
                .map(translationMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaterialGroupTranslationDTO> findByMaterialGroupId(Long materialGroupId) {
        return translationMapper.toDto(translationRepository.findByMaterialGroupId(materialGroupId));
    }
}
