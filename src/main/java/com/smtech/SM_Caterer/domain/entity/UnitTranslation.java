package com.smtech.SM_Caterer.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.smtech.SM_Caterer.domain.enums.LanguageCode;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Translation entity for Unit.
 * Stores unit name in different languages.
 */
@Entity
@Table(name = "unit_translations",
       uniqueConstraints = {
           @UniqueConstraint(name = "unique_unit_lang", columnNames = {"unit_id", "language_code"})
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"unit"})
@EqualsAndHashCode(exclude = {"unit"})
public class UnitTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    @NotNull(message = "Unit is required")
    @JsonBackReference
    private Unit unit;

    @Enumerated(EnumType.STRING)
    @Column(name = "language_code", nullable = false, length = 2)
    @NotNull(message = "Language code is required")
    private LanguageCode languageCode;

    @Column(name = "unit_name", nullable = false, length = 100)
    @NotBlank(message = "Unit name is required")
    @Size(max = 100, message = "Unit name must not exceed 100 characters")
    private String unitName;

    @Version
    @Column(name = "version")
    private Long version;
}
