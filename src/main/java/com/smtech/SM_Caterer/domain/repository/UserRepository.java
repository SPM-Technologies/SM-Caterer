package com.smtech.SM_Caterer.domain.repository;

import com.smtech.SM_Caterer.domain.entity.User;
import com.smtech.SM_Caterer.domain.enums.UserRole;
import com.smtech.SM_Caterer.domain.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for User entity.
 *
 * Security:
 * - All queries tenant-aware
 * - Password never returned in queries
 * - Failed login tracking
 */
@Repository
public interface UserRepository extends BaseRepository<User, Long> {

    /**
     * Finds user by username (for authentication).
     * @param username Username
     * @return User if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds user by email.
     * @param email Email address
     * @return User if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds user by username and tenant.
     * @param tenantId Tenant ID
     * @param username Username
     * @return User if found
     */
    Optional<User> findByTenantIdAndUsername(Long tenantId, String username);

    /**
     * Finds user by email and tenant.
     * @param tenantId Tenant ID
     * @param email Email
     * @return User if found
     */
    Optional<User> findByTenantIdAndEmail(Long tenantId, String email);

    /**
     * Finds all users for tenant.
     * @param tenantId Tenant ID
     * @return List of users
     */
    List<User> findByTenantId(Long tenantId);

    /**
     * Finds all users for tenant with pagination.
     * @param tenantId Tenant ID
     * @param pageable Pagination parameters
     * @return Page of users
     */
    Page<User> findByTenantId(Long tenantId, Pageable pageable);

    /**
     * Finds users by tenant and role.
     * @param tenantId Tenant ID
     * @param role User role
     * @return List of users
     */
    List<User> findByTenantIdAndRole(Long tenantId, UserRole role);

    /**
     * Finds users by role with pagination.
     * @param role User role
     * @param pageable Pagination parameters
     * @return Page of users
     */
    Page<User> findByRole(UserRole role, Pageable pageable);

    /**
     * Finds users by tenant and status.
     * @param tenantId Tenant ID
     * @param status User status
     * @return List of users
     */
    List<User> findByTenantIdAndStatus(Long tenantId, UserStatus status);

    /**
     * Finds users by status.
     * @param status User status
     * @return List of users
     */
    List<User> findByStatus(UserStatus status);

    /**
     * Checks if username exists.
     * @param username Username
     * @return true if exists
     */
    boolean existsByUsername(String username);

    /**
     * Checks if email exists.
     * @param email Email
     * @return true if exists
     */
    boolean existsByEmail(String email);

    /**
     * Checks if username exists for tenant.
     * @param tenantId Tenant ID
     * @param username Username
     * @return true if exists
     */
    boolean existsByTenantIdAndUsername(Long tenantId, String username);

    /**
     * Finds locked users (for admin unlock).
     */
    @Query("SELECT u FROM User u WHERE u.tenant.id = :tenantId AND u.status = 'LOCKED'")
    List<User> findLockedUsers(@Param("tenantId") Long tenantId);

    /**
     * Counts users by tenant and role.
     */
    long countByTenantIdAndRole(Long tenantId, UserRole role);

    /**
     * Counts users by tenant.
     */
    long countByTenantId(Long tenantId);
}
