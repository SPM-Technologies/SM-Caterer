package com.smtech.SM_Caterer.service.mapper;

import com.smtech.SM_Caterer.domain.entity.OrderUtility;
import com.smtech.SM_Caterer.service.dto.OrderUtilityDTO;
import org.mapstruct.*;

/**
 * Mapper for OrderUtility entity.
 */
@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.WARN
)
public interface OrderUtilityMapper extends EntityMapper<OrderUtilityDTO, OrderUtility> {

    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "utilityId", source = "utility.id")
    @Mapping(target = "utilityName", source = "utility.utilityCode")
    @Override
    OrderUtilityDTO toDto(OrderUtility entity);

    @Mapping(target = "order", ignore = true)
    @Mapping(target = "utility", ignore = true)
    @Override
    OrderUtility toEntity(OrderUtilityDTO dto);
}
