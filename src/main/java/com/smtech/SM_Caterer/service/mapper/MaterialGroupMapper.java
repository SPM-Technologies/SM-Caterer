package com.smtech.SM_Caterer.service.mapper;

import com.smtech.SM_Caterer.domain.entity.MaterialGroup;
import com.smtech.SM_Caterer.service.dto.MaterialGroupDTO;
import org.mapstruct.*;

/**
 * Mapper for MaterialGroup entity.
 */
@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.WARN
)
public interface MaterialGroupMapper extends EntityMapper<MaterialGroupDTO, MaterialGroup> {

    @Mapping(target = "tenantId", source = "tenant.id")
    @Override
    MaterialGroupDTO toDto(MaterialGroup entity);

    @Mapping(target = "tenant", ignore = true)
    @Mapping(target = "translations", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Override
    MaterialGroup toEntity(MaterialGroupDTO dto);
}
