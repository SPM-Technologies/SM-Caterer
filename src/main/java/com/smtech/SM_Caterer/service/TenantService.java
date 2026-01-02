package com.smtech.SM_Caterer.service;

import com.smtech.SM_Caterer.domain.enums.TenantStatus;
import com.smtech.SM_Caterer.service.base.BaseService;
import com.smtech.SM_Caterer.service.dto.TenantDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for Tenant operations.
 */
public interface TenantService extends BaseService<TenantDTO, Long> {

    /**
     * Finds tenant by unique code.
     * @param tenantCode Tenant code
     * @return Tenant if found
     */
    Optional<TenantDTO> findByTenantCode(String tenantCode);

    /**
     * Checks if tenant code exists.
     * @param tenantCode Tenant code
     * @return true if exists
     */
    boolean existsByTenantCode(String tenantCode);

    /**
     * Checks if email exists.
     * @param email Email address
     * @return true if exists
     */
    boolean existsByEmail(String email);

    /**
     * Finds tenants by status.
     * @param status Tenant status
     * @return List of tenants
     */
    List<TenantDTO> findByStatus(TenantStatus status);

    /**
     * Finds tenants expiring before given date.
     * @param date Expiry date
     * @return List of tenants
     */
    List<TenantDTO> findTenantsExpiringBefore(LocalDate date);

    /**
     * Finds all active tenants with pagination.
     * @param pageable Pagination parameters
     * @return Page of active tenants
     */
    Page<TenantDTO> findAllActive(Pageable pageable);
}
