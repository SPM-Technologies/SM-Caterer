package com.smtech.SM_Caterer.service;

import com.smtech.SM_Caterer.service.base.BaseService;
import com.smtech.SM_Caterer.service.dto.MaterialGroupDTO;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for MaterialGroup operations.
 */
public interface MaterialGroupService extends BaseService<MaterialGroupDTO, Long> {

    /**
     * Finds material group by tenant and code.
     * @param tenantId Tenant ID
     * @param groupCode Group code
     * @return MaterialGroup if found
     */
    Optional<MaterialGroupDTO> findByTenantIdAndGroupCode(Long tenantId, String groupCode);

    /**
     * Finds all material groups by tenant.
     * @param tenantId Tenant ID
     * @return List of material groups
     */
    List<MaterialGroupDTO> findByTenantId(Long tenantId);
}
