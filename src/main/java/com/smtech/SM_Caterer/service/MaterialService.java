package com.smtech.SM_Caterer.service;

import com.smtech.SM_Caterer.service.base.BaseService;
import com.smtech.SM_Caterer.service.dto.MaterialDTO;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Material operations.
 */
public interface MaterialService extends BaseService<MaterialDTO, Long> {

    Optional<MaterialDTO> findByTenantIdAndMaterialCode(Long tenantId, String materialCode);

    List<MaterialDTO> findByTenantId(Long tenantId);

    List<MaterialDTO> findLowStockMaterials(Long tenantId);
}
