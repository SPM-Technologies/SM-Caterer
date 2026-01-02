package com.smtech.SM_Caterer.service.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Base DTO with common fields.
 * All DTOs should extend this class to inherit common audit fields.
 */
@Data
public abstract class BaseDTO {

    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
    private Long version; // For optimistic locking
}
