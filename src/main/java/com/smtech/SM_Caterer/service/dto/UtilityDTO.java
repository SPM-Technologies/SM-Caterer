package com.smtech.SM_Caterer.service.dto;

import com.smtech.SM_Caterer.domain.enums.Status;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for Utility entity.
 * Represents additional items/services (e.g., Tables, Chairs, Decoration).
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UtilityDTO extends BaseDTO {

    private Long tenantId;

    @NotBlank(message = "Utility code is required")
    @Size(max = 50, message = "Utility code must not exceed 50 characters")
    private String utilityCode;

    @DecimalMin(value = "0.00", message = "Price must be non-negative")
    private BigDecimal price;

    private Status status;
}
