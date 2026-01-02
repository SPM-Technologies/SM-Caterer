package com.smtech.SM_Caterer.service.impl;

import com.smtech.SM_Caterer.domain.entity.MaterialGroup;
import com.smtech.SM_Caterer.domain.entity.Tenant;
import com.smtech.SM_Caterer.domain.repository.MaterialGroupRepository;
import com.smtech.SM_Caterer.domain.repository.TenantRepository;
import com.smtech.SM_Caterer.exception.DuplicateResourceException;
import com.smtech.SM_Caterer.exception.ResourceNotFoundException;
import com.smtech.SM_Caterer.service.MaterialGroupService;
import com.smtech.SM_Caterer.service.base.BaseServiceImpl;
import com.smtech.SM_Caterer.service.dto.MaterialGroupDTO;
import com.smtech.SM_Caterer.service.mapper.EntityMapper;
import com.smtech.SM_Caterer.service.mapper.MaterialGroupMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for MaterialGroup operations.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MaterialGroupServiceImpl extends BaseServiceImpl<MaterialGroup, MaterialGroupDTO, Long>
        implements MaterialGroupService {

    private final MaterialGroupRepository materialGroupRepository;
    private final MaterialGroupMapper materialGroupMapper;
    private final TenantRepository tenantRepository;

    @Override
    protected JpaRepository<MaterialGroup, Long> getRepository() {
        return materialGroupRepository;
    }

    @Override
    protected EntityMapper<MaterialGroupDTO, MaterialGroup> getMapper() {
        return materialGroupMapper;
    }

    @Override
    protected String getEntityName() {
        return "MaterialGroup";
    }

    @Override
    @Transactional
    public MaterialGroupDTO create(MaterialGroupDTO dto) {
        // Validate unique constraint
        if (materialGroupRepository.existsByTenantIdAndGroupCode(dto.getTenantId(), dto.getGroupCode())) {
            throw new DuplicateResourceException("MaterialGroup", "groupCode", dto.getGroupCode());
        }

        MaterialGroup entity = materialGroupMapper.toEntity(dto);

        // Set tenant
        if (dto.getTenantId() != null) {
            Tenant tenant = tenantRepository.findById(dto.getTenantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", dto.getTenantId()));
            entity.setTenant(tenant);
        }

        MaterialGroup saved = materialGroupRepository.save(entity);
        return materialGroupMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MaterialGroupDTO> findByTenantIdAndGroupCode(Long tenantId, String groupCode) {
        return materialGroupRepository.findByTenantIdAndGroupCode(tenantId, groupCode)
                .map(materialGroupMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaterialGroupDTO> findByTenantId(Long tenantId) {
        return materialGroupMapper.toDto(materialGroupRepository.findByTenantId(tenantId));
    }
}
