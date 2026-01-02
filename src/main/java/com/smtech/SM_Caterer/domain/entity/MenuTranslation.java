package com.smtech.SM_Caterer.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.smtech.SM_Caterer.domain.enums.LanguageCode;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Translation entity for Menu.
 * Stores menu name and description in different languages.
 */
@Entity
@Table(name = "menu_translations",
       uniqueConstraints = {
           @UniqueConstraint(name = "unique_menu_lang", columnNames = {"menu_id", "language_code"})
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"menu"})
@EqualsAndHashCode(exclude = {"menu"})
public class MenuTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    @NotNull(message = "Menu is required")
    @JsonBackReference
    private Menu menu;

    @Enumerated(EnumType.STRING)
    @Column(name = "language_code", nullable = false, length = 2)
    @NotNull(message = "Language code is required")
    private LanguageCode languageCode;

    @Column(name = "menu_name", nullable = false, length = 200)
    @NotBlank(message = "Menu name is required")
    @Size(max = 200, message = "Menu name must not exceed 200 characters")
    private String menuName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Version
    @Column(name = "version")
    private Long version;
}
