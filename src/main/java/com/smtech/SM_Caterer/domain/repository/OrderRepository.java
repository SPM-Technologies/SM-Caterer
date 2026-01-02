package com.smtech.SM_Caterer.domain.repository;

import com.smtech.SM_Caterer.domain.entity.Order;
import com.smtech.SM_Caterer.domain.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Order entity.
 *
 * Performance:
 * - Uses EntityGraph to prevent N+1 queries
 * - Optimized for dashboard and calendar views
 */
@Repository
public interface OrderRepository extends BaseRepository<Order, Long> {

    /**
     * Finds order by tenant and order number.
     */
    Optional<Order> findByTenantIdAndOrderNumber(Long tenantId, String orderNumber);

    /**
     * Finds all orders for tenant with related entities loaded.
     * Uses EntityGraph to prevent N+1 queries.
     */
    @EntityGraph(attributePaths = {"customer", "eventType", "menuItems", "utilities"})
    List<Order> findByTenantId(Long tenantId);

    /**
     * Paginated query with eager loading.
     */
    @EntityGraph(attributePaths = {"customer", "eventType", "menuItems", "utilities"})
    Page<Order> findByTenantId(Long tenantId, Pageable pageable);

    /**
     * Finds orders by tenant and status.
     */
    List<Order> findByTenantIdAndStatus(Long tenantId, OrderStatus status);

    /**
     * Finds orders by tenant and status with pagination.
     */
    Page<Order> findByTenantIdAndStatus(Long tenantId, OrderStatus status, Pageable pageable);

    /**
     * Finds orders by tenant and customer.
     */
    List<Order> findByTenantIdAndCustomerId(Long tenantId, Long customerId);

    /**
     * Finds orders by customer.
     */
    List<Order> findByCustomerId(Long customerId);

    /**
     * Finds orders by tenant and event date.
     */
    List<Order> findByTenantIdAndEventDate(Long tenantId, LocalDate eventDate);

    /**
     * Finds orders by event date.
     */
    List<Order> findByEventDate(LocalDate eventDate);

    /**
     * Finds orders by status.
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * Finds orders by tenant and event date range.
     */
    @Query("SELECT o FROM Order o WHERE o.tenant.id = :tenantId " +
           "AND o.eventDate BETWEEN :startDate AND :endDate " +
           "ORDER BY o.eventDate")
    List<Order> findByEventDateRange(@Param("tenantId") Long tenantId,
                                     @Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate);

    /**
     * Finds upcoming orders (future event dates).
     */
    @Query("SELECT o FROM Order o WHERE o.tenant.id = :tenantId " +
           "AND o.eventDate >= :today AND o.status NOT IN ('CANCELLED', 'COMPLETED') " +
           "ORDER BY o.eventDate")
    List<Order> findUpcomingOrders(@Param("tenantId") Long tenantId, @Param("today") LocalDate today);

    /**
     * Finds orders with pending payments.
     */
    @Query("SELECT o FROM Order o WHERE o.tenant.id = :tenantId " +
           "AND o.balanceAmount > 0 AND o.status NOT IN ('CANCELLED', 'DRAFT')")
    List<Order> findOrdersWithPendingPayments(@Param("tenantId") Long tenantId);

    /**
     * Finds orders for dashboard (today and upcoming).
     */
    @Query("SELECT o FROM Order o WHERE o.tenant.id = :tenantId " +
           "AND o.eventDate >= :today " +
           "ORDER BY o.eventDate LIMIT 10")
    List<Order> findDashboardOrders(@Param("tenantId") Long tenantId, @Param("today") LocalDate today);

    /**
     * Checks if order number exists for tenant.
     */
    boolean existsByTenantIdAndOrderNumber(Long tenantId, String orderNumber);

    /**
     * Counts orders by tenant.
     */
    long countByTenantId(Long tenantId);

    /**
     * Counts orders by tenant and status.
     */
    long countByTenantIdAndStatus(Long tenantId, OrderStatus status);

    /**
     * Counts orders by tenant and event date range.
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.tenant.id = :tenantId " +
           "AND o.eventDate BETWEEN :startDate AND :endDate")
    long countByEventDateRange(@Param("tenantId") Long tenantId,
                               @Param("startDate") LocalDate startDate,
                               @Param("endDate") LocalDate endDate);
}
