package com.smtech.SM_Caterer.service.mapper;

import com.smtech.SM_Caterer.domain.entity.Material;
import com.smtech.SM_Caterer.service.dto.MaterialDTO;
import org.mapstruct.*;

/**
 * Mapper for Material entity.
 * Handles relationships with tenant, material group, and unit.
 */
@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.WARN,
    uses = {MaterialGroupMapper.class, UnitMapper.class}
)
public interface MaterialMapper extends EntityMapper<MaterialDTO, Material> {

    @Mapping(target = "tenantId", source = "tenant.id")
    @Mapping(target = "materialGroupId", source = "materialGroup.id")
    @Mapping(target = "materialGroupCode", source = "materialGroup.groupCode")
    @Mapping(target = "unitId", source = "unit.id")
    @Mapping(target = "unitCode", source = "unit.unitCode")
    @Override
    MaterialDTO toDto(Material entity);

    @Mapping(target = "tenant", ignore = true)
    @Mapping(target = "materialGroup", ignore = true)
    @Mapping(target = "unit", ignore = true)
    @Mapping(target = "translations", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Override
    Material toEntity(MaterialDTO dto);
}
