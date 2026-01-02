package com.smtech.SM_Caterer.domain.repository;

import com.smtech.SM_Caterer.domain.entity.Payment;
import com.smtech.SM_Caterer.domain.enums.PaymentMethod;
import com.smtech.SM_Caterer.domain.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository for Payment entity.
 */
@Repository
public interface PaymentRepository extends BaseRepository<Payment, Long> {

    /**
     * Finds all payments for order.
     * @param orderId Order ID
     * @return List of payments
     */
    List<Payment> findByOrderId(Long orderId);

    /**
     * Finds all payments for tenant.
     * @param tenantId Tenant ID
     * @return List of payments
     */
    List<Payment> findByTenantId(Long tenantId);

    /**
     * Finds all payments for tenant with pagination.
     * @param tenantId Tenant ID
     * @param pageable Pagination parameters
     * @return Page of payments
     */
    Page<Payment> findByTenantId(Long tenantId, Pageable pageable);

    /**
     * Finds payments by tenant and payment date.
     */
    List<Payment> findByTenantIdAndPaymentDate(Long tenantId, LocalDate paymentDate);

    /**
     * Finds payments by tenant and date range.
     */
    @Query("SELECT p FROM Payment p WHERE p.tenant.id = :tenantId " +
           "AND p.paymentDate BETWEEN :startDate AND :endDate " +
           "ORDER BY p.paymentDate DESC")
    List<Payment> findByDateRange(@Param("tenantId") Long tenantId,
                                  @Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate);

    /**
     * Finds payments by tenant and payment method.
     */
    List<Payment> findByTenantIdAndPaymentMethod(Long tenantId, PaymentMethod paymentMethod);

    /**
     * Finds payments by tenant and status.
     */
    List<Payment> findByTenantIdAndStatus(Long tenantId, PaymentStatus status);

    /**
     * Finds payments by status.
     */
    List<Payment> findByStatus(PaymentStatus status);

    /**
     * Calculates total payments for order.
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
           "WHERE p.order.id = :orderId AND p.status = 'COMPLETED'")
    BigDecimal calculateTotalPaymentsForOrder(@Param("orderId") Long orderId);

    /**
     * Calculates total payments for tenant by date range.
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
           "WHERE p.tenant.id = :tenantId " +
           "AND p.paymentDate BETWEEN :startDate AND :endDate " +
           "AND p.status = 'COMPLETED'")
    BigDecimal calculateTotalPaymentsByDateRange(@Param("tenantId") Long tenantId,
                                                 @Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);

    /**
     * Finds payments by transaction reference.
     */
    @Query("SELECT p FROM Payment p WHERE p.tenant.id = :tenantId " +
           "AND p.transactionReference = :transactionReference")
    List<Payment> findByTransactionReference(@Param("tenantId") Long tenantId,
                                             @Param("transactionReference") String transactionReference);

    /**
     * Deletes all payments for order.
     */
    void deleteByOrderId(Long orderId);

    /**
     * Counts payments for order.
     */
    long countByOrderId(Long orderId);

    /**
     * Counts payments by tenant.
     */
    long countByTenantId(Long tenantId);

    /**
     * Counts payments by tenant and payment method.
     */
    long countByTenantIdAndPaymentMethod(Long tenantId, PaymentMethod paymentMethod);
}
