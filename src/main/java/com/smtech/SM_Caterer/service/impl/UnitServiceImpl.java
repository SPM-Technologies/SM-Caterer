package com.smtech.SM_Caterer.service.impl;

import com.smtech.SM_Caterer.domain.entity.Tenant;
import com.smtech.SM_Caterer.domain.entity.Unit;
import com.smtech.SM_Caterer.domain.repository.TenantRepository;
import com.smtech.SM_Caterer.domain.repository.UnitRepository;
import com.smtech.SM_Caterer.exception.DuplicateResourceException;
import com.smtech.SM_Caterer.exception.ResourceNotFoundException;
import com.smtech.SM_Caterer.service.UnitService;
import com.smtech.SM_Caterer.service.base.BaseServiceImpl;
import com.smtech.SM_Caterer.service.dto.UnitDTO;
import com.smtech.SM_Caterer.service.mapper.EntityMapper;
import com.smtech.SM_Caterer.service.mapper.UnitMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for Unit operations.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UnitServiceImpl extends BaseServiceImpl<Unit, UnitDTO, Long>
        implements UnitService {

    private final UnitRepository unitRepository;
    private final UnitMapper unitMapper;
    private final TenantRepository tenantRepository;

    @Override
    protected JpaRepository<Unit, Long> getRepository() {
        return unitRepository;
    }

    @Override
    protected EntityMapper<UnitDTO, Unit> getMapper() {
        return unitMapper;
    }

    @Override
    protected String getEntityName() {
        return "Unit";
    }

    @Override
    @Transactional
    public UnitDTO create(UnitDTO dto) {
        log.debug("Creating new unit: {}", dto.getUnitCode());

        // Validate unique constraint
        if (unitRepository.existsByTenantIdAndUnitCode(dto.getTenantId(), dto.getUnitCode())) {
            throw new DuplicateResourceException("Unit", "unitCode", dto.getUnitCode());
        }

        Unit entity = unitMapper.toEntity(dto);

        // Set tenant reference
        if (dto.getTenantId() != null) {
            Tenant tenant = tenantRepository.findById(dto.getTenantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", dto.getTenantId()));
            entity.setTenant(tenant);
        }

        Unit saved = unitRepository.save(entity);
        log.info("Unit created: {} (ID: {})", saved.getUnitCode(), saved.getId());

        return unitMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UnitDTO> findByTenantIdAndUnitCode(Long tenantId, String unitCode) {
        return unitRepository.findByTenantIdAndUnitCode(tenantId, unitCode)
                .map(unitMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitDTO> findByTenantId(Long tenantId) {
        return unitMapper.toDto(unitRepository.findByTenantId(tenantId));
    }
}
