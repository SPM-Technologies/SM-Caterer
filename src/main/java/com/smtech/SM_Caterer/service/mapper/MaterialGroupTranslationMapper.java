package com.smtech.SM_Caterer.service.mapper;

import com.smtech.SM_Caterer.domain.entity.MaterialGroupTranslation;
import com.smtech.SM_Caterer.service.dto.MaterialGroupTranslationDTO;
import org.mapstruct.*;

/**
 * Mapper for MaterialGroupTranslation entity.
 */
@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.WARN
)
public interface MaterialGroupTranslationMapper extends EntityMapper<MaterialGroupTranslationDTO, MaterialGroupTranslation> {

    @Mapping(target = "materialGroupId", source = "materialGroup.id")
    @Override
    MaterialGroupTranslationDTO toDto(MaterialGroupTranslation entity);

    @Mapping(target = "materialGroup", ignore = true)
    @Override
    MaterialGroupTranslation toEntity(MaterialGroupTranslationDTO dto);
}
