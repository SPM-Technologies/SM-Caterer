package com.smtech.SM_Caterer.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Base entity class providing common fields for all entities.
 *
 * Features:
 * - Auto-generated ID
 * - Audit timestamps (created_at, updated_at)
 * - Audit users (created_by, updated_by)
 * - Optimistic locking (version)
 * - Soft delete support (deleted_at)
 *
 * All entities MUST extend this class.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private Long updatedBy;

    /**
     * Optimistic locking version field.
     * Automatically incremented on each update.
     * Prevents lost updates in concurrent transactions.
     */
    @Version
    @Column(name = "version")
    private Long version;

    /**
     * Soft delete timestamp.
     * If not null, record is considered deleted.
     * Use isDeleted() to check status.
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * Checks if this entity is soft-deleted.
     * @return true if deleted, false otherwise
     */
    @Transient
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * Marks this entity as deleted (soft delete).
     * Sets deletedAt to current timestamp.
     */
    @Transient
    public void markAsDeleted() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * Restores a soft-deleted entity.
     * Sets deletedAt to null.
     */
    @Transient
    public void restore() {
        this.deletedAt = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseEntity)) return false;
        BaseEntity that = (BaseEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
