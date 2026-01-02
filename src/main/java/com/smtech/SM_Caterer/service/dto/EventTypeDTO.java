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
 * DTO for EventType entity.
 * Represents type of event (e.g., Wedding, Birthday, Corporate).
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventTypeDTO extends BaseDTO {

    private Long tenantId;

    @NotBlank(message = "Event type code is required")
    @Size(max = 50, message = "Event type code must not exceed 50 characters")
    private String eventTypeCode;

    private Status status;
}
