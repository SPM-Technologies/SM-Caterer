package com.smtech.SM_Caterer.service.mapper;

import com.smtech.SM_Caterer.domain.entity.UtilityTranslation;
import com.smtech.SM_Caterer.service.dto.UtilityTranslationDTO;
import org.mapstruct.*;

/**
 * Mapper for UtilityTranslation entity.
 */
@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.WARN
)
public interface UtilityTranslationMapper extends EntityMapper<UtilityTranslationDTO, UtilityTranslation> {

    @Mapping(target = "utilityId", source = "utility.id")
    @Override
    UtilityTranslationDTO toDto(UtilityTranslation entity);

    @Mapping(target = "utility", ignore = true)
    @Override
    UtilityTranslation toEntity(UtilityTranslationDTO dto);
}
