package com.smtech.SM_Caterer.service.mapper;

import com.smtech.SM_Caterer.domain.entity.OrderMenuItem;
import com.smtech.SM_Caterer.service.dto.OrderMenuItemDTO;
import org.mapstruct.*;

/**
 * Mapper for OrderMenuItem entity.
 */
@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.WARN
)
public interface OrderMenuItemMapper extends EntityMapper<OrderMenuItemDTO, OrderMenuItem> {

    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "menuId", source = "menu.id")
    @Mapping(target = "menuName", source = "menu.menuCode")
    @Override
    OrderMenuItemDTO toDto(OrderMenuItem entity);

    @Mapping(target = "order", ignore = true)
    @Mapping(target = "menu", ignore = true)
    @Override
    OrderMenuItem toEntity(OrderMenuItemDTO dto);
}
