package com.smtech.SM_Caterer.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.smtech.SM_Caterer.domain.enums.LanguageCode;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Translation entity for MaterialGroup.
 * Stores group name in different languages.
 */
@Entity
@Table(name = "material_group_translations",
       uniqueConstraints = {
           @UniqueConstraint(name = "unique_group_lang", columnNames = {"material_group_id", "language_code"})
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"materialGroup"})
@EqualsAndHashCode(exclude = {"materialGroup"})
public class MaterialGroupTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_group_id", nullable = false)
    @NotNull(message = "Material group is required")
    @JsonBackReference
    private MaterialGroup materialGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "language_code", nullable = false, length = 2)
    @NotNull(message = "Language code is required")
    private LanguageCode languageCode;

    @Column(name = "group_name", nullable = false, length = 200)
    @NotBlank(message = "Group name is required")
    @Size(max = 200, message = "Group name must not exceed 200 characters")
    private String groupName;

    @Version
    @Column(name = "version")
    private Long version;
}
