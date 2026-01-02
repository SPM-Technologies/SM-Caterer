package com.smtech.SM_Caterer.service.mapper;

import com.smtech.SM_Caterer.domain.entity.Customer;
import com.smtech.SM_Caterer.service.dto.CustomerDTO;
import org.mapstruct.*;

/**
 * Mapper for Customer entity.
 */
@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.WARN
)
public interface CustomerMapper extends EntityMapper<CustomerDTO, Customer> {

    @Mapping(target = "tenantId", source = "tenant.id")
    @Override
    CustomerDTO toDto(Customer entity);

    @Mapping(target = "tenant", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Override
    Customer toEntity(CustomerDTO dto);
}
