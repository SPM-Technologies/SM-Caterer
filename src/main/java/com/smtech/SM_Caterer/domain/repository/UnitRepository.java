package com.smtech.SM_Caterer.domain.repository;

import com.smtech.SM_Caterer.domain.entity.Unit;
import com.smtech.SM_Caterer.domain.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Unit entity.
 */
@Repository
public interface UnitRepository extends BaseRepository<Unit, Long> {

    /**
     * Finds unit by tenant and unit code.
     * @param tenantId Tenant ID
     * @param unitCode Unit code
     * @return Unit if found
     */
    Optional<Unit> findByTenantIdAndUnitCode(Long tenantId, String unitCode);

    /**
     * Finds all units for tenant with translations loaded.
     * Uses EntityGraph to prevent N+1 queries.
     */
    @EntityGraph(attributePaths = {"translations"})
    List<Unit> findByTenantId(Long tenantId);

    /**
     * Paginated query with eager loading.
     */
    @EntityGraph(attributePaths = {"translations"})
    Page<Unit> findByTenantId(Long tenantId, Pageable pageable);

    /**
     * Finds units by tenant and status.
     * @param tenantId Tenant ID
     * @param status Status
     * @return List of units
     */
    List<Unit> findByTenantIdAndStatus(Long tenantId, Status status);

    /**
     * Checks if unit code exists for tenant.
     * @param tenantId Tenant ID
     * @param unitCode Unit code
     * @return true if exists
     */
    boolean existsByTenantIdAndUnitCode(Long tenantId, String unitCode);

    /**
     * Counts units by tenant.
     */
    long countByTenantId(Long tenantId);

    /**
     * Counts units by tenant and status.
     */
    long countByTenantIdAndStatus(Long tenantId, Status status);

    /**
     * Finds unit by ID and tenant ID (tenant isolation).
     */
    Optional<Unit> findByIdAndTenantId(Long id, Long tenantId);
}
