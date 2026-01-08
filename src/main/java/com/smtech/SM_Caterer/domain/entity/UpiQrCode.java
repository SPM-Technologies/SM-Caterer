package com.smtech.SM_Caterer.domain.entity;

import com.smtech.SM_Caterer.domain.enums.UpiQrCodeStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Filter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * UPI QR Code entity for generating payment QR codes.
 * Extends TenantBaseEntity for consistent multi-tenant support.
 */
@Entity
@Table(name = "upi_qr_codes",
       indexes = {
           @Index(name = "idx_upi_qr_tenant_id", columnList = "tenant_id"),
           @Index(name = "idx_upi_qr_order_id", columnList = "order_id"),
           @Index(name = "idx_upi_qr_status", columnList = "status")
       })
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"order"})
@EqualsAndHashCode(callSuper = true, exclude = {"order"})
public class UpiQrCode extends TenantBaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @NotNull(message = "Order is required")
    private Order order;

    @Column(name = "upi_id", nullable = false, length = 100)
    @NotBlank(message = "UPI ID is required")
    @Size(max = 100, message = "UPI ID must not exceed 100 characters")
    private String upiId;

    @Column(name = "payee_name", nullable = false, length = 200)
    @NotBlank(message = "Payee name is required")
    @Size(max = 200, message = "Payee name must not exceed 200 characters")
    private String payeeName;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @Column(name = "transaction_note", length = 255)
    @Size(max = 255, message = "Transaction note must not exceed 255 characters")
    private String transactionNote;

    @Column(name = "qr_code_path", length = 500)
    @Size(max = 500, message = "QR code path must not exceed 500 characters")
    private String qrCodePath;

    @Column(name = "upi_deep_link", columnDefinition = "TEXT")
    private String upiDeepLink;

    @Column(name = "generated_at", nullable = false, updatable = false)
    private LocalDateTime generatedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull(message = "Status is required")
    @Builder.Default
    private UpiQrCodeStatus status = UpiQrCodeStatus.ACTIVE;

    @PrePersist
    @Override
    protected void validateTenant() {
        super.validateTenant();
        if (status == null) {
            status = UpiQrCodeStatus.ACTIVE;
        }
        if (generatedAt == null) {
            generatedAt = LocalDateTime.now();
        }
    }

    /**
     * Checks if QR code has expired.
     */
    @Transient
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Marks QR code as used.
     */
    public void markAsUsed() {
        this.status = UpiQrCodeStatus.USED;
    }

    /**
     * Marks QR code as expired.
     */
    public void markAsExpired() {
        this.status = UpiQrCodeStatus.EXPIRED;
    }
}
