package com.smtech.SM_Caterer.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;

/**
 * Recipe Item entity - mapping between Menu and Material.
 * Defines which materials (ingredients) are needed for a menu item.
 */
@Entity
@Table(name = "recipe_items",
       uniqueConstraints = {
           @UniqueConstraint(name = "unique_menu_material", columnNames = {"menu_id", "material_id"})
       })
@SQLDelete(sql = "UPDATE recipe_items SET deleted_at = NOW(), version = version + 1 WHERE id = ? AND version = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"menu", "material"})
@EqualsAndHashCode(callSuper = true)
public class RecipeItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    @NotNull(message = "Menu is required")
    private Menu menu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    @NotNull(message = "Material is required")
    private Material material;

    @Column(name = "quantity_required", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Quantity required is required")
    @DecimalMin(value = "0.01", message = "Quantity required must be greater than 0")
    private BigDecimal quantityRequired;

    @PrePersist
    protected void onCreate() {
        if (quantityRequired == null) {
            quantityRequired = BigDecimal.ONE;
        }
    }
}
