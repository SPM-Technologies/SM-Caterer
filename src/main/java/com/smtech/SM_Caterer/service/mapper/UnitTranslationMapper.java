package com.smtech.SM_Caterer.service.mapper;

import com.smtech.SM_Caterer.domain.entity.UnitTranslation;
import com.smtech.SM_Caterer.service.dto.UnitTranslationDTO;
import org.mapstruct.*;

/**
 * Mapper for UnitTranslation entity.
 */
@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.WARN
)
public interface UnitTranslationMapper extends EntityMapper<UnitTranslationDTO, UnitTranslation> {

    @Mapping(target = "unitId", source = "unit.id")
    @Override
    UnitTranslationDTO toDto(UnitTranslation entity);

    @Mapping(target = "unit", ignore = true)
    @Override
    UnitTranslation toEntity(UnitTranslationDTO dto);
}
