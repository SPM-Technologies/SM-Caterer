package com.smtech.SM_Caterer.domain.repository;

import com.smtech.SM_Caterer.domain.entity.OrderMenuItem;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for OrderMenuItem entity.
 */
@Repository
public interface OrderMenuItemRepository extends BaseRepository<OrderMenuItem, Long> {

    /**
     * Finds all menu items for order.
     * @param orderId Order ID
     * @return List of menu items
     */
    List<OrderMenuItem> findByOrderId(Long orderId);

    /**
     * Finds menu items by order and menu.
     * @param orderId Order ID
     * @param menuId Menu ID
     * @return List of menu items
     */
    List<OrderMenuItem> findByOrderIdAndMenuId(Long orderId, Long menuId);

    /**
     * Finds menu items with menu details for order.
     * Used for calculating total cost.
     */
    @Query("SELECT omi FROM OrderMenuItem omi " +
           "JOIN FETCH omi.menu m " +
           "WHERE omi.order.id = :orderId")
    List<OrderMenuItem> findByOrderIdWithMenuDetails(@Param("orderId") Long orderId);

    /**
     * Deletes all menu items for order.
     */
    void deleteByOrderId(Long orderId);

    /**
     * Counts menu items for order.
     */
    long countByOrderId(Long orderId);

    /**
     * Finds all order menu items for menu.
     * @param menuId Menu ID
     * @return List of order menu items
     */
    List<OrderMenuItem> findByMenuId(Long menuId);

    /**
     * Finds orders containing specific menu by tenant.
     * Used to check if menu is in use before deletion.
     */
    @Query("SELECT omi FROM OrderMenuItem omi " +
           "WHERE omi.menu.id = :menuId " +
           "AND omi.order.tenant.id = :tenantId")
    List<OrderMenuItem> findByTenantIdAndMenuId(@Param("tenantId") Long tenantId, @Param("menuId") Long menuId);
}
