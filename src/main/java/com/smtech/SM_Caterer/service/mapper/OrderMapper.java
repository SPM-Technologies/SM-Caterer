package com.smtech.SM_Caterer.service.mapper;

import com.smtech.SM_Caterer.domain.entity.Order;
import com.smtech.SM_Caterer.service.dto.OrderDTO;
import org.mapstruct.*;

/**
 * Mapper for Order entity.
 * Handles complex relationships with customer, event type, and child collections.
 */
@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.WARN,
    uses = {CustomerMapper.class, EventTypeMapper.class, OrderMenuItemMapper.class, OrderUtilityMapper.class}
)
public interface OrderMapper extends EntityMapper<OrderDTO, Order> {

    @Mapping(target = "tenantId", source = "tenant.id")
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.name")
    @Mapping(target = "eventTypeId", source = "eventType.id")
    @Mapping(target = "eventTypeName", source = "eventType.eventCode")
    @Mapping(target = "menuItems", ignore = true)
    @Mapping(target = "utilities", ignore = true)
    @Override
    OrderDTO toDto(Order entity);

    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "eventType", ignore = true)
    @Mapping(target = "createdByUser", ignore = true)
    @Mapping(target = "payments", ignore = true)
    @Mapping(target = "menuItems", ignore = true)
    @Mapping(target = "utilities", ignore = true)
    @Mapping(target = "tenant", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Override
    Order toEntity(OrderDTO dto);
}
