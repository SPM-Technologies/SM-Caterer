package com.smtech.SM_Caterer.web.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Form DTO for order menu item in the wizard.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderMenuItemFormDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "Menu is required")
    private Long menuId;

    private String menuCode;          // Display only
    private String menuName;          // Display only (from translation)
    private String menuCategory;      // Display only (VEG, NON_VEG, VEGAN)

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Price per item is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal pricePerItem;

    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    /**
     * Calculate subtotal based on quantity and price.
     */
    public void calculateSubtotal() {
        if (quantity != null && pricePerItem != null && quantity > 0) {
            this.subtotal = pricePerItem.multiply(BigDecimal.valueOf(quantity));
        } else {
            this.subtotal = BigDecimal.ZERO;
        }
    }

    /**
     * Validate the menu item.
     */
    public boolean isValid() {
        return menuId != null &&
               quantity != null && quantity > 0 &&
               pricePerItem != null && pricePerItem.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Get formatted category display name.
     */
    public String getCategoryDisplayName() {
        if (menuCategory == null) return "";
        return switch (menuCategory) {
            case "VEG" -> "Vegetarian";
            case "NON_VEG" -> "Non-Vegetarian";
            case "VEGAN" -> "Vegan";
            default -> menuCategory;
        };
    }
}
