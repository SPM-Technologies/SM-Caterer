package com.smtech.SM_Caterer.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.smtech.SM_Caterer.domain.enums.LanguageCode;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Translation entity for Material.
 * Stores material name and description in different languages.
 */
@Entity
@Table(name = "material_translations",
       uniqueConstraints = {
           @UniqueConstraint(name = "unique_material_lang", columnNames = {"material_id", "language_code"})
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"material"})
@EqualsAndHashCode(exclude = {"material"})
public class MaterialTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    @NotNull(message = "Material is required")
    @JsonBackReference
    private Material material;

    @Enumerated(EnumType.STRING)
    @Column(name = "language_code", nullable = false, length = 2)
    @NotNull(message = "Language code is required")
    private LanguageCode languageCode;

    @Column(name = "material_name", nullable = false, length = 200)
    @NotBlank(message = "Material name is required")
    @Size(max = 200, message = "Material name must not exceed 200 characters")
    private String materialName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Version
    @Column(name = "version")
    private Long version;
}
