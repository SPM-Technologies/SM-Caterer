package com.smtech.SM_Caterer.service;

import com.smtech.SM_Caterer.service.base.BaseService;
import com.smtech.SM_Caterer.service.dto.MenuDTO;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Menu operations.
 */
public interface MenuService extends BaseService<MenuDTO, Long> {

    Optional<MenuDTO> findByTenantIdAndMenuCode(Long tenantId, String menuCode);

    List<MenuDTO> findByTenantId(Long tenantId);
}
