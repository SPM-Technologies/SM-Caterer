package com.smtech.SM_Caterer.service.impl;

import com.smtech.SM_Caterer.domain.entity.Order;
import com.smtech.SM_Caterer.domain.entity.Payment;
import com.smtech.SM_Caterer.domain.enums.PaymentStatus;
import com.smtech.SM_Caterer.domain.repository.OrderRepository;
import com.smtech.SM_Caterer.domain.repository.PaymentRepository;
import com.smtech.SM_Caterer.exception.ResourceNotFoundException;
import com.smtech.SM_Caterer.service.PaymentService;
import com.smtech.SM_Caterer.service.base.BaseServiceImpl;
import com.smtech.SM_Caterer.service.dto.PaymentDTO;
import com.smtech.SM_Caterer.service.mapper.EntityMapper;
import com.smtech.SM_Caterer.service.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service implementation for Payment operations.
 *
 * Business Logic:
 * - Payment amount validation
 * - Payment status management
 * - Order payment tracking
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
        log.debug("Creating new payment for order ID: {}", dto.getOrderId());

        Payment entity = paymentMapper.toEntity(dto);

        // Set order reference
        if (dto.getOrderId() != null) {
            Order order = orderRepository.findById(dto.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order", "id", dto.getOrderId()));
            entity.setOrder(order);

            // Validate payment amount against order total
            if (order.getTotalAmount() != null && entity.getAmount() != null) {
                BigDecimal totalPaid = paymentRepository.findByOrderId(dto.getOrderId()).stream()
                        .map(Payment::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .add(entity.getAmount());

                if (totalPaid.compareTo(order.getTotalAmount()) > 0) {
                    log.warn("Payment amount exceeds order total. Order: {}, Total: {}, Already Paid: {}, New Payment: {}",
                            order.getOrderNumber(), order.getTotalAmount(),
                            totalPaid.subtract(entity.getAmount()), entity.getAmount());
                }
            }
        }

        Payment saved = paymentRepository.save(entity);
        log.info("Payment created (ID: {}) for order ID: {}, Amount: {}",
                saved.getId(), dto.getOrderId(), saved.getAmount());

        return paymentMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDTO> findByOrderId(Long orderId) {
        return paymentMapper.toDto(paymentRepository.findByOrderId(orderId));
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
}
