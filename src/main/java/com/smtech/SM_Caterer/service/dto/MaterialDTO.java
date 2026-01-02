package com.smtech.SM_Caterer.service.dto;

import com.smtech.SM_Caterer.domain.enums.Status;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for Material entity.
 * Represents an ingredient or item used in catering.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialDTO extends BaseDTO {

    private Long tenantId;

    @NotNull(message = "Material group is required")
    private Long materialGroupId;
    private String materialGroupCode; // For display

    @NotNull(message = "Unit is required")
    private Long unitId;
    private String unitCode; // For display

    @NotBlank(message = "Material code is required")
    @Size(max = 50, message = "Material code must not exceed 50 characters")
    private String materialCode;

    @DecimalMin(value = "0.00", message = "Cost per unit must be non-negative")
    private BigDecimal costPerUnit;

    @DecimalMin(value = "0.00", message = "Minimum stock must be non-negative")
    private BigDecimal minimumStock;

    @DecimalMin(value = "0.00", message = "Current stock must be non-negative")
    private BigDecimal currentStock;

    private Status status;
}
