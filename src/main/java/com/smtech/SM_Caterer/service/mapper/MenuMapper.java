package com.smtech.SM_Caterer.service.mapper;

import com.smtech.SM_Caterer.domain.entity.Menu;
import com.smtech.SM_Caterer.service.dto.MenuDTO;
import org.mapstruct.*;

/**
 * Mapper for Menu entity.
 */
@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.WARN
)
public interface MenuMapper extends EntityMapper<MenuDTO, Menu> {

    @Mapping(target = "tenantId", source = "tenant.id")
    @Override
    MenuDTO toDto(Menu entity);

    @Mapping(target = "tenant", ignore = true)
    @Mapping(target = "translations", ignore = true)
    @Mapping(target = "recipeItems", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Override
    Menu toEntity(MenuDTO dto);
}
