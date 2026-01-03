package com.smtech.SM_Caterer.security;

import com.smtech.SM_Caterer.domain.enums.UserRole;
import com.smtech.SM_Caterer.domain.enums.UserStatus;
import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * Custom UserDetails implementation for Spring Security.
 * Contains user information and tenant context.
 *
 * @author CloudCaters Team
 * @version 1.0
 * @since Phase 2
 */
@Data
@Builder
public class CustomUserDetails implements UserDetails, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private Long tenantId;
    private String tenantCode;
    private UserRole role;
    private UserStatus status;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Return role as authority with ROLE_ prefix
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.LOCKED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }

    /**
     * Gets full name of user.
     *
     * @return First name + last name
     */
    public String getFullName() {
        return firstName + (lastName != null ? " " + lastName : "");
    }

    /**
     * Checks if user has specific role.
     *
     * @param checkRole Role to check
     * @return true if user has role
     */
    public boolean hasRole(UserRole checkRole) {
        return this.role == checkRole;
    }

    /**
     * Checks if user is super admin.
     *
     * @return true if super admin
     */
    public boolean isSuperAdmin() {
        return this.role == UserRole.SUPER_ADMIN;
    }

    /**
     * Checks if user is tenant admin or higher.
     *
     * @return true if tenant admin or super admin
     */
    public boolean isTenantAdminOrHigher() {
        return this.role == UserRole.SUPER_ADMIN || this.role == UserRole.TENANT_ADMIN;
    }

    /**
     * Checks if user is manager or higher.
     *
     * @return true if manager, tenant admin, or super admin
     */
    public boolean isManagerOrHigher() {
        return this.role == UserRole.SUPER_ADMIN ||
               this.role == UserRole.TENANT_ADMIN ||
               this.role == UserRole.MANAGER;
    }
}
