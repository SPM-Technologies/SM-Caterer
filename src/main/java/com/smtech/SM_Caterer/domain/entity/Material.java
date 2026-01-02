package com.smtech.SM_Caterer.domain.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.smtech.SM_Caterer.domain.enums.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Material (ingredient/item) entity.
 * Supports multi-language translations and stock tracking.
 */
@Entity
@Table(name = "materials",
       uniqueConstraints = {
           @UniqueConstraint(name = "unique_tenant_material", columnNames = {"tenant_id", "material_code"})
       },
       indexes = {
           @Index(name = "idx_tenant_id", columnList = "tenant_id"),
           @Index(name = "idx_material_group_id", columnList = "material_group_id"),
           @Index(name = "idx_current_stock", columnList = "current_stock"),
           @Index(name = "idx_materials_deleted_at", columnList = "deleted_at")
       })
@SQLDelete(sql = "UPDATE materials SET deleted_at = NOW(), version = version + 1 WHERE id = ? AND version = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"tenant", "materialGroup", "unit", "translations"})
@EqualsAndHashCode(callSuper = true, exclude = {"translations"})
public class Material extends TenantBaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_group_id", nullable = false)
    @NotNull(message = "Material group is required")
    private MaterialGroup materialGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    @NotNull(message = "Unit is required")
    private Unit unit;

    @Column(name = "material_code", nullable = false, length = 50)
    @NotBlank(message = "Material code is required")
    @Size(max = 50, message = "Material code must not exceed 50 characters")
    private String materialCode;

    @Column(name = "cost_per_unit", precision = 10, scale = 2)
    @DecimalMin(value = "0.00", message = "Cost per unit must be non-negative")
    @Builder.Default
    private BigDecimal costPerUnit = BigDecimal.ZERO;

    @Column(name = "minimum_stock", precision = 10, scale = 2)
    @DecimalMin(value = "0.00", message = "Minimum stock must be non-negative")
    @Builder.Default
    private BigDecimal minimumStock = BigDecimal.ZERO;

    @Column(name = "current_stock", precision = 10, scale = 2)
    @DecimalMin(value = "0.00", message = "Current stock must be non-negative")
    @Builder.Default
    private BigDecimal currentStock = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private Status status = Status.ACTIVE;

    @OneToMany(mappedBy = "material", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    private List<MaterialTranslation> translations = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = Status.ACTIVE;
        }
        if (costPerUnit == null) {
            costPerUnit = BigDecimal.ZERO;
        }
        if (minimumStock == null) {
            minimumStock = BigDecimal.ZERO;
        }
        if (currentStock == null) {
            currentStock = BigDecimal.ZERO;
        }
    }

    /**
     * Checks if material is low on stock.
     */
    @Transient
    public boolean isLowStock() {
        return currentStock.compareTo(minimumStock) < 0;
    }

    /**
     * Helper method to add translation.
     */
    public void addTranslation(MaterialTranslation translation) {
        translations.add(translation);
        translation.setMaterial(this);
    }

    /**
     * Helper method to remove translation.
     */
    public void removeTranslation(MaterialTranslation translation) {
        translations.remove(translation);
        translation.setMaterial(null);
    }
}
