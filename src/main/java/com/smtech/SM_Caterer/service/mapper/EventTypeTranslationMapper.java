package com.smtech.SM_Caterer.service.mapper;

import com.smtech.SM_Caterer.domain.entity.EventTypeTranslation;
import com.smtech.SM_Caterer.service.dto.EventTypeTranslationDTO;
import org.mapstruct.*;

/**
 * Mapper for EventTypeTranslation entity.
 */
@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.WARN
)
public interface EventTypeTranslationMapper extends EntityMapper<EventTypeTranslationDTO, EventTypeTranslation> {

    @Mapping(target = "eventTypeId", source = "eventType.id")
    @Override
    EventTypeTranslationDTO toDto(EventTypeTranslation entity);

    @Mapping(target = "eventType", ignore = true)
    @Override
    EventTypeTranslation toEntity(EventTypeTranslationDTO dto);
}
