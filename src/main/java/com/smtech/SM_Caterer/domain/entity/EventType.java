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
 * Event Type entity for categorizing catering events.
 * Supports multi-language translations.
 *
 * Examples: Wedding, Birthday, Corporate Event, etc.
 */
@Entity
@Table(name = "event_types",
       uniqueConstraints = {
           @UniqueConstraint(name = "unique_tenant_event", columnNames = {"tenant_id", "event_code"})
       },
       indexes = {
           @Index(name = "idx_tenant_id", columnList = "tenant_id")
       })
@SQLDelete(sql = "UPDATE event_types SET deleted_at = NOW(), version = version + 1 WHERE id = ? AND version = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"tenant", "translations"})
@EqualsAndHashCode(callSuper = true, exclude = {"translations"})
public class EventType extends TenantBaseEntity {

    @Column(name = "event_code", nullable = false, length = 50)
    @NotBlank(message = "Event code is required")
    @Size(max = 50, message = "Event code must not exceed 50 characters")
    private String eventCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private Status status = Status.ACTIVE;

    @OneToMany(mappedBy = "eventType", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    private List<EventTypeTranslation> translations = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = Status.ACTIVE;
        }
    }

    /**
     * Helper method to add translation.
     */
    public void addTranslation(EventTypeTranslation translation) {
        translations.add(translation);
        translation.setEventType(this);
    }

    /**
     * Helper method to remove translation.
     */
    public void removeTranslation(EventTypeTranslation translation) {
        translations.remove(translation);
        translation.setEventType(null);
    }
}
