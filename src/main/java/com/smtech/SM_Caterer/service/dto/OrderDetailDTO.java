package com.smtech.SM_Caterer.service.dto;

import com.smtech.SM_Caterer.domain.enums.OrderStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for order detail view with all related data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetailDTO {

    private Long id;
    private Long tenantId;
    private String orderNumber;

    // Customer info
    private Long customerId;
    private String customerCode;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String customerAddress;

    // Event info
    private Long eventTypeId;
    private String eventTypeCode;
    private String eventTypeName;
    private LocalDate eventDate;
    private LocalTime eventTime;
    private String venueName;
    private String venueAddress;
    private Integer guestCount;

    // Pricing
    private BigDecimal menuSubtotal;
    private BigDecimal utilitySubtotal;
    private BigDecimal totalAmount;
    private BigDecimal discountPercent;
    private BigDecimal discountAmount;
    private BigDecimal taxPercent;
    private BigDecimal taxAmount;
    private BigDecimal grandTotal;
    private BigDecimal advanceAmount;
    private BigDecimal balanceAmount;

    // Status & workflow
    private OrderStatus status;
    private String notes;

    // Audit info
    private Long createdBy;
    private String createdByName;
    private LocalDateTime createdAt;

    private LocalDateTime submittedAt;
    private Long submittedBy;
    private String submittedByName;

    private LocalDateTime approvedAt;
    private Long approvedBy;
    private String approvedByName;

    private LocalDateTime cancelledAt;
    private Long cancelledBy;
    private String cancelledByName;
    private String cancellationReason;

    private LocalDateTime completedAt;
    private Long completedBy;
    private String completedByName;

    // Line items
    @Builder.Default
    private List<OrderMenuItemDTO> menuItems = new ArrayList<>();

    @Builder.Default
    private List<OrderUtilityDTO> utilities = new ArrayList<>();

    @Builder.Default
    private List<PaymentDTO> payments = new ArrayList<>();

    // Computed fields
    private BigDecimal totalPaid;

    // ===== Helper Methods =====

    /**
     * Check if order can be edited.
     */
    public boolean isEditable() {
        return status != null && status.isEditable();
    }

    /**
     * Check if order can be cancelled.
     */
    public boolean isCancellable() {
        return status != null && status.isCancellable();
    }

    /**
     * Get next valid statuses.
     */
    public OrderStatus[] getNextStatuses() {
        return status != null ? status.getNextStatuses() : new OrderStatus[]{};
    }

    /**
     * Check if order is fully paid.
     */
    public boolean isFullyPaid() {
        return balanceAmount != null && balanceAmount.compareTo(BigDecimal.ZERO) <= 0;
    }

    /**
     * Get status badge class.
     */
    public String getStatusBadgeClass() {
        return status != null ? status.getBadgeClass() : "bg-secondary";
    }

    /**
     * Get status display name.
     */
    public String getStatusDisplayName() {
        return status != null ? status.getDisplayName() : "Unknown";
    }
}
