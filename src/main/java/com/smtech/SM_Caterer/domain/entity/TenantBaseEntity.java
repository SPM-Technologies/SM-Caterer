package com.smtech.SM_Caterer.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Base entity for all tenant-specific entities.
 *
 * Provides:
 * - Automatic tenant reference
 * - Tenant isolation support
 *
 * All tenant-specific entities MUST extend this class.
 *
 * Usage:
 * - MaterialGroup, Unit, Material, Menu, Customer, etc.
 *
 * Not used by:
 * - Tenant entity itself
 * - Translation entities (they reference parent, not tenant directly)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@MappedSuperclass
public abstract class TenantBaseEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    @NotNull(message = "Tenant is required")
    private Tenant tenant;

    /**
     * Validates that tenant is set before persisting.
     */
    @PrePersist
    protected void validateTenant() {
        if (tenant == null) {
            throw new IllegalStateException(
                "Tenant must be set before persisting " + getClass().getSimpleName()
            );
        }
    }
}
