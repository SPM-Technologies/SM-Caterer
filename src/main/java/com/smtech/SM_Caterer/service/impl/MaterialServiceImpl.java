package com.smtech.SM_Caterer.service.impl;

import com.smtech.SM_Caterer.domain.entity.Material;
import com.smtech.SM_Caterer.domain.entity.MaterialGroup;
import com.smtech.SM_Caterer.domain.entity.Tenant;
import com.smtech.SM_Caterer.domain.entity.Unit;
import com.smtech.SM_Caterer.domain.repository.MaterialGroupRepository;
import com.smtech.SM_Caterer.domain.repository.MaterialRepository;
import com.smtech.SM_Caterer.domain.repository.TenantRepository;
import com.smtech.SM_Caterer.domain.repository.UnitRepository;
import com.smtech.SM_Caterer.exception.DuplicateResourceException;
import com.smtech.SM_Caterer.exception.ResourceNotFoundException;
import com.smtech.SM_Caterer.service.MaterialService;
import com.smtech.SM_Caterer.service.base.BaseServiceImpl;
import com.smtech.SM_Caterer.service.dto.MaterialDTO;
import com.smtech.SM_Caterer.service.mapper.EntityMapper;
import com.smtech.SM_Caterer.service.mapper.MaterialMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for Material operations.
 *
 * Business Logic:
 * - Stock level validation
 * - Low stock warnings
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MaterialServiceImpl extends BaseServiceImpl<Material, MaterialDTO, Long>
        implements MaterialService {

    private final MaterialRepository materialRepository;
    private final MaterialMapper materialMapper;
    private final TenantRepository tenantRepository;
    private final MaterialGroupRepository materialGroupRepository;
    private final UnitRepository unitRepository;

    @Override
    protected JpaRepository<Material, Long> getRepository() {
        return materialRepository;
    }

    @Override
    protected EntityMapper<MaterialDTO, Material> getMapper() {
        return materialMapper;
    }

    @Override
    protected String getEntityName() {
        return "Material";
    }

    @Override
    @Transactional
    public MaterialDTO create(MaterialDTO dto) {
        log.debug("Creating new material: {}", dto.getMaterialCode());

        // Validate unique constraint
        if (materialRepository.existsByTenantIdAndMaterialCode(dto.getTenantId(), dto.getMaterialCode())) {
            throw new DuplicateResourceException("Material", "materialCode", dto.getMaterialCode());
        }

        Material entity = materialMapper.toEntity(dto);

        // Set tenant reference
        if (dto.getTenantId() != null) {
            Tenant tenant = tenantRepository.findById(dto.getTenantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", dto.getTenantId()));
            entity.setTenant(tenant);
        }

        // Set material group reference
        if (dto.getMaterialGroupId() != null) {
            MaterialGroup group = materialGroupRepository.findById(dto.getMaterialGroupId())
                    .orElseThrow(() -> new ResourceNotFoundException("MaterialGroup", "id", dto.getMaterialGroupId()));
            entity.setMaterialGroup(group);
        }

        // Set unit reference
        if (dto.getUnitId() != null) {
            Unit unit = unitRepository.findById(dto.getUnitId())
                    .orElseThrow(() -> new ResourceNotFoundException("Unit", "id", dto.getUnitId()));
            entity.setUnit(unit);
        }

        Material saved = materialRepository.save(entity);
        log.info("Material created: {} (ID: {})", saved.getMaterialCode(), saved.getId());

        // Check for low stock warning
        if (saved.getCurrentStock() != null && saved.getMinimumStock() != null &&
            saved.getCurrentStock().compareTo(saved.getMinimumStock()) < 0) {
            log.warn("Low stock warning: Material {} (ID: {}) stock level {} is below minimum {}",
                    saved.getMaterialCode(), saved.getId(), saved.getCurrentStock(), saved.getMinimumStock());
        }

        return materialMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MaterialDTO> findByTenantIdAndMaterialCode(Long tenantId, String materialCode) {
        return materialRepository.findByTenantIdAndMaterialCode(tenantId, materialCode)
                .map(materialMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaterialDTO> findByTenantId(Long tenantId) {
        return materialMapper.toDto(materialRepository.findByTenantId(tenantId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaterialDTO> findLowStockMaterials(Long tenantId) {
        log.debug("Finding low stock materials for tenant ID: {}", tenantId);
        List<Material> lowStockMaterials = materialRepository.findByTenantId(tenantId).stream()
                .filter(material -> material.getCurrentStock() != null &&
                                  material.getMinimumStock() != null &&
                                  material.getCurrentStock().compareTo(material.getMinimumStock()) < 0)
                .toList();

        log.info("Found {} low stock materials for tenant ID: {}", lowStockMaterials.size(), tenantId);
        return materialMapper.toDto(lowStockMaterials);
    }
}
