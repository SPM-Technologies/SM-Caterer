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
 * Utility entity for additional services/items.
 * Supports multi-language translations.
 *
 * Examples: Tables, Chairs, Decorations, Servers, etc.
 */
@Entity
@Table(name = "utilities",
       uniqueConstraints = {
           @UniqueConstraint(name = "unique_tenant_utility", columnNames = {"tenant_id", "utility_code"})
       },
       indexes = {
           @Index(name = "idx_tenant_id", columnList = "tenant_id"),
           @Index(name = "idx_utilities_deleted_at", columnList = "deleted_at")
       })
@SQLDelete(sql = "UPDATE utilities SET deleted_at = NOW(), version = version + 1 WHERE id = ? AND version = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"tenant", "translations"})
@EqualsAndHashCode(callSuper = true, exclude = {"translations"})
public class Utility extends TenantBaseEntity {

    @Column(name = "utility_code", nullable = false, length = 50)
    @NotBlank(message = "Utility code is required")
    @Size(max = 50, message = "Utility code must not exceed 50 characters")
    private String utilityCode;

    @Column(name = "cost_per_unit", precision = 10, scale = 2)
    @DecimalMin(value = "0.00", message = "Cost per unit must be non-negative")
    @Builder.Default
    private BigDecimal costPerUnit = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private Status status = Status.ACTIVE;

    @OneToMany(mappedBy = "utility", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    private List<UtilityTranslation> translations = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = Status.ACTIVE;
        }
        if (costPerUnit == null) {
            costPerUnit = BigDecimal.ZERO;
        }
    }

    /**
     * Helper method to add translation.
     */
    public void addTranslation(UtilityTranslation translation) {
        translations.add(translation);
        translation.setUtility(this);
    }

    /**
     * Helper method to remove translation.
     */
    public void removeTranslation(UtilityTranslation translation) {
        translations.remove(translation);
        translation.setUtility(null);
    }
}
