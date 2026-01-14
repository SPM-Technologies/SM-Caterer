package com.smtech.SM_Caterer.domain.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.smtech.SM_Caterer.domain.enums.MenuCategory;
import com.smtech.SM_Caterer.domain.enums.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

/**
 * Menu entity representing a food item/dish.
 * Supports multi-language translations and recipe items.
 */
@Entity
@Table(name = "menus",
       uniqueConstraints = {
           @UniqueConstraint(name = "unique_tenant_menu", columnNames = {"tenant_id", "menu_code"})
       },
       indexes = {
           @Index(name = "idx_tenant_id", columnList = "tenant_id"),
           @Index(name = "idx_category", columnList = "category"),
           @Index(name = "idx_menus_deleted_at", columnList = "deleted_at")
       })
@SQLDelete(sql = "UPDATE menus SET deleted_at = NOW(), version = version + 1 WHERE id = ? AND version = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"tenant", "translations", "recipeItems"})
@EqualsAndHashCode(callSuper = true, exclude = {"translations", "recipeItems"})
public class Menu extends TenantBaseEntity {

    @Column(name = "menu_code", nullable = false, length = 50)
    @NotBlank(message = "Menu code is required")
    @Size(max = 50, message = "Menu code must not exceed 50 characters")
    private String menuCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 10)
    @Builder.Default
    private MenuCategory category = MenuCategory.VEG;

    @Column(name = "serves_count")
    @Min(value = 1, message = "Serves count must be at least 1")
    @Builder.Default
    private Integer servesCount = 1;

    @Column(name = "cost_per_serve", precision = 10, scale = 2)
    @DecimalMin(value = "0.00", message = "Cost per serve must be non-negative")
    @Builder.Default
    private BigDecimal costPerServe = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private Status status = Status.ACTIVE;

    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    private List<MenuTranslation> translations = new ArrayList<>();

    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<RecipeItem> recipeItems = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = Status.ACTIVE;
        }
        if (category == null) {
            category = MenuCategory.VEG;
        }
        if (servesCount == null) {
            servesCount = 1;
        }
        if (costPerServe == null) {
            costPerServe = BigDecimal.ZERO;
        }
    }

    /**
     * Helper method to add translation.
     */
    public void addTranslation(MenuTranslation translation) {
        translations.add(translation);
        translation.setMenu(this);
    }

    /**
     * Helper method to remove translation.
     */
    public void removeTranslation(MenuTranslation translation) {
        translations.remove(translation);
        translation.setMenu(null);
    }

    /**
     * Helper method to add recipe item.
     */
    public void addRecipeItem(RecipeItem recipeItem) {
        recipeItems.add(recipeItem);
        recipeItem.setMenu(this);
    }

    /**
     * Helper method to remove recipe item.
     */
    public void removeRecipeItem(RecipeItem recipeItem) {
        recipeItems.remove(recipeItem);
        recipeItem.setMenu(null);
    }
}
