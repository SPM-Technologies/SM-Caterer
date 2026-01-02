package com.smtech.SM_Caterer.service.impl;

import com.smtech.SM_Caterer.domain.entity.Customer;
import com.smtech.SM_Caterer.domain.entity.Tenant;
import com.smtech.SM_Caterer.domain.repository.CustomerRepository;
import com.smtech.SM_Caterer.domain.repository.TenantRepository;
import com.smtech.SM_Caterer.exception.DuplicateResourceException;
import com.smtech.SM_Caterer.exception.ResourceNotFoundException;
import com.smtech.SM_Caterer.service.CustomerService;
import com.smtech.SM_Caterer.service.base.BaseServiceImpl;
import com.smtech.SM_Caterer.service.dto.CustomerDTO;
import com.smtech.SM_Caterer.service.mapper.CustomerMapper;
import com.smtech.SM_Caterer.service.mapper.EntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for Customer operations.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CustomerServiceImpl extends BaseServiceImpl<Customer, CustomerDTO, Long>
        implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final TenantRepository tenantRepository;

    @Override
    protected JpaRepository<Customer, Long> getRepository() {
        return customerRepository;
    }

    @Override
    protected EntityMapper<CustomerDTO, Customer> getMapper() {
        return customerMapper;
    }

    @Override
    protected String getEntityName() {
        return "Customer";
    }

    @Override
    @Transactional
    public CustomerDTO create(CustomerDTO dto) {
        log.debug("Creating new customer: {}", dto.getCustomerCode());

        // Validate unique constraint
        if (customerRepository.existsByTenantIdAndCustomerCode(dto.getTenantId(), dto.getCustomerCode())) {
            throw new DuplicateResourceException("Customer", "customerCode", dto.getCustomerCode());
        }

        Customer entity = customerMapper.toEntity(dto);

        // Set tenant reference
        if (dto.getTenantId() != null) {
            Tenant tenant = tenantRepository.findById(dto.getTenantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", dto.getTenantId()));
            entity.setTenant(tenant);
        }

        Customer saved = customerRepository.save(entity);
        log.info("Customer created: {} (ID: {})", saved.getCustomerCode(), saved.getId());

        return customerMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CustomerDTO> findByTenantIdAndCustomerCode(Long tenantId, String customerCode) {
        return customerRepository.findByTenantIdAndCustomerCode(tenantId, customerCode)
                .map(customerMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerDTO> findByTenantId(Long tenantId) {
        return customerMapper.toDto(customerRepository.findByTenantId(tenantId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerDTO> findByPhone(String phone) {
        return customerMapper.toDto(customerRepository.findByPhone(phone));
    }
}
