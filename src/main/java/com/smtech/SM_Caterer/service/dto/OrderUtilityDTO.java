package com.smtech.SM_Caterer.service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for OrderUtility entity.
 * Represents utilities/services selected in an order.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderUtilityDTO extends BaseDTO {

    private Long orderId;

    @NotNull(message = "Utility is required")
    private Long utilityId;
    private String utilityName; // For display

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", message = "Price must be non-negative")
    private BigDecimal price;

    @DecimalMin(value = "0.00", message = "Subtotal must be non-negative")
    private BigDecimal subtotal;
}
