package com.smtech.SM_Caterer.domain.entity;

import com.smtech.SM_Caterer.domain.enums.LanguageCode;
import com.smtech.SM_Caterer.domain.enums.UserRole;
import com.smtech.SM_Caterer.domain.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

/**
 * User entity for authentication and authorization.
 *
 * Security:
 * - Password MUST be BCrypt encoded before saving
 * - Email is unique across system
 * - Failed login attempts tracked
 * - Account locking support
 *
 * CRITICAL: Never return password in DTOs or APIs
 *
 * Phase 2: Added Hibernate Filter for automatic tenant isolation.
 * Note: User extends BaseEntity (not TenantBaseEntity) because
 * SUPER_ADMIN users may not have a tenant.
 */
@Entity
@Table(name = "users",
       uniqueConstraints = {
           @UniqueConstraint(name = "unique_user_email", columnNames = "email")
       },
       indexes = {
           @Index(name = "idx_username", columnList = "username"),
           @Index(name = "idx_tenant_id", columnList = "tenant_id"),
           @Index(name = "idx_role", columnList = "role"),
           @Index(name = "idx_status", columnList = "status"),
           @Index(name = "idx_users_deleted_at", columnList = "deleted_at")
       })
@SQLDelete(sql = "UPDATE users SET deleted_at = NOW(), version = version + 1 WHERE id = ? AND version = ?")
@Where(clause = "deleted_at IS NULL")
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = Long.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"password", "tenant"})
@EqualsAndHashCode(callSuper = true, exclude = {"password"})
public class User extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be 3-50 characters")
    @Pattern(regexp = "^[a-z0-9._-]+$", message = "Username must be lowercase alphanumeric with . _ -")
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    /**
     * BCrypt encoded password.
     * CRITICAL: Must be encoded using PasswordEncoder before saving.
     * CRITICAL: Never expose in DTOs or logs.
     */
    @Column(name = "password", nullable = false, length = 255)
    @NotBlank(message = "Password is required")
    private String password;

    @Column(name = "first_name", nullable = false, length = 100)
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    @Column(name = "last_name", length = 100)
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    @Column(name = "phone", length = 20)
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    @NotNull(message = "Role is required")
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull(message = "Status is required")
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "language_preference", length = 2)
    @Builder.Default
    private LanguageCode languagePreference = LanguageCode.en;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    @Column(name = "failed_login_attempts")
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = UserStatus.ACTIVE;
        }
        if (languagePreference == null) {
            languagePreference = LanguageCode.en;
        }
        if (failedLoginAttempts == null) {
            failedLoginAttempts = 0;
        }
    }

    /**
     * Gets full name of user.
     * @return First name + last name
     */
    @Transient
    public String getFullName() {
        return firstName + (lastName != null ? " " + lastName : "");
    }

    /**
     * Increments failed login attempts.
     * Locks account if attempts exceed threshold.
     */
    @Transient
    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 5) {
            this.status = UserStatus.LOCKED;
        }
    }

    /**
     * Resets failed login attempts on successful login.
     */
    @Transient
    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.lastLogin = LocalDateTime.now();
    }
}
