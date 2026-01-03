package com.smtech.SM_Caterer.service;

import com.smtech.SM_Caterer.domain.enums.OrderStatus;
import com.smtech.SM_Caterer.service.base.BaseService;
import com.smtech.SM_Caterer.service.dto.OrderDTO;
import com.smtech.SM_Caterer.service.dto.OrderDetailDTO;
import com.smtech.SM_Caterer.service.dto.OrderSearchCriteria;
import com.smtech.SM_Caterer.web.dto.OrderFormDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for Order operations.
 * Supports full order lifecycle workflow.
 */
public interface OrderService extends BaseService<OrderDTO, Long> {

    // ===== Basic Queries =====

    Optional<OrderDTO> findByTenantIdAndOrderNumber(Long tenantId, String orderNumber);

    Page<OrderDTO> findByTenantId(Long tenantId, Pageable pageable);

    List<OrderDTO> findByCustomerId(Long customerId);

    List<OrderDTO> findByEventDate(LocalDate eventDate);

    List<OrderDTO> findByStatus(OrderStatus status);

    // ===== Detail View =====

    /**
     * Find order with all details loaded (for view page).
     */
    Optional<OrderDetailDTO> findByIdWithDetails(Long orderId);

    // ===== Search & Filter =====

    /**
     * Search orders by multiple criteria.
     */
    Page<OrderDTO> searchOrders(Long tenantId, OrderSearchCriteria criteria, Pageable pageable);

    // ===== Order Creation from Wizard =====

    /**
     * Create order from wizard form DTO.
     */
    OrderDTO createFromForm(OrderFormDTO formDTO, Long tenantId, Long userId);

    /**
     * Update order from wizard form DTO (for editing draft/pending orders).
     */
    OrderDTO updateFromForm(Long orderId, OrderFormDTO formDTO, Long userId);

    // ===== Workflow Methods =====

    /**
     * Submit order for approval.
     * Transitions: DRAFT -> PENDING
     */
    OrderDTO submit(Long orderId, Long userId);

    /**
     * Approve order.
     * Transitions: PENDING -> CONFIRMED
     */
    OrderDTO approve(Long orderId, Long userId);

    /**
     * Reject order (return to draft).
     * Transitions: PENDING -> DRAFT
     */
    OrderDTO reject(Long orderId, Long userId, String reason);

    /**
     * Cancel order with reason.
     * Transitions: any (except COMPLETED/CANCELLED) -> CANCELLED
     */
    OrderDTO cancel(Long orderId, Long userId, String reason);

    /**
     * Start order execution.
     * Transitions: CONFIRMED -> IN_PROGRESS
     */
    OrderDTO startProgress(Long orderId, Long userId);

    /**
     * Complete order.
     * Transitions: IN_PROGRESS -> COMPLETED
     */
    OrderDTO complete(Long orderId, Long userId);

    /**
     * Update order status with audit trail.
     */
    OrderDTO updateStatus(Long orderId, OrderStatus newStatus, Long userId, String notes);

    // ===== Clone =====

    /**
     * Clone an existing order with new event date.
     */
    OrderDTO cloneOrder(Long orderId, LocalDate newEventDate, Long userId);

    // ===== Dashboard Queries =====

    /**
     * Count orders by status for tenant.
     */
    long countByStatus(Long tenantId, OrderStatus status);

    /**
     * Count today's orders.
     */
    long countTodaysOrders(Long tenantId);

    /**
     * Count pending approval orders.
     */
    long countPendingApproval(Long tenantId);

    /**
     * Get upcoming orders (next 7 days).
     */
    List<OrderDTO> getUpcomingOrders(Long tenantId, int days);

    /**
     * Get recent orders.
     */
    List<OrderDTO> getRecentOrders(Long tenantId, int limit);
}
