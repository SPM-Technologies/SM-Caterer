package com.smtech.SM_Caterer.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.smtech.SM_Caterer.domain.enums.LanguageCode;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Translation entity for Utility.
 * Stores utility name in different languages.
 */
@Entity
@Table(name = "utility_translations",
       uniqueConstraints = {
           @UniqueConstraint(name = "unique_utility_lang", columnNames = {"utility_id", "language_code"})
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"utility"})
@EqualsAndHashCode(exclude = {"utility"})
public class UtilityTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utility_id", nullable = false)
    @NotNull(message = "Utility is required")
    @JsonBackReference
    private Utility utility;

    @Enumerated(EnumType.STRING)
    @Column(name = "language_code", nullable = false, length = 2)
    @NotNull(message = "Language code is required")
    private LanguageCode languageCode;

    @Column(name = "utility_name", nullable = false, length = 200)
    @NotBlank(message = "Utility name is required")
    @Size(max = 200, message = "Utility name must not exceed 200 characters")
    private String utilityName;

    @Version
    @Column(name = "version")
    private Long version;
}
