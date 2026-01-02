package com.smtech.SM_Caterer.domain.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.smtech.SM_Caterer.domain.enums.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit of measurement entity.
 * Supports multi-language translations.
 *
 * Examples: KG, Liter, Dozen, Piece, etc.
 */
@Entity
@Table(name = "units",
       uniqueConstraints = {
           @UniqueConstraint(name = "unique_tenant_unit", columnNames = {"tenant_id", "unit_code"})
       },
       indexes = {
           @Index(name = "idx_tenant_id", columnList = "tenant_id")
       })
@SQLDelete(sql = "UPDATE units SET deleted_at = NOW(), version = version + 1 WHERE id = ? AND version = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"tenant", "translations"})
@EqualsAndHashCode(callSuper = true, exclude = {"translations"})
public class Unit extends TenantBaseEntity {

    @Column(name = "unit_code", nullable = false, length = 20)
    @NotBlank(message = "Unit code is required")
    @Size(max = 20, message = "Unit code must not exceed 20 characters")
    private String unitCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private Status status = Status.ACTIVE;

    @OneToMany(mappedBy = "unit", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    private List<UnitTranslation> translations = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = Status.ACTIVE;
        }
    }

    /**
     * Helper method to add translation.
     */
    public void addTranslation(UnitTranslation translation) {
        translations.add(translation);
        translation.setUnit(this);
    }

    /**
     * Helper method to remove translation.
     */
    public void removeTranslation(UnitTranslation translation) {
        translations.remove(translation);
        translation.setUnit(null);
    }
}
