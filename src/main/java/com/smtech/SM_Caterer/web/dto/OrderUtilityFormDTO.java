package com.smtech.SM_Caterer.web.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Form DTO for order utility item in the wizard.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderUtilityFormDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "Utility is required")
    private Long utilityId;

    private String utilityCode;       // Display only
    private String utilityName;       // Display only (from translation)

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Price per unit is required")
    @DecimalMin(value = "0.00", message = "Price must be non-negative")
    private BigDecimal pricePerUnit;

    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    /**
     * Calculate subtotal based on quantity and price.
     */
    public void calculateSubtotal() {
        if (quantity != null && pricePerUnit != null && quantity > 0) {
            this.subtotal = pricePerUnit.multiply(BigDecimal.valueOf(quantity));
        } else {
            this.subtotal = BigDecimal.ZERO;
        }
    }

    /**
     * Validate the utility item.
     */
    public boolean isValid() {
        return utilityId != null &&
               quantity != null && quantity > 0 &&
               pricePerUnit != null && pricePerUnit.compareTo(BigDecimal.ZERO) >= 0;
    }
}
