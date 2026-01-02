package com.smtech.SM_Caterer.domain.repository;

import com.smtech.SM_Caterer.domain.entity.Menu;
import com.smtech.SM_Caterer.domain.enums.MenuCategory;
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
 * Repository for Menu entity.
 *
 * Performance:
 * - Uses EntityGraph to prevent N+1 queries
 * - Optimized for menu listing with translations
 */
@Repository
public interface MenuRepository extends BaseRepository<Menu, Long> {

    /**
     * Finds menu by tenant and menu code.
     */
    Optional<Menu> findByTenantIdAndMenuCode(Long tenantId, String menuCode);

    /**
     * Finds all menus for tenant with translations and recipe items loaded.
     * Uses EntityGraph to prevent N+1 queries.
     */
    @EntityGraph(attributePaths = {"translations", "recipeItems"})
    List<Menu> findByTenantId(Long tenantId);

    /**
     * Paginated query with eager loading.
     */
    @EntityGraph(attributePaths = {"translations", "recipeItems"})
    Page<Menu> findByTenantId(Long tenantId, Pageable pageable);

    /**
     * Finds menus by tenant and category.
     */
    List<Menu> findByTenantIdAndCategory(Long tenantId, MenuCategory category);

    /**
     * Finds menus by tenant and status.
     */
    List<Menu> findByTenantIdAndStatus(Long tenantId, Status status);

    /**
     * Finds menus by tenant, category and status.
     */
    @Query("SELECT m FROM Menu m WHERE m.tenant.id = :tenantId " +
           "AND m.category = :category AND m.status = :status")
    List<Menu> findByTenantIdAndCategoryAndStatus(@Param("tenantId") Long tenantId,
                                                   @Param("category") MenuCategory category,
                                                   @Param("status") Status status);

    /**
     * Checks if menu code exists for tenant.
     */
    boolean existsByTenantIdAndMenuCode(Long tenantId, String menuCode);

    /**
     * Counts menus by tenant.
     */
    long countByTenantId(Long tenantId);

    /**
     * Counts menus by tenant and category.
     */
    long countByTenantIdAndCategory(Long tenantId, MenuCategory category);

    /**
     * Counts menus by tenant and status.
     */
    long countByTenantIdAndStatus(Long tenantId, Status status);
}
