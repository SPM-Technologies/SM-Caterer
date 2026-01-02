package com.smtech.SM_Caterer.service.dto;

import com.smtech.SM_Caterer.domain.enums.Status;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for Menu entity.
 * Represents a menu/dish offered by the caterer.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuDTO extends BaseDTO {

    private Long tenantId;

    @NotBlank(message = "Menu code is required")
    @Size(max = 50, message = "Menu code must not exceed 50 characters")
    private String menuCode;

    @DecimalMin(value = "0.00", message = "Price per person must be non-negative")
    private BigDecimal pricePerPerson;

    @Min(value = 1, message = "Serves count must be at least 1")
    private Integer servesCount;

    private Status status;
}
