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

    /**
     * Finds material by ID and tenant ID (tenant isolation).
     */
    Optional<Material> findByIdAndTenantId(Long id, Long tenantId);

    // =====================================================
    // PHASE 6: STOCK REPORT QUERIES
    // =====================================================

    /**
     * Finds all active materials with full details for stock report.
     */
    @EntityGraph(attributePaths = {"materialGroup", "unit", "translations"})
    @Query("SELECT m FROM Material m WHERE m.tenant.id = :tenantId " +
           "AND m.status = 'ACTIVE' AND m.deletedAt IS NULL " +
           "ORDER BY m.materialGroup.id, m.materialCode")
    List<Material> findAllForStockReport(@Param("tenantId") Long tenantId);

    /**
     * Finds materials for stock report with pagination.
     */
    @EntityGraph(attributePaths = {"materialGroup", "unit"})
    @Query("SELECT m FROM Material m WHERE m.tenant.id = :tenantId " +
           "AND (:groupId IS NULL OR m.materialGroup.id = :groupId) " +
           "AND (:status IS NULL OR m.status = :status) " +
           "AND m.deletedAt IS NULL " +
           "ORDER BY m.materialGroup.id, m.materialCode")
    Page<Material> findMaterialsForReport(@Param("tenantId") Long tenantId,
                                          @Param("groupId") Long groupId,
                                          @Param("status") Status status,
                                          Pageable pageable);

    /**
     * Counts low stock materials.
     */
    @Query("SELECT COUNT(m) FROM Material m WHERE m.tenant.id = :tenantId " +
           "AND m.currentStock < m.minimumStock AND m.status = 'ACTIVE' AND m.deletedAt IS NULL")
    Long countLowStockMaterials(@Param("tenantId") Long tenantId);

    /**
     * Counts out of stock materials.
     */
    @Query("SELECT COUNT(m) FROM Material m WHERE m.tenant.id = :tenantId " +
           "AND m.currentStock <= 0 AND m.status = 'ACTIVE' AND m.deletedAt IS NULL")
    Long countOutOfStockMaterials(@Param("tenantId") Long tenantId);

    /**
     * Gets total stock value for tenant.
     */
    @Query("SELECT COALESCE(SUM(m.currentStock * m.costPerUnit), 0) FROM Material m " +
           "WHERE m.tenant.id = :tenantId AND m.status = 'ACTIVE' AND m.deletedAt IS NULL")
    java.math.BigDecimal getTotalStockValue(@Param("tenantId") Long tenantId);

    /**
     * Gets stock value by material group.
     */
    @Query("SELECT m.materialGroup.id, COALESCE(SUM(m.currentStock * m.costPerUnit), 0) " +
           "FROM Material m WHERE m.tenant.id = :tenantId " +
           "AND m.status = 'ACTIVE' AND m.deletedAt IS NULL " +
           "GROUP BY m.materialGroup.id")
    List<Object[]> getStockValueByGroup(@Param("tenantId") Long tenantId);

    /**
     * Finds critical low stock materials (below 50% of minimum).
     */
    @EntityGraph(attributePaths = {"materialGroup", "unit"})
    @Query("SELECT m FROM Material m WHERE m.tenant.id = :tenantId " +
           "AND m.currentStock < (m.minimumStock * 0.5) " +
           "AND m.status = 'ACTIVE' AND m.deletedAt IS NULL " +
           "ORDER BY (m.currentStock / m.minimumStock) ASC")
    List<Material> findCriticalLowStockMaterials(@Param("tenantId") Long tenantId);

    /**
     * Finds materials by stock status.
     */
    @EntityGraph(attributePaths = {"materialGroup", "unit"})
    @Query("SELECT m FROM Material m WHERE m.tenant.id = :tenantId " +
           "AND m.status = 'ACTIVE' AND m.deletedAt IS NULL " +
           "AND ((:stockStatus = 'OUT_OF_STOCK' AND m.currentStock <= 0) " +
           "OR (:stockStatus = 'LOW_STOCK' AND m.currentStock > 0 AND m.currentStock < m.minimumStock) " +
           "OR (:stockStatus = 'IN_STOCK' AND m.currentStock >= m.minimumStock))")
    List<Material> findByStockStatus(@Param("tenantId") Long tenantId, @Param("stockStatus") String stockStatus);
}
