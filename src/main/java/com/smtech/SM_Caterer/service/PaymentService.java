package com.smtech.SM_Caterer.service;

import com.smtech.SM_Caterer.domain.enums.PaymentMethod;
import com.smtech.SM_Caterer.domain.enums.PaymentStatus;
import com.smtech.SM_Caterer.service.base.BaseService;
import com.smtech.SM_Caterer.service.dto.PaymentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for Payment operations.
 */
public interface PaymentService extends BaseService<PaymentDTO, Long> {

    /**
     * Finds all payments for an order.
     */
    List<PaymentDTO> findByOrderId(Long orderId);

    /**
     * Finds all payments for a tenant.
     */
    List<PaymentDTO> findByTenantId(Long tenantId);

    /**
     * Finds payments by status.
     */
    List<PaymentDTO> findByStatus(PaymentStatus status);

    /**
     * Finds payment by ID and tenant ID.
     */
    Optional<PaymentDTO> findByIdAndTenantId(Long id, Long tenantId);

    /**
     * Search payments with filters.
     */
    Page<PaymentDTO> searchPayments(Long tenantId, Long orderId, PaymentStatus status,
                                     PaymentMethod method, LocalDate dateFrom, LocalDate dateTo,
                                     Pageable pageable);

    /**
     * Creates a payment with full workflow (number generation, PDF, email).
     */
    PaymentDTO createPaymentWithWorkflow(PaymentDTO dto);

    /**
     * Calculates total paid amount for an order.
     */
    BigDecimal calculateTotalPaidForOrder(Long orderId);

    /**
     * Calculates balance due for an order.
     */
    BigDecimal calculateBalanceDue(Long orderId);

    /**
     * Gets payments for order with pagination.
     */
    Page<PaymentDTO> findByOrderId(Long orderId, Pageable pageable);

    /**
     * Downloads receipt PDF as bytes.
     */
    byte[] downloadReceipt(Long paymentId, Long tenantId);

    /**
     * Resends payment receipt email.
     */
    boolean resendReceiptEmail(Long paymentId, Long tenantId);
}
