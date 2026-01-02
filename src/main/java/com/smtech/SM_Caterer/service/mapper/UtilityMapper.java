package com.smtech.SM_Caterer.service.mapper;

import com.smtech.SM_Caterer.domain.entity.Utility;
import com.smtech.SM_Caterer.service.dto.UtilityDTO;
import org.mapstruct.*;

/**
 * Mapper for Utility entity.
 */
@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.WARN
)
public interface UtilityMapper extends EntityMapper<UtilityDTO, Utility> {

    @Mapping(target = "tenantId", source = "tenant.id")
    @Override
    UtilityDTO toDto(Utility entity);

    @Mapping(target = "tenant", ignore = true)
    @Mapping(target = "translations", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Override
    Utility toEntity(UtilityDTO dto);
}
