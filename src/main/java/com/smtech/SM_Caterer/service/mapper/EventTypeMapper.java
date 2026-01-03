package com.smtech.SM_Caterer.service.mapper;

import com.smtech.SM_Caterer.domain.entity.EventType;
import com.smtech.SM_Caterer.service.dto.EventTypeDTO;
import org.mapstruct.*;

/**
 * Mapper for EventType entity.
 */
@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.WARN
)
public interface EventTypeMapper extends EntityMapper<EventTypeDTO, EventType> {

    @Mapping(target = "tenantId", source = "tenant.id")
    @Mapping(target = "eventTypeCode", source = "eventCode")
    @Override
    EventTypeDTO toDto(EventType entity);

    @Mapping(target = "tenant", ignore = true)
    @Mapping(target = "translations", ignore = true)
    @Mapping(target = "eventCode", source = "eventTypeCode")
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Override
    EventType toEntity(EventTypeDTO dto);
}
