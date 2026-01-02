package com.smtech.SM_Caterer.service.mapper;

import com.smtech.SM_Caterer.domain.entity.UpiQrCode;
import com.smtech.SM_Caterer.service.dto.UpiQrCodeDTO;
import org.mapstruct.*;

/**
 * Mapper for UpiQrCode entity.
 */
@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.WARN
)
public interface UpiQrCodeMapper extends EntityMapper<UpiQrCodeDTO, UpiQrCode> {

    @Mapping(target = "tenantId", source = "tenant.id")
    @Override
    UpiQrCodeDTO toDto(UpiQrCode entity);

    @Mapping(target = "tenant", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "generatedAt", ignore = true)
    @Mapping(target = "expiresAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Override
    UpiQrCode toEntity(UpiQrCodeDTO dto);
}
