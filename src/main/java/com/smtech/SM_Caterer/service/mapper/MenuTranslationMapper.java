package com.smtech.SM_Caterer.service.mapper;

import com.smtech.SM_Caterer.domain.entity.MenuTranslation;
import com.smtech.SM_Caterer.service.dto.MenuTranslationDTO;
import org.mapstruct.*;

/**
 * Mapper for MenuTranslation entity.
 */
@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.WARN
)
public interface MenuTranslationMapper extends EntityMapper<MenuTranslationDTO, MenuTranslation> {

    @Mapping(target = "menuId", source = "menu.id")
    @Override
    MenuTranslationDTO toDto(MenuTranslation entity);

    @Mapping(target = "menu", ignore = true)
    @Override
    MenuTranslation toEntity(MenuTranslationDTO dto);
}
