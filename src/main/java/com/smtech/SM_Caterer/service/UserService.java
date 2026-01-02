package com.smtech.SM_Caterer.service;

import com.smtech.SM_Caterer.domain.enums.UserRole;
import com.smtech.SM_Caterer.domain.enums.UserStatus;
import com.smtech.SM_Caterer.service.base.BaseService;
import com.smtech.SM_Caterer.service.dto.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for User operations.
 */
public interface UserService extends BaseService<UserDTO, Long> {

    /**
     * Finds user by username.
     * @param username Username
     * @return User if found
     */
    Optional<UserDTO> findByUsername(String username);

    /**
     * Finds user by email.
     * @param email Email address
     * @return User if found
     */
    Optional<UserDTO> findByEmail(String email);

    /**
     * Finds users by tenant ID.
     * @param tenantId Tenant ID
     * @param pageable Pagination parameters
     * @return Page of users
     */
    Page<UserDTO> findByTenantId(Long tenantId, Pageable pageable);

    /**
     * Finds users by role.
     * @param role User role
     * @param pageable Pagination parameters
     * @return Page of users
     */
    Page<UserDTO> findByRole(UserRole role, Pageable pageable);

    /**
     * Finds users by status.
     * @param status User status
     * @return List of users
     */
    List<UserDTO> findByStatus(UserStatus status);

    /**
     * Changes user password.
     * @param userId User ID
     * @param newPassword New password (will be encoded)
     */
    void changePassword(Long userId, String newPassword);

    /**
     * Records successful login.
     * @param userId User ID
     */
    void recordSuccessfulLogin(Long userId);

    /**
     * Records failed login attempt.
     * @param userId User ID
     */
    void recordFailedLogin(Long userId);

    /**
     * Unlocks user account.
     * @param userId User ID
     */
    void unlockAccount(Long userId);
}
