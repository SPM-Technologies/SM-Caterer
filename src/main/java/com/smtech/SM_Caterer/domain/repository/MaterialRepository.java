package com.smtech.SM_Caterer.domain.repository;

import com.smtech.SM_Caterer.domain.entity.Material;
import com.smtech.SM_Caterer.domain.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Material entity.
 *
 * Performance:
 * - Uses EntityGraph to prevent N+1 queries
 * - Optimized for stock alerts
 */
@Repository
public interface MaterialRepository extends BaseRepository<Material, Long> {

    /**
     * Finds material by tenant and material code.
     */
    Optional<Material> findByTenantIdAndMaterialCode(Long tenantId, String materialCode);

    /**
     * Finds all materials for tenant with translations loaded.
     * Uses EntityGraph to prevent N+1 queries.
     */
    @EntityGraph(attributePaths = {"translations", "materialGroup", "unit"})
    List<Material> findByTenantId(Long tenantId);

    /**
     * Paginated query with eager loading.
     */
    @EntityGraph(attributePaths = {"translations", "materialGroup", "unit"})
    Page<Material> findByTenantId(Long tenantId, Pageable pageable);

    /**
     * Finds materials by tenant and material group.
     */
    List<Material> findByTenantIdAndMaterialGroupId(Long tenantId, Long materialGroupId);

    /**
     * Finds materials by tenant and status.
     */
    List<Material> findByTenantIdAndStatus(Long tenantId, Status status);

    /**
     * Finds materials with low stock (below minimum).
     * CRITICAL: Used for stock alerts.
     */
    @Query("SELECT m FROM Material m WHERE m.tenant.id = :tenantId " +
           "AND m.currentStock < m.minimumStock AND m.status = 'ACTIVE'")
    List<Material> findLowStockMaterials(@Param("tenantId") Long tenantId);

    /**
     * Finds materials with low stock (paginated).
     */
    @Query("SELECT m FROM Material m WHERE m.tenant.id = :tenantId " +
           "AND m.currentStock < m.minimumStock AND m.status = 'ACTIVE'")
    Page<Material> findLowStockMaterials(@Param("tenantId") Long tenantId, Pageable pageable);

    /**
     * Checks if material code exists for tenant.
     */
    boolean existsByTenantIdAndMaterialCode(Long tenantId, String materialCode);

    /**
     * Counts materials by tenant.
     */
    long countByTenantId(Long tenantId);

    /**
     * Counts materials by tenant and status.
     */
    long countByTenantIdAndStatus(Long tenantId, Status status);
}
