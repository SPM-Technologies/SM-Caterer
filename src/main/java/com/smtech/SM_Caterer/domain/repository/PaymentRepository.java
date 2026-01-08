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
import java.util.Optional;

/**
 * Repository for Payment entity.
 */
@Repository
public interface PaymentRepository extends BaseRepository<Payment, Long> {

    /**
     * Finds all payments for order.
     */
    List<Payment> findByOrderId(Long orderId);

    /**
     * Finds all payments for tenant.
     */
    List<Payment> findByTenantId(Long tenantId);

    /**
     * Finds all payments for tenant with pagination.
     */
    Page<Payment> findByTenantId(Long tenantId, Pageable pageable);

    /**
     * Finds payment by ID and tenant ID.
     */
    Optional<Payment> findByIdAndTenantId(Long id, Long tenantId);

    /**
     * Finds payments by tenant and order.
     */
    @Query("SELECT p FROM Payment p WHERE p.tenant.id = :tenantId AND p.order.id = :orderId")
    List<Payment> findByTenantIdAndOrderId(@Param("tenantId") Long tenantId, @Param("orderId") Long orderId);

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
     * Calculates sum of completed payments for order.
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
           "WHERE p.order.id = :orderId AND p.status = 'COMPLETED'")
    BigDecimal sumCompletedPaymentsByOrderId(@Param("orderId") Long orderId);

    /**
     * Calculates total payments for order (alias for compatibility).
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
     * Counts payments with payment number prefix for generating next number.
     */
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.tenant.id = :tenantId AND p.paymentNumber LIKE :prefix%")
    long countByTenantIdAndPaymentNumberStartingWith(@Param("tenantId") Long tenantId, @Param("prefix") String prefix);

    /**
     * Search payments with filters.
     */
    @Query("""
        SELECT p FROM Payment p
        WHERE p.tenant.id = :tenantId
        AND (:orderId IS NULL OR p.order.id = :orderId)
        AND (:status IS NULL OR p.status = :status)
        AND (:method IS NULL OR p.paymentMethod = :method)
        AND (:dateFrom IS NULL OR p.paymentDate >= :dateFrom)
        AND (:dateTo IS NULL OR p.paymentDate <= :dateTo)
        ORDER BY p.paymentDate DESC
    """)
    Page<Payment> searchPayments(
        @Param("tenantId") Long tenantId,
        @Param("orderId") Long orderId,
        @Param("status") PaymentStatus status,
        @Param("method") PaymentMethod method,
        @Param("dateFrom") LocalDate dateFrom,
        @Param("dateTo") LocalDate dateTo,
        Pageable pageable
    );

    /**
     * Counts today's payments for tenant.
     */
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.tenant.id = :tenantId AND p.paymentDate = :date")
    long countTodayPayments(@Param("tenantId") Long tenantId, @Param("date") LocalDate date);

    /**
     * Sums today's completed payments for tenant.
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.tenant.id = :tenantId AND p.paymentDate = :date AND p.status = 'COMPLETED'")
    BigDecimal sumTodayPayments(@Param("tenantId") Long tenantId, @Param("date") LocalDate date);

    /**
     * Sums payments between dates for tenant.
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.tenant.id = :tenantId AND p.paymentDate BETWEEN :startDate AND :endDate AND p.status = 'COMPLETED'")
    BigDecimal sumPaymentsBetweenDates(@Param("tenantId") Long tenantId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

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
