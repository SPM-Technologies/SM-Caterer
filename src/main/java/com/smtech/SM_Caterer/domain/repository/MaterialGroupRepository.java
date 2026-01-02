package com.smtech.SM_Caterer.domain.repository;

import com.smtech.SM_Caterer.domain.entity.MaterialGroup;
import com.smtech.SM_Caterer.domain.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for MaterialGroup entity.
 */
@Repository
public interface MaterialGroupRepository extends BaseRepository<MaterialGroup, Long> {

    /**
     * Finds material group by tenant and group code.
     * @param tenantId Tenant ID
     * @param groupCode Group code
     * @return MaterialGroup if found
     */
    Optional<MaterialGroup> findByTenantIdAndGroupCode(Long tenantId, String groupCode);

    /**
     * Finds all material groups for tenant with translations loaded.
     * Uses EntityGraph to prevent N+1 queries.
     */
    @EntityGraph(attributePaths = {"translations"})
    List<MaterialGroup> findByTenantId(Long tenantId);

    /**
     * Paginated query with eager loading.
     */
    @EntityGraph(attributePaths = {"translations"})
    Page<MaterialGroup> findByTenantId(Long tenantId, Pageable pageable);

    /**
     * Finds material groups by tenant and status.
     * @param tenantId Tenant ID
     * @param status Status
     * @return List of material groups
     */
    List<MaterialGroup> findByTenantIdAndStatus(Long tenantId, Status status);

    /**
     * Checks if group code exists for tenant.
     * @param tenantId Tenant ID
     * @param groupCode Group code
     * @return true if exists
     */
    boolean existsByTenantIdAndGroupCode(Long tenantId, String groupCode);

    /**
     * Counts material groups by tenant.
     */
    long countByTenantId(Long tenantId);

    /**
     * Counts material groups by tenant and status.
     */
    long countByTenantIdAndStatus(Long tenantId, Status status);
}
