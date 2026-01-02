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
 * DTO for MaterialGroup entity.
 * Represents category of materials (e.g., Vegetables, Spices).
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialGroupDTO extends BaseDTO {

    private Long tenantId;

    @NotBlank(message = "Group code is required")
    @Size(max = 50, message = "Group code must not exceed 50 characters")
    private String groupCode;

    private Status status;
}
