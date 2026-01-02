package com.smtech.SM_Caterer.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Order Utility entity - line item for utility in an order.
 */
@Entity
@Table(name = "order_utilities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"order", "utility"})
@EqualsAndHashCode(exclude = {"order", "utility"})
public class OrderUtility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @NotNull(message = "Order is required")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utility_id", nullable = false)
    @NotNull(message = "Utility is required")
    private Utility utility;

    @Column(name = "quantity", nullable = false)
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @Column(name = "price_per_item", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Price per item is required")
    @DecimalMin(value = "0.00", message = "Price per item must be non-negative")
    private BigDecimal pricePerItem;

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    @NotNull(message = "Subtotal is required")
    @DecimalMin(value = "0.00", message = "Subtotal must be non-negative")
    private BigDecimal subtotal;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Version
    @Column(name = "version")
    private Long version;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        recalculateSubtotal();
    }

    @PreUpdate
    protected void onUpdate() {
        recalculateSubtotal();
    }

    /**
     * Calculates and updates subtotal.
     */
    @Transient
    public void recalculateSubtotal() {
        if (quantity != null && pricePerItem != null) {
            this.subtotal = pricePerItem.multiply(BigDecimal.valueOf(quantity));
        }
    }
}
