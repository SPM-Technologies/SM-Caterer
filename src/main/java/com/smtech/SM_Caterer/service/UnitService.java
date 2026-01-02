package com.smtech.SM_Caterer.service;

import com.smtech.SM_Caterer.service.base.BaseService;
import com.smtech.SM_Caterer.service.dto.UnitDTO;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Unit operations.
 */
public interface UnitService extends BaseService<UnitDTO, Long> {

    Optional<UnitDTO> findByTenantIdAndUnitCode(Long tenantId, String unitCode);

    List<UnitDTO> findByTenantId(Long tenantId);
}
