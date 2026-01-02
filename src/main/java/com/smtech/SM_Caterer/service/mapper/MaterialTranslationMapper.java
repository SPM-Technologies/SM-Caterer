package com.smtech.SM_Caterer.service.mapper;

import com.smtech.SM_Caterer.domain.entity.MaterialTranslation;
import com.smtech.SM_Caterer.service.dto.MaterialTranslationDTO;
import org.mapstruct.*;

/**
 * Mapper for MaterialTranslation entity.
 */
@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.WARN
)
public interface MaterialTranslationMapper extends EntityMapper<MaterialTranslationDTO, MaterialTranslation> {

    @Mapping(target = "materialId", source = "material.id")
    @Override
    MaterialTranslationDTO toDto(MaterialTranslation entity);

    @Mapping(target = "material", ignore = true)
    @Override
    MaterialTranslation toEntity(MaterialTranslationDTO dto);
}
