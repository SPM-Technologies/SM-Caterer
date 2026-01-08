package com.smtech.SM_Caterer.domain.entity;

import com.smtech.SM_Caterer.domain.enums.TenantStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDate;

/**
 * Tenant entity representing a catering business in the system.
 * Each tenant has isolated data and users.
 *
 * Security:
 * - Soft delete enabled (SQLDelete)
 * - Only active tenants shown by default (Where clause)
 *
 * Validation:
 * - Unique tenant code per system
 * - Valid email format
 * - Valid Indian phone (10 digits)
 * - Valid GSTIN/PAN format
 */
@Entity
@Table(name = "tenants",
       indexes = {
           @Index(name = "idx_tenant_code", columnList = "tenant_code"),
           @Index(name = "idx_status", columnList = "status"),
           @Index(name = "idx_tenants_deleted_at", columnList = "deleted_at")
       })
@SQLDelete(sql = "UPDATE tenants SET deleted_at = NOW(), version = version + 1 WHERE id = ? AND version = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {})
@EqualsAndHashCode(callSuper = true)
public class Tenant extends BaseEntity {

    @Column(name = "tenant_code", nullable = false, unique = true, length = 50)
    @NotBlank(message = "Tenant code is required")
    @Size(max = 50, message = "Tenant code must not exceed 50 characters")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Tenant code must be uppercase alphanumeric with underscores")
    private String tenantCode;

    @Column(name = "business_name", nullable = false, length = 200)
    @NotBlank(message = "Business name is required")
    @Size(max = 200, message = "Business name must not exceed 200 characters")
    private String businessName;

    @Column(name = "contact_person", length = 100)
    @Size(max = 100, message = "Contact person must not exceed 100 characters")
    private String contactPerson;

    @Column(name = "email", unique = true, length = 100)
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Column(name = "phone", length = 20)
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
    private String phone;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "city", length = 100)
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Column(name = "state", length = 100)
    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;

    @Column(name = "pincode", length = 10)
    @Pattern(regexp = "^[0-9]{6}$", message = "Pincode must be 6 digits")
    private String pincode;

    @Column(name = "gstin", length = 20)
    @Pattern(regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$",
             message = "Invalid GSTIN format")
    private String gstin;

    @Column(name = "pan", length = 10)
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$",
             message = "Invalid PAN format")
    private String pan;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull(message = "Status is required")
    @Builder.Default
    private TenantStatus status = TenantStatus.ACTIVE;

    @Column(name = "subscription_start_date")
    private LocalDate subscriptionStartDate;

    @Column(name = "subscription_end_date")
    private LocalDate subscriptionEndDate;

    // ========================================
    // FEATURE TOGGLES
    // ========================================

    @Column(name = "payment_enabled")
    @Builder.Default
    private Boolean paymentEnabled = true;

    @Column(name = "email_enabled")
    @Builder.Default
    private Boolean emailEnabled = false;

    // ========================================
    // EMAIL CONFIGURATION
    // ========================================

    @Column(name = "smtp_host", length = 255)
    private String smtpHost;

    @Column(name = "smtp_port")
    private Integer smtpPort;

    @Column(name = "smtp_username", length = 255)
    private String smtpUsername;

    @Column(name = "smtp_password", length = 255)
    private String smtpPassword;

    @Column(name = "smtp_from_email", length = 255)
    @Email(message = "Invalid from email address")
    private String smtpFromEmail;

    @Column(name = "smtp_from_name", length = 100)
    private String smtpFromName;

    @Column(name = "smtp_use_tls")
    @Builder.Default
    private Boolean smtpUseTls = true;

    // ========================================
    // UPI CONFIGURATION
    // ========================================

    @Column(name = "default_upi_id", length = 100)
    private String defaultUpiId;

    @Column(name = "upi_payee_name", length = 200)
    private String upiPayeeName;

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = TenantStatus.ACTIVE;
        }
    }

    /**
     * Checks if payment feature is enabled for this tenant.
     */
    @Transient
    public boolean isPaymentFeatureEnabled() {
        return paymentEnabled != null && paymentEnabled;
    }

    /**
     * Checks if email is properly configured.
     */
    @Transient
    public boolean isEmailConfigured() {
        return emailEnabled != null && emailEnabled
            && smtpHost != null && !smtpHost.isBlank()
            && smtpPort != null
            && smtpFromEmail != null && !smtpFromEmail.isBlank();
    }

    /**
     * Checks if UPI is configured.
     */
    @Transient
    public boolean isUpiConfigured() {
        return defaultUpiId != null && !defaultUpiId.isBlank()
            && upiPayeeName != null && !upiPayeeName.isBlank();
    }

    /**
     * Checks if tenant subscription is active.
     * @return true if subscription is valid, false otherwise
     */
    @Transient
    public boolean isSubscriptionActive() {
        if (subscriptionEndDate == null) {
            return true; // No expiry
        }
        return LocalDate.now().isBefore(subscriptionEndDate) ||
               LocalDate.now().isEqual(subscriptionEndDate);
    }
}
