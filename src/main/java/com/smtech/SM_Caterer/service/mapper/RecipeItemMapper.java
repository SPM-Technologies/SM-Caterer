package com.smtech.SM_Caterer.service.mapper;

import com.smtech.SM_Caterer.domain.entity.RecipeItem;
import com.smtech.SM_Caterer.service.dto.RecipeItemDTO;
import org.mapstruct.*;

/**
 * Mapper for RecipeItem entity.
 */
@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.WARN
)
public interface RecipeItemMapper extends EntityMapper<RecipeItemDTO, RecipeItem> {

    @Mapping(target = "menuId", source = "menu.id")
    @Mapping(target = "menuCode", source = "menu.menuCode")
    @Mapping(target = "materialId", source = "material.id")
    @Mapping(target = "materialCode", source = "material.materialCode")
    @Mapping(target = "quantityRequired", source = "quantityRequired")
    @Mapping(target = "materialName", ignore = true)
    @Mapping(target = "unitCode", source = "material.unit.unitCode")
    @Override
    RecipeItemDTO toDto(RecipeItem entity);

    @Mapping(target = "menu", ignore = true)
    @Mapping(target = "material", ignore = true)
    @Mapping(target = "quantityRequired", source = "quantityRequired")
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Override
    RecipeItem toEntity(RecipeItemDTO dto);
}
