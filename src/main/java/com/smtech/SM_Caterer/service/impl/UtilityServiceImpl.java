package com.smtech.SM_Caterer.service.impl;

import com.smtech.SM_Caterer.domain.entity.Tenant;
import com.smtech.SM_Caterer.domain.entity.Utility;
import com.smtech.SM_Caterer.domain.repository.TenantRepository;
import com.smtech.SM_Caterer.domain.repository.UtilityRepository;
import com.smtech.SM_Caterer.exception.DuplicateResourceException;
import com.smtech.SM_Caterer.exception.ResourceNotFoundException;
import com.smtech.SM_Caterer.service.UtilityService;
import com.smtech.SM_Caterer.service.base.BaseServiceImpl;
import com.smtech.SM_Caterer.service.dto.UtilityDTO;
import com.smtech.SM_Caterer.service.mapper.EntityMapper;
import com.smtech.SM_Caterer.service.mapper.UtilityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for Utility operations.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UtilityServiceImpl extends BaseServiceImpl<Utility, UtilityDTO, Long>
        implements UtilityService {

    private final UtilityRepository utilityRepository;
    private final UtilityMapper utilityMapper;
    private final TenantRepository tenantRepository;

    @Override
    protected JpaRepository<Utility, Long> getRepository() {
        return utilityRepository;
    }

    @Override
    protected EntityMapper<UtilityDTO, Utility> getMapper() {
        return utilityMapper;
    }

    @Override
    protected String getEntityName() {
        return "Utility";
    }

    @Override
    @Transactional
    public UtilityDTO create(UtilityDTO dto) {
        log.debug("Creating new utility: {}", dto.getUtilityCode());

        // Validate unique constraint
        if (utilityRepository.existsByTenantIdAndUtilityCode(dto.getTenantId(), dto.getUtilityCode())) {
            throw new DuplicateResourceException("Utility", "utilityCode", dto.getUtilityCode());
        }

        Utility entity = utilityMapper.toEntity(dto);

        // Set tenant reference
        if (dto.getTenantId() != null) {
            Tenant tenant = tenantRepository.findById(dto.getTenantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", dto.getTenantId()));
            entity.setTenant(tenant);
        }

        Utility saved = utilityRepository.save(entity);
        log.info("Utility created: {} (ID: {})", saved.getUtilityCode(), saved.getId());

        return utilityMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UtilityDTO> findByTenantIdAndUtilityCode(Long tenantId, String utilityCode) {
        return utilityRepository.findByTenantIdAndUtilityCode(tenantId, utilityCode)
                .map(utilityMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UtilityDTO> findByTenantId(Long tenantId) {
        return utilityMapper.toDto(utilityRepository.findByTenantId(tenantId));
    }
}
