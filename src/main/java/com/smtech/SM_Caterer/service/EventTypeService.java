package com.smtech.SM_Caterer.service;

import com.smtech.SM_Caterer.service.base.BaseService;
import com.smtech.SM_Caterer.service.dto.EventTypeDTO;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for EventType operations.
 */
public interface EventTypeService extends BaseService<EventTypeDTO, Long> {

    Optional<EventTypeDTO> findByTenantIdAndEventTypeCode(Long tenantId, String eventTypeCode);

    List<EventTypeDTO> findByTenantId(Long tenantId);
}
