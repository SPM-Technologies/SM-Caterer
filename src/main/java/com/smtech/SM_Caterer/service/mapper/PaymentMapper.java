package com.smtech.SM_Caterer.service.mapper;

import com.smtech.SM_Caterer.domain.entity.Payment;
import com.smtech.SM_Caterer.service.dto.PaymentDTO;
import org.mapstruct.*;

/**
 * Mapper for Payment entity.
 */
@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.WARN
)
public interface PaymentMapper extends EntityMapper<PaymentDTO, Payment> {

    @Mapping(target = "tenantId", source = "tenant.id")
    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "orderNumber", source = "order.orderNumber")
    @Mapping(target = "customerName", source = "order.customer.name")
    @Override
    PaymentDTO toDto(Payment entity);

    @Mapping(target = "tenant", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "createdByUser", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Override
    Payment toEntity(PaymentDTO dto);
}
