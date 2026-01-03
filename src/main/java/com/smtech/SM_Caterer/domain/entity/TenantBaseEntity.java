package com.smtech.SM_Caterer.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

/**
 * Base entity for all tenant-specific entities.
 *
 * Provides:
 * - Automatic tenant reference
 * - Tenant isolation support via Hibernate Filter
 *
 * All tenant-specific entities MUST extend this class.
 *
 * Usage:
 * - MaterialGroup, Unit, Material, Menu, Customer, etc.
 *
 * Not used by:
 * - Tenant entity itself
 * - Translation entities (they reference parent, not tenant directly)
 *
 * Phase 2: Added Hibernate Filter for automatic tenant isolation.
 * The filter is enabled in TenantContextFilter for each request.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@MappedSuperclass
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = Long.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
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
