package com.smtech.SM_Caterer.domain.repository;

import com.smtech.SM_Caterer.domain.entity.Utility;
import com.smtech.SM_Caterer.domain.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Utility entity.
 */
@Repository
public interface UtilityRepository extends BaseRepository<Utility, Long> {

    /**
     * Finds utility by tenant and utility code.
     * @param tenantId Tenant ID
     * @param utilityCode Utility code
     * @return Utility if found
     */
    Optional<Utility> findByTenantIdAndUtilityCode(Long tenantId, String utilityCode);

    /**
     * Finds all utilities for tenant with translations loaded.
     * Uses EntityGraph to prevent N+1 queries.
     */
    @EntityGraph(attributePaths = {"translations"})
    List<Utility> findByTenantId(Long tenantId);

    /**
     * Paginated query with eager loading.
     */
    @EntityGraph(attributePaths = {"translations"})
    Page<Utility> findByTenantId(Long tenantId, Pageable pageable);

    /**
     * Finds utilities by tenant and status.
     * @param tenantId Tenant ID
     * @param status Status
     * @return List of utilities
     */
    List<Utility> findByTenantIdAndStatus(Long tenantId, Status status);

    /**
     * Checks if utility code exists for tenant.
     * @param tenantId Tenant ID
     * @param utilityCode Utility code
     * @return true if exists
     */
    boolean existsByTenantIdAndUtilityCode(Long tenantId, String utilityCode);

    /**
     * Counts utilities by tenant.
     */
    long countByTenantId(Long tenantId);

    /**
     * Counts utilities by tenant and status.
     */
    long countByTenantIdAndStatus(Long tenantId, Status status);
}
