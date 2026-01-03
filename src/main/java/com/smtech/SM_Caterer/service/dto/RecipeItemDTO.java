package com.smtech.SM_Caterer.service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for RecipeItem entity.
 * Represents materials/ingredients required for a menu item.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeItemDTO extends BaseDTO {

    @NotNull(message = "Menu is required")
    private Long menuId;
    private String menuCode; // For display

    @NotNull(message = "Material is required")
    private Long materialId;
    private String materialCode; // For display

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.01", message = "Quantity must be greater than zero")
    private BigDecimal quantityRequired;

    private String materialName;
    private String unitCode;

    private String notes;
}
