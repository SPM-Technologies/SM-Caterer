package com.smtech.SM_Caterer.service.impl;

import com.smtech.SM_Caterer.domain.entity.Tenant;
import com.smtech.SM_Caterer.domain.enums.TenantStatus;
import com.smtech.SM_Caterer.domain.repository.TenantRepository;
import com.smtech.SM_Caterer.exception.DuplicateResourceException;
import com.smtech.SM_Caterer.service.TenantService;
import com.smtech.SM_Caterer.service.base.BaseServiceImpl;
import com.smtech.SM_Caterer.service.dto.TenantDTO;
import com.smtech.SM_Caterer.service.mapper.EntityMapper;
import com.smtech.SM_Caterer.service.mapper.TenantMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service implementation for Tenant operations.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TenantServiceImpl extends BaseServiceImpl<Tenant, TenantDTO, Long>
        implements TenantService {

    private final TenantRepository tenantRepository;
    private final TenantMapper tenantMapper;

    @Override
    protected JpaRepository<Tenant, Long> getRepository() {
        return tenantRepository;
    }

    @Override
    protected EntityMapper<TenantDTO, Tenant> getMapper() {
        return tenantMapper;
    }

    @Override
    protected String getEntityName() {
        return "Tenant";
    }

    @Override
    @Transactional
    public TenantDTO create(TenantDTO dto) {
        // Validate unique constraints
        if (tenantRepository.existsByTenantCode(dto.getTenantCode())) {
            throw new DuplicateResourceException("Tenant", "tenantCode", dto.getTenantCode());
        }
        if (dto.getEmail() != null && tenantRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Tenant", "email", dto.getEmail());
        }

        return super.create(dto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TenantDTO> findByTenantCode(String tenantCode) {
        return tenantRepository.findByTenantCode(tenantCode)
                .map(tenantMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByTenantCode(String tenantCode) {
        return tenantRepository.existsByTenantCode(tenantCode);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return tenantRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TenantDTO> findByStatus(TenantStatus status) {
        return tenantMapper.toDto(tenantRepository.findByStatus(status));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TenantDTO> findTenantsExpiringBefore(LocalDate date) {
        return tenantMapper.toDto(tenantRepository.findTenantsExpiringBefore(date));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TenantDTO> findAllActive(Pageable pageable) {
        return tenantRepository.findAllActive(pageable)
                .map(tenantMapper::toDto);
    }
}
