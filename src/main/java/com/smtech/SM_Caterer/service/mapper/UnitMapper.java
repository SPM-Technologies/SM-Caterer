package com.smtech.SM_Caterer.service.mapper;

import com.smtech.SM_Caterer.domain.entity.Unit;
import com.smtech.SM_Caterer.service.dto.UnitDTO;
import org.mapstruct.*;

/**
 * Mapper for Unit entity.
 */
@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.WARN
)
public interface UnitMapper extends EntityMapper<UnitDTO, Unit> {

    @Mapping(target = "tenantId", source = "tenant.id")
    @Override
    UnitDTO toDto(Unit entity);

    @Mapping(target = "tenant", ignore = true)
    @Mapping(target = "translations", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Override
    Unit toEntity(UnitDTO dto);
}
