package com.smtech.SM_Caterer.service.dto;

import com.smtech.SM_Caterer.domain.enums.OrderStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for Order entity.
 * Represents a catering order/booking.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO extends BaseDTO {

    private Long tenantId;

    @NotBlank(message = "Order number is required")
    @Size(max = 50, message = "Order number must not exceed 50 characters")
    private String orderNumber;

    @NotNull(message = "Customer is required")
    private Long customerId;
    private String customerName; // For display

    @NotNull(message = "Event type is required")
    private Long eventTypeId;
    private String eventTypeName; // For display

    @NotNull(message = "Event date is required")
    private LocalDate eventDate;

    private LocalTime eventTime;

    @Size(max = 200, message = "Venue name must not exceed 200 characters")
    private String venueName;

    private String venueAddress;

    @NotNull(message = "Guest count is required")
    @Min(value = 1, message = "Guest count must be at least 1")
    private Integer guestCount;

    @DecimalMin(value = "0.00", message = "Total amount must be non-negative")
    private BigDecimal totalAmount;

    @DecimalMin(value = "0.00", message = "Advance amount must be non-negative")
    private BigDecimal advanceAmount;

    @DecimalMin(value = "0.00", message = "Balance amount must be non-negative")
    private BigDecimal balanceAmount;

    @NotNull(message = "Status is required")
    private OrderStatus status;

    private String notes;

    // Child collections
    @Builder.Default
    private List<OrderMenuItemDTO> menuItems = new ArrayList<>();

    @Builder.Default
    private List<OrderUtilityDTO> utilities = new ArrayList<>();
}
