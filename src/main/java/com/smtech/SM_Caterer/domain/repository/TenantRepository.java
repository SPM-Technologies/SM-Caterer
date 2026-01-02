package com.smtech.SM_Caterer.domain.repository;

import com.smtech.SM_Caterer.domain.entity.Tenant;
import com.smtech.SM_Caterer.domain.enums.TenantStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Tenant entity.
 *
 * Query Safety:
 * - All queries use named parameters (:param)
 * - No string concatenation (SQL injection safe)
 */
@Repository
public interface TenantRepository extends BaseRepository<Tenant, Long> {

    /**
     * Finds tenant by unique tenant code.
     * @param tenantCode Tenant code (case-sensitive)
     * @return Tenant if found
     */
    Optional<Tenant> findByTenantCode(String tenantCode);

    /**
     * Checks if tenant code already exists.
     * Used for duplicate validation.
     */
    boolean existsByTenantCode(String tenantCode);

    /**
     * Checks if email already exists.
     * Used for duplicate validation.
     */
    boolean existsByEmail(String email);

    /**
     * Finds all tenants with given status.
     * @param status Tenant status
     * @return List of tenants
     */
    List<Tenant> findByStatus(TenantStatus status);

    /**
     * Finds tenants with pagination.
     * @param status Tenant status
     * @param pageable Pagination parameters
     * @return Page of tenants
     */
    Page<Tenant> findByStatus(TenantStatus status, Pageable pageable);

    /**
     * Finds tenants whose subscription ends before given date.
     * Used for expiry alerts.
     */
    @Query("SELECT t FROM Tenant t WHERE t.subscriptionEndDate < :date AND t.status = 'ACTIVE'")
    List<Tenant> findTenantsExpiringBefore(@Param("date") LocalDate date);

    /**
     * Counts tenants by status.
     */
    long countByStatus(TenantStatus status);

    /**
     * Finds all active tenants with pagination.
     * Optimized query for dashboard.
     */
    @Query("SELECT t FROM Tenant t WHERE t.status = 'ACTIVE' ORDER BY t.businessName")
    Page<Tenant> findAllActive(Pageable pageable);
}
