package com.smtech.SM_Caterer.service;

import com.smtech.SM_Caterer.service.base.BaseService;
import com.smtech.SM_Caterer.service.dto.CustomerDTO;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Customer operations.
 */
public interface CustomerService extends BaseService<CustomerDTO, Long> {

    Optional<CustomerDTO> findByTenantIdAndCustomerCode(Long tenantId, String customerCode);

    List<CustomerDTO> findByTenantId(Long tenantId);

    List<CustomerDTO> findByPhone(String phone);
}
