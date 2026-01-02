package com.smtech.SM_Caterer.service;

import com.smtech.SM_Caterer.service.base.BaseService;
import com.smtech.SM_Caterer.service.dto.UtilityDTO;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Utility operations.
 */
public interface UtilityService extends BaseService<UtilityDTO, Long> {

    Optional<UtilityDTO> findByTenantIdAndUtilityCode(Long tenantId, String utilityCode);

    List<UtilityDTO> findByTenantId(Long tenantId);
}
