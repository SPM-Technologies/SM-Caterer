package com.smtech.SM_Caterer.domain.entity;

import com.smtech.SM_Caterer.domain.enums.PaymentMethod;
import com.smtech.SM_Caterer.domain.enums.PaymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Payment entity for tracking order payments.
 * Extends TenantBaseEntity for consistent multi-tenant support.
 */
@Entity
@Table(name = "payments",
       indexes = {
           @Index(name = "idx_payments_tenant_id", columnList = "tenant_id"),
           @Index(name = "idx_payments_order_id", columnList = "order_id"),
           @Index(name = "idx_payments_payment_date", columnList = "payment_date"),
           @Index(name = "idx_payments_payment_number", columnList = "payment_number"),
           @Index(name = "idx_payments_deleted_at", columnList = "deleted_at")
       })
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@SQLDelete(sql = "UPDATE payments SET deleted_at = NOW(), version = version + 1 WHERE id = ? AND version = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"order", "createdByUser"})
@EqualsAndHashCode(callSuper = true, exclude = {"order", "createdByUser"})
public class Payment extends TenantBaseEntity {

    @Column(name = "payment_number", length = 50)
    private String paymentNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @NotNull(message = "Order is required")
    private Order order;

    @Column(name = "payment_date", nullable = false)
    @NotNull(message = "Payment date is required")
    private LocalDate paymentDate;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @Column(name = "transaction_reference", length = 100)
    @Size(max = 100, message = "Transaction reference must not exceed 100 characters")
    private String transactionReference;

    @Column(name = "upi_id", length = 100)
    @Size(max = 100, message = "UPI ID must not exceed 100 characters")
    private String upiId;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull(message = "Status is required")
    @Builder.Default
    private PaymentStatus status = PaymentStatus.COMPLETED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", insertable = false, updatable = false)
    private User createdByUser;

    @Column(name = "receipt_path", length = 500)
    private String receiptPath;

    @Column(name = "email_sent")
    @Builder.Default
    private Boolean emailSent = false;

    @PrePersist
    @Override
    protected void validateTenant() {
        super.validateTenant();
        if (status == null) {
            status = PaymentStatus.COMPLETED;
        }
    }

    /**
     * Get order number for display.
     */
    @Transient
    public String getOrderNumber() {
        return order != null ? order.getOrderNumber() : null;
    }

    /**
     * Get customer name for display.
     */
    @Transient
    public String getCustomerName() {
        return order != null && order.getCustomer() != null
            ? order.getCustomer().getName() : null;
    }
}
