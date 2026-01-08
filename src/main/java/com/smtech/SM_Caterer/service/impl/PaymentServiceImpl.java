package com.smtech.SM_Caterer.service.impl;

import com.smtech.SM_Caterer.domain.entity.Order;
import com.smtech.SM_Caterer.domain.entity.Payment;
import com.smtech.SM_Caterer.domain.entity.Tenant;
import com.smtech.SM_Caterer.domain.enums.PaymentMethod;
import com.smtech.SM_Caterer.domain.enums.PaymentStatus;
import com.smtech.SM_Caterer.domain.repository.OrderRepository;
import com.smtech.SM_Caterer.domain.repository.PaymentRepository;
import com.smtech.SM_Caterer.domain.repository.TenantRepository;
import com.smtech.SM_Caterer.exception.InvalidOperationException;
import com.smtech.SM_Caterer.exception.ResourceNotFoundException;
import com.smtech.SM_Caterer.service.*;
import com.smtech.SM_Caterer.service.base.BaseServiceImpl;
import com.smtech.SM_Caterer.service.dto.PaymentDTO;
import com.smtech.SM_Caterer.service.mapper.EntityMapper;
import com.smtech.SM_Caterer.service.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service implementation for Payment operations.
 *
 * Business Logic:
 * - Payment amount validation
 * - Payment status management
 * - Order payment tracking
 * - PDF receipt generation
 * - Email notifications
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PaymentServiceImpl extends BaseServiceImpl<Payment, PaymentDTO, Long>
        implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final OrderRepository orderRepository;
    private final TenantRepository tenantRepository;
    private final PaymentNumberGeneratorService paymentNumberGeneratorService;
    private final PdfReceiptService pdfReceiptService;
    private final EmailService emailService;

    @Override
    protected JpaRepository<Payment, Long> getRepository() {
        return paymentRepository;
    }

    @Override
    protected EntityMapper<PaymentDTO, Payment> getMapper() {
        return paymentMapper;
    }

    @Override
    protected String getEntityName() {
        return "Payment";
    }

    @Override
    @Transactional
    public PaymentDTO create(PaymentDTO dto) {
        return createPaymentWithWorkflow(dto);
    }

    @Override
    @Transactional
    public PaymentDTO createPaymentWithWorkflow(PaymentDTO dto) {
        log.debug("Creating new payment for order ID: {}", dto.getOrderId());

        // Validate order exists
        Order order = orderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", dto.getOrderId()));

        // Get tenant
        Tenant tenant = order.getTenant();

        // Check if payment feature is enabled
        if (!tenant.isPaymentFeatureEnabled()) {
            throw new InvalidOperationException("Payment feature is not enabled for this tenant");
        }

        // Validate payment amount
        validatePaymentAmount(dto, order);

        // Create payment entity
        Payment entity = paymentMapper.toEntity(dto);
        entity.setOrder(order);
        entity.setTenant(tenant);

        // Generate payment number
        String paymentNumber = paymentNumberGeneratorService.generatePaymentNumber(tenant.getId());
        entity.setPaymentNumber(paymentNumber);

        // Save payment
        Payment saved = paymentRepository.save(entity);
        log.info("Payment created (ID: {}, Number: {}) for order ID: {}, Amount: {}",
                saved.getId(), paymentNumber, dto.getOrderId(), saved.getAmount());

        // Generate PDF receipt
        try {
            String receiptPath = pdfReceiptService.generateReceipt(saved);
            saved.setReceiptPath(receiptPath);
            saved = paymentRepository.save(saved);
        } catch (Exception e) {
            log.error("Failed to generate PDF receipt for payment {}: {}", paymentNumber, e.getMessage());
        }

        // Send email notification (async)
        if (emailService.isEmailEnabled(tenant)) {
            try {
                emailService.sendPaymentReceipt(saved, saved.getReceiptPath());
                saved.setEmailSent(true);
                saved = paymentRepository.save(saved);
            } catch (Exception e) {
                log.error("Failed to send email for payment {}: {}", paymentNumber, e.getMessage());
            }
        }

        return paymentMapper.toDto(saved);
    }

    private void validatePaymentAmount(PaymentDTO dto, Order order) {
        if (order.getTotalAmount() == null) {
            return; // No validation possible
        }

        BigDecimal totalPaid = calculateTotalPaidForOrder(order.getId());
        BigDecimal newPayment = dto.getAmount();
        BigDecimal totalAfterPayment = totalPaid.add(newPayment);

        if (totalAfterPayment.compareTo(order.getTotalAmount()) > 0) {
            BigDecimal balance = order.getTotalAmount().subtract(totalPaid);
            throw new InvalidOperationException(
                    String.format("Payment amount (%.2f) exceeds order balance (%.2f)",
                            newPayment.doubleValue(), balance.doubleValue()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDTO> findByOrderId(Long orderId) {
        return paymentMapper.toDto(paymentRepository.findByOrderId(orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentDTO> findByOrderId(Long orderId, Pageable pageable) {
        // Note: For now, get all and create page manually
        List<Payment> payments = paymentRepository.findByOrderId(orderId);
        return Page.empty(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDTO> findByTenantId(Long tenantId) {
        return paymentMapper.toDto(paymentRepository.findByTenantId(tenantId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDTO> findByStatus(PaymentStatus status) {
        return paymentMapper.toDto(paymentRepository.findByStatus(status));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentDTO> findByIdAndTenantId(Long id, Long tenantId) {
        return paymentRepository.findByIdAndTenantId(id, tenantId)
                .map(paymentMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentDTO> searchPayments(Long tenantId, Long orderId, PaymentStatus status,
                                            PaymentMethod method, LocalDate dateFrom, LocalDate dateTo,
                                            Pageable pageable) {
        return paymentRepository.searchPayments(tenantId, orderId, status, method, dateFrom, dateTo, pageable)
                .map(paymentMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalPaidForOrder(Long orderId) {
        BigDecimal total = paymentRepository.sumCompletedPaymentsByOrderId(orderId);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateBalanceDue(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getTotalAmount() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalPaid = calculateTotalPaidForOrder(orderId);
        return order.getTotalAmount().subtract(totalPaid);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadReceipt(Long paymentId, Long tenantId) {
        Payment payment = paymentRepository.findByIdAndTenantId(paymentId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));

        return pdfReceiptService.generateReceiptBytes(payment);
    }

    @Override
    @Transactional
    public boolean resendReceiptEmail(Long paymentId, Long tenantId) {
        Payment payment = paymentRepository.findByIdAndTenantId(paymentId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));

        Tenant tenant = payment.getTenant();
        if (!emailService.isEmailEnabled(tenant)) {
            log.warn("Email not enabled for tenant: {}", tenantId);
            return false;
        }

        try {
            emailService.sendPaymentReceipt(payment, payment.getReceiptPath());
            payment.setEmailSent(true);
            paymentRepository.save(payment);
            return true;
        } catch (Exception e) {
            log.error("Failed to resend email for payment {}: {}", payment.getPaymentNumber(), e.getMessage());
            return false;
        }
    }
}
