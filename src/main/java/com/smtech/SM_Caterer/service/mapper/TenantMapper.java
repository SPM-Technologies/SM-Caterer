package com.smtech.SM_Caterer.service.mapper;

import com.smtech.SM_Caterer.domain.entity.Tenant;
import com.smtech.SM_Caterer.service.dto.TenantDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper for Tenant entity.
 * MapStruct generates implementation at compile time.
 */
@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.WARN
)
public interface TenantMapper extends EntityMapper<TenantDTO, Tenant> {
    // MapStruct generates implementation automatically
}
