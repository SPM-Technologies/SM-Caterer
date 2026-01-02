package com.smtech.SM_Caterer.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.smtech.SM_Caterer.domain.enums.LanguageCode;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Translation entity for EventType.
 * Stores event type name in different languages.
 */
@Entity
@Table(name = "event_type_translations",
       uniqueConstraints = {
           @UniqueConstraint(name = "unique_event_lang", columnNames = {"event_type_id", "language_code"})
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"eventType"})
@EqualsAndHashCode(exclude = {"eventType"})
public class EventTypeTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_type_id", nullable = false)
    @NotNull(message = "Event type is required")
    @JsonBackReference
    private EventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "language_code", nullable = false, length = 2)
    @NotNull(message = "Language code is required")
    private LanguageCode languageCode;

    @Column(name = "event_name", nullable = false, length = 200)
    @NotBlank(message = "Event name is required")
    @Size(max = 200, message = "Event name must not exceed 200 characters")
    private String eventName;

    @Version
    @Column(name = "version")
    private Long version;
}
