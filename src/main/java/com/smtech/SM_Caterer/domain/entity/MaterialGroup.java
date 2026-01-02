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
 * Material Group entity for categorizing materials.
 * Supports multi-language translations.
 *
 * Examples: Vegetables, Spices, Dairy Products, etc.
 */
@Entity
@Table(name = "material_groups",
       uniqueConstraints = {
           @UniqueConstraint(name = "unique_tenant_group", columnNames = {"tenant_id", "group_code"})
       },
       indexes = {
           @Index(name = "idx_tenant_id", columnList = "tenant_id")
       })
@SQLDelete(sql = "UPDATE material_groups SET deleted_at = NOW(), version = version + 1 WHERE id = ? AND version = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"tenant", "translations"})
@EqualsAndHashCode(callSuper = true, exclude = {"translations"})
public class MaterialGroup extends TenantBaseEntity {

    @Column(name = "group_code", nullable = false, length = 50)
    @NotBlank(message = "Group code is required")
    @Size(max = 50, message = "Group code must not exceed 50 characters")
    private String groupCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private Status status = Status.ACTIVE;

    @OneToMany(mappedBy = "materialGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    private List<MaterialGroupTranslation> translations = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = Status.ACTIVE;
        }
    }

    /**
     * Helper method to add translation.
     */
    public void addTranslation(MaterialGroupTranslation translation) {
        translations.add(translation);
        translation.setMaterialGroup(this);
    }

    /**
     * Helper method to remove translation.
     */
    public void removeTranslation(MaterialGroupTranslation translation) {
        translations.remove(translation);
        translation.setMaterialGroup(null);
    }
}
