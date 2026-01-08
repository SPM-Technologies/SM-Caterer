package com.smtech.SM_Caterer.domain.entity;

import com.smtech.SM_Caterer.domain.enums.EmailStatus;
import com.smtech.SM_Caterer.domain.enums.EmailType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

/**
 * EmailLog entity for tracking email sending attempts.
 * Extends TenantBaseEntity for consistent multi-tenant support.
 */
@Entity
@Table(name = "email_logs",
       indexes = {
           @Index(name = "idx_email_logs_tenant_id", columnList = "tenant_id"),
           @Index(name = "idx_email_logs_status", columnList = "status"),
           @Index(name = "idx_email_logs_sent_at", columnList = "sent_at"),
           @Index(name = "idx_email_logs_reference", columnList = "reference_type, reference_id")
       })
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@SQLDelete(sql = "UPDATE email_logs SET deleted_at = NOW(), version = version + 1 WHERE id = ? AND version = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {})
@EqualsAndHashCode(callSuper = true)
public class EmailLog extends TenantBaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "email_type", nullable = false, length = 50)
    @NotNull(message = "Email type is required")
    private EmailType emailType;

    @Column(name = "to_email", nullable = false, length = 255)
    @NotBlank(message = "To email is required")
    @Email(message = "Invalid email address")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String toEmail;

    @Column(name = "to_name", length = 200)
    @Size(max = 200, message = "To name must not exceed 200 characters")
    private String toName;

    @Column(name = "subject", nullable = false, length = 500)
    @NotBlank(message = "Subject is required")
    @Size(max = 500, message = "Subject must not exceed 500 characters")
    private String subject;

    @Column(name = "body", columnDefinition = "LONGTEXT")
    private String body;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "reference_type", length = 50)
    @Size(max = 50, message = "Reference type must not exceed 50 characters")
    private String referenceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull(message = "Status is required")
    @Builder.Default
    private EmailStatus status = EmailStatus.PENDING;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "attachment_path", length = 500)
    @Size(max = 500, message = "Attachment path must not exceed 500 characters")
    private String attachmentPath;

    @PrePersist
    @Override
    protected void validateTenant() {
        super.validateTenant();
        if (status == null) {
            status = EmailStatus.PENDING;
        }
        if (retryCount == null) {
            retryCount = 0;
        }
    }

    /**
     * Marks email as sent.
     */
    public void markAsSent() {
        this.status = EmailStatus.SENT;
        this.sentAt = LocalDateTime.now();
        this.errorMessage = null;
    }

    /**
     * Marks email as failed with error message.
     */
    public void markAsFailed(String errorMessage) {
        this.status = EmailStatus.FAILED;
        this.errorMessage = errorMessage;
        this.retryCount = this.retryCount + 1;
    }

    /**
     * Checks if email can be retried (max 3 attempts).
     */
    @Transient
    public boolean canRetry() {
        return retryCount < 3 && status == EmailStatus.FAILED;
    }
}
