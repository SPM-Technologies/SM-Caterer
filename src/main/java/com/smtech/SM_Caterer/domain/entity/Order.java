package com.smtech.SM_Caterer.domain.entity;

import com.smtech.SM_Caterer.domain.enums.OrderStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order entity representing a catering order/booking.
 * Supports full workflow: DRAFT -> PENDING -> CONFIRMED -> IN_PROGRESS -> COMPLETED
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
           @Index(name = "idx_orders_deleted_at", columnList = "deleted_at"),
           @Index(name = "idx_orders_customer", columnList = "customer_id")
       })
@SQLDelete(sql = "UPDATE orders SET deleted_at = NOW(), version = version + 1 WHERE id = ? AND version = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"tenant", "customer", "eventType", "createdByUser", "submittedByUser",
                     "approvedByUser", "cancelledByUser", "completedByUser",
                     "menuItems", "utilities", "payments"})
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

    // ===== Pricing Fields =====

    @Column(name = "menu_subtotal", precision = 12, scale = 2)
    @DecimalMin(value = "0.00", message = "Menu subtotal must be non-negative")
    @Builder.Default
    private BigDecimal menuSubtotal = BigDecimal.ZERO;

    @Column(name = "utility_subtotal", precision = 12, scale = 2)
    @DecimalMin(value = "0.00", message = "Utility subtotal must be non-negative")
    @Builder.Default
    private BigDecimal utilitySubtotal = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 12, scale = 2)
    @DecimalMin(value = "0.00", message = "Total amount must be non-negative")
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "discount_percent", precision = 5, scale = 2)
    @DecimalMin(value = "0.00", message = "Discount must be non-negative")
    @DecimalMax(value = "100.00", message = "Discount cannot exceed 100%")
    @Builder.Default
    private BigDecimal discountPercent = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 12, scale = 2)
    @DecimalMin(value = "0.00", message = "Discount amount must be non-negative")
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "tax_percent", precision = 5, scale = 2)
    @DecimalMin(value = "0.00", message = "Tax must be non-negative")
    @Builder.Default
    private BigDecimal taxPercent = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 12, scale = 2)
    @DecimalMin(value = "0.00", message = "Tax amount must be non-negative")
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "grand_total", precision = 12, scale = 2)
    @DecimalMin(value = "0.00", message = "Grand total must be non-negative")
    @Builder.Default
    private BigDecimal grandTotal = BigDecimal.ZERO;

    @Column(name = "advance_amount", precision = 12, scale = 2)
    @DecimalMin(value = "0.00", message = "Advance amount must be non-negative")
    @Builder.Default
    private BigDecimal advanceAmount = BigDecimal.ZERO;

    @Column(name = "balance_amount", precision = 12, scale = 2)
    @DecimalMin(value = "0.00", message = "Balance amount must be non-negative")
    @Builder.Default
    private BigDecimal balanceAmount = BigDecimal.ZERO;

    // ===== Status & Notes =====

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull(message = "Status is required")
    @Builder.Default
    private OrderStatus status = OrderStatus.DRAFT;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // ===== Workflow Audit Fields =====

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "submitted_by")
    private Long submittedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by", insertable = false, updatable = false)
    private User submittedByUser;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "approved_by")
    private Long approvedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by", insertable = false, updatable = false)
    private User approvedByUser;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancelled_by")
    private Long cancelledBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancelled_by", insertable = false, updatable = false)
    private User cancelledByUser;

    @Column(name = "cancellation_reason", length = 500)
    @Size(max = 500, message = "Cancellation reason must not exceed 500 characters")
    private String cancellationReason;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "completed_by")
    private Long completedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "completed_by", insertable = false, updatable = false)
    private User completedByUser;

    // ===== Relationships =====

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

    // ===== Lifecycle Callbacks =====

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = OrderStatus.DRAFT;
        }
        initializeDefaults();
    }

    private void initializeDefaults() {
        if (menuSubtotal == null) menuSubtotal = BigDecimal.ZERO;
        if (utilitySubtotal == null) utilitySubtotal = BigDecimal.ZERO;
        if (totalAmount == null) totalAmount = BigDecimal.ZERO;
        if (discountPercent == null) discountPercent = BigDecimal.ZERO;
        if (discountAmount == null) discountAmount = BigDecimal.ZERO;
        if (taxPercent == null) taxPercent = BigDecimal.ZERO;
        if (taxAmount == null) taxAmount = BigDecimal.ZERO;
        if (grandTotal == null) grandTotal = BigDecimal.ZERO;
        if (advanceAmount == null) advanceAmount = BigDecimal.ZERO;
        if (balanceAmount == null) balanceAmount = BigDecimal.ZERO;
    }

    // ===== Collection Helper Methods =====

    public void addMenuItem(OrderMenuItem menuItem) {
        menuItems.add(menuItem);
        menuItem.setOrder(this);
    }

    public void removeMenuItem(OrderMenuItem menuItem) {
        menuItems.remove(menuItem);
        menuItem.setOrder(null);
    }

    public void clearMenuItems() {
        menuItems.forEach(item -> item.setOrder(null));
        menuItems.clear();
    }

    public void addUtility(OrderUtility utility) {
        utilities.add(utility);
        utility.setOrder(this);
    }

    public void removeUtility(OrderUtility utility) {
        utilities.remove(utility);
        utility.setOrder(null);
    }

    public void clearUtilities() {
        utilities.forEach(item -> item.setOrder(null));
        utilities.clear();
    }

    public void addPayment(Payment payment) {
        payments.add(payment);
        payment.setOrder(this);
    }

    public void removePayment(Payment payment) {
        payments.remove(payment);
        payment.setOrder(null);
    }

    // ===== Pricing Calculation Methods =====

    /**
     * Recalculate all pricing totals based on line items.
     */
    public void recalculateTotals() {
        // Menu subtotal
        this.menuSubtotal = menuItems.stream()
            .map(OrderMenuItem::getSubtotal)
            .filter(s -> s != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Utility subtotal
        this.utilitySubtotal = utilities.stream()
            .map(OrderUtility::getSubtotal)
            .filter(s -> s != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Total before discount
        this.totalAmount = menuSubtotal.add(utilitySubtotal);

        // Calculate discount
        if (discountPercent != null && discountPercent.compareTo(BigDecimal.ZERO) > 0) {
            this.discountAmount = totalAmount.multiply(discountPercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            this.discountAmount = BigDecimal.ZERO;
        }

        // After discount
        BigDecimal afterDiscount = totalAmount.subtract(discountAmount);

        // Calculate tax
        if (taxPercent != null && taxPercent.compareTo(BigDecimal.ZERO) > 0) {
            this.taxAmount = afterDiscount.multiply(taxPercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            this.taxAmount = BigDecimal.ZERO;
        }

        // Grand total
        this.grandTotal = afterDiscount.add(taxAmount);

        // Update balance
        recalculateBalance();
    }

    /**
     * Calculates and updates balance amount based on payments.
     */
    public void recalculateBalance() {
        BigDecimal totalPaid = payments.stream()
            .map(Payment::getAmount)
            .filter(a -> a != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.advanceAmount = totalPaid;
        this.balanceAmount = this.grandTotal.subtract(totalPaid);

        // Ensure balance is not negative
        if (this.balanceAmount.compareTo(BigDecimal.ZERO) < 0) {
            this.balanceAmount = BigDecimal.ZERO;
        }
    }

    // ===== Workflow Methods =====

    /**
     * Submit order for approval.
     */
    public void submit(Long userId) {
        if (this.status != OrderStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT orders can be submitted");
        }
        this.status = OrderStatus.PENDING;
        this.submittedAt = LocalDateTime.now();
        this.submittedBy = userId;
    }

    /**
     * Approve order.
     */
    public void approve(Long userId) {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("Only PENDING orders can be approved");
        }
        this.status = OrderStatus.CONFIRMED;
        this.approvedAt = LocalDateTime.now();
        this.approvedBy = userId;
    }

    /**
     * Cancel order with reason.
     */
    public void cancel(Long userId, String reason) {
        if (!this.status.isCancellable()) {
            throw new IllegalStateException("Order cannot be cancelled in status: " + this.status);
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Cancellation reason is required");
        }
        this.status = OrderStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancelledBy = userId;
        this.cancellationReason = reason.trim();
    }

    /**
     * Start order execution.
     */
    public void startProgress(Long userId) {
        if (this.status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Only CONFIRMED orders can be started");
        }
        this.status = OrderStatus.IN_PROGRESS;
    }

    /**
     * Complete order.
     */
    public void complete(Long userId) {
        if (this.status != OrderStatus.IN_PROGRESS) {
            throw new IllegalStateException("Only IN_PROGRESS orders can be completed");
        }
        this.status = OrderStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.completedBy = userId;
    }

    // ===== Utility Methods =====

    /**
     * Check if order can be edited.
     */
    @Transient
    public boolean isEditable() {
        return status != null && status.isEditable();
    }

    /**
     * Check if order can be cancelled.
     */
    @Transient
    public boolean isCancellable() {
        return status != null && status.isCancellable();
    }

    /**
     * Get total paid amount from payments.
     */
    @Transient
    public BigDecimal getTotalPaid() {
        return payments.stream()
            .map(Payment::getAmount)
            .filter(a -> a != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Check if order is fully paid.
     */
    @Transient
    public boolean isFullyPaid() {
        return balanceAmount != null && balanceAmount.compareTo(BigDecimal.ZERO) <= 0;
    }
}
