package com.smtech.SM_Caterer.service.impl;

import com.smtech.SM_Caterer.domain.entity.EventType;
import com.smtech.SM_Caterer.domain.entity.Tenant;
import com.smtech.SM_Caterer.domain.repository.EventTypeRepository;
import com.smtech.SM_Caterer.domain.repository.TenantRepository;
import com.smtech.SM_Caterer.exception.DuplicateResourceException;
import com.smtech.SM_Caterer.exception.ResourceNotFoundException;
import com.smtech.SM_Caterer.service.EventTypeService;
import com.smtech.SM_Caterer.service.base.BaseServiceImpl;
import com.smtech.SM_Caterer.service.dto.EventTypeDTO;
import com.smtech.SM_Caterer.service.mapper.EntityMapper;
import com.smtech.SM_Caterer.service.mapper.EventTypeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for EventType operations.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class EventTypeServiceImpl extends BaseServiceImpl<EventType, EventTypeDTO, Long>
        implements EventTypeService {

    private final EventTypeRepository eventTypeRepository;
    private final EventTypeMapper eventTypeMapper;
    private final TenantRepository tenantRepository;

    @Override
    protected JpaRepository<EventType, Long> getRepository() {
        return eventTypeRepository;
    }

    @Override
    protected EntityMapper<EventTypeDTO, EventType> getMapper() {
        return eventTypeMapper;
    }

    @Override
    protected String getEntityName() {
        return "EventType";
    }

    @Override
    @Transactional
    public EventTypeDTO create(EventTypeDTO dto) {
        log.debug("Creating new event type: {}", dto.getEventTypeCode());

        // Validate unique constraint (entity field is eventCode, DTO field is eventTypeCode)
        if (eventTypeRepository.existsByTenantIdAndEventCode(dto.getTenantId(), dto.getEventTypeCode())) {
            throw new DuplicateResourceException("EventType", "eventTypeCode", dto.getEventTypeCode());
        }

        EventType entity = eventTypeMapper.toEntity(dto);

        // Set tenant reference
        if (dto.getTenantId() != null) {
            Tenant tenant = tenantRepository.findById(dto.getTenantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", dto.getTenantId()));
            entity.setTenant(tenant);
        }

        EventType saved = eventTypeRepository.save(entity);
        log.info("EventType created: {} (ID: {})", saved.getEventCode(), saved.getId());

        return eventTypeMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EventTypeDTO> findByTenantIdAndEventTypeCode(Long tenantId, String eventTypeCode) {
        return eventTypeRepository.findByTenantIdAndEventCode(tenantId, eventTypeCode)
                .map(eventTypeMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventTypeDTO> findByTenantId(Long tenantId) {
        return eventTypeMapper.toDto(eventTypeRepository.findByTenantId(tenantId));
    }
}
