package com.smtech.SM_Caterer.service.dto;

import com.smtech.SM_Caterer.domain.enums.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * DTO for Unit entity.
 * Represents unit of measurement (e.g., kg, liter, piece).
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnitDTO extends BaseDTO {

    private Long tenantId;

    @NotBlank(message = "Unit code is required")
    @Size(max = 20, message = "Unit code must not exceed 20 characters")
    private String unitCode;

    private Status status;
}
