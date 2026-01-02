package com.smtech.SM_Caterer.domain.entity;

import com.smtech.SM_Caterer.domain.enums.OrderStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order entity representing a catering order/booking.
 */
@Entity
@Table(name = "orders",
       uniqueConstraints = {
           @UniqueConstraint(name = "unique_tenant_order", columnNames = {"tenant_id", "order_number"})
       },
       indexes = {
           @Index(name = "idx_tenant_id", columnList = "tenant_id"),
           @Index(name = "idx_event_date", columnList = "event_date"),
           @Index(name = "idx_status", columnList = "status"),
           @Index(name = "idx_orders_deleted_at", columnList = "deleted_at")
       })
@SQLDelete(sql = "UPDATE orders SET deleted_at = NOW(), version = version + 1 WHERE id = ? AND version = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"tenant", "customer", "eventType", "createdByUser", "menuItems", "utilities", "payments"})
@EqualsAndHashCode(callSuper = true, exclude = {"menuItems", "utilities", "payments"})
public class Order extends TenantBaseEntity {

    @Column(name = "order_number", nullable = false, length = 50)
    @NotBlank(message = "Order number is required")
    @Size(max = 50, message = "Order number must not exceed 50 characters")
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @NotNull(message = "Customer is required")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_type_id", nullable = false)
    @NotNull(message = "Event type is required")
    private EventType eventType;

    @Column(name = "event_date", nullable = false)
    @NotNull(message = "Event date is required")
    private LocalDate eventDate;

    @Column(name = "event_time")
    private LocalTime eventTime;

    @Column(name = "venue_name", length = 200)
    @Size(max = 200, message = "Venue name must not exceed 200 characters")
    private String venueName;

    @Column(name = "venue_address", columnDefinition = "TEXT")
    private String venueAddress;

    @Column(name = "guest_count", nullable = false)
    @NotNull(message = "Guest count is required")
    @Min(value = 1, message = "Guest count must be at least 1")
    private Integer guestCount;

    @Column(name = "total_amount", precision = 12, scale = 2)
    @DecimalMin(value = "0.00", message = "Total amount must be non-negative")
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "advance_amount", precision = 12, scale = 2)
    @DecimalMin(value = "0.00", message = "Advance amount must be non-negative")
    @Builder.Default
    private BigDecimal advanceAmount = BigDecimal.ZERO;

    @Column(name = "balance_amount", precision = 12, scale = 2)
    @DecimalMin(value = "0.00", message = "Balance amount must be non-negative")
    @Builder.Default
    private BigDecimal balanceAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull(message = "Status is required")
    @Builder.Default
    private OrderStatus status = OrderStatus.DRAFT;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", insertable = false, updatable = false)
    private User createdByUser;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderMenuItem> menuItems = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderUtility> utilities = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = OrderStatus.DRAFT;
        }
        if (totalAmount == null) {
            totalAmount = BigDecimal.ZERO;
        }
        if (advanceAmount == null) {
            advanceAmount = BigDecimal.ZERO;
        }
        if (balanceAmount == null) {
            balanceAmount = BigDecimal.ZERO;
        }
    }

    /**
     * Helper method to add menu item.
     */
    public void addMenuItem(OrderMenuItem menuItem) {
        menuItems.add(menuItem);
        menuItem.setOrder(this);
    }

    /**
     * Helper method to remove menu item.
     */
    public void removeMenuItem(OrderMenuItem menuItem) {
        menuItems.remove(menuItem);
        menuItem.setOrder(null);
    }

    /**
     * Helper method to add utility.
     */
    public void addUtility(OrderUtility utility) {
        utilities.add(utility);
        utility.setOrder(this);
    }

    /**
     * Helper method to remove utility.
     */
    public void removeUtility(OrderUtility utility) {
        utilities.remove(utility);
        utility.setOrder(null);
    }

    /**
     * Helper method to add payment.
     */
    public void addPayment(Payment payment) {
        payments.add(payment);
        payment.setOrder(this);
    }

    /**
     * Helper method to remove payment.
     */
    public void removePayment(Payment payment) {
        payments.remove(payment);
        payment.setOrder(null);
    }

    /**
     * Calculates and updates balance amount.
     */
    @Transient
    public void recalculateBalance() {
        this.balanceAmount = this.totalAmount.subtract(this.advanceAmount);
    }
}
