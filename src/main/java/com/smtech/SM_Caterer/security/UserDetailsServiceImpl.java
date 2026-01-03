package com.smtech.SM_Caterer.security;

import com.smtech.SM_Caterer.domain.entity.User;
import com.smtech.SM_Caterer.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UserDetailsService implementation for Spring Security.
 * Loads user from database by username or email.
 *
 * @author CloudCaters Team
 * @version 1.0
 * @since Phase 2
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads user by username or email.
     *
     * @param usernameOrEmail Username or email
     * @return UserDetails
     * @throws UsernameNotFoundException if user not found
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        log.debug("Attempting to load user by username or email: {}", usernameOrEmail);

        // Try to find by username first, then by email
        User user = userRepository.findByUsername(usernameOrEmail)
                .orElseGet(() -> userRepository.findByEmail(usernameOrEmail)
                        .orElseThrow(() -> {
                            log.warn("User not found during authentication");
                            log.debug("User not found with username or email: {}", usernameOrEmail);
                            return new UsernameNotFoundException("User not found");
                        }));

        log.debug("User found: {} (ID: {}, Tenant: {})",
                user.getUsername(), user.getId(),
                user.getTenant() != null ? user.getTenant().getTenantCode() : "N/A");

        return buildUserDetails(user);
    }

    /**
     * Loads user by ID.
     *
     * @param userId User ID
     * @return UserDetails
     * @throws UsernameNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        log.debug("Attempting to load user by ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", userId);
                    return new UsernameNotFoundException("User not found with ID: " + userId);
                });

        return buildUserDetails(user);
    }

    /**
     * Builds CustomUserDetails from User entity.
     *
     * @param user User entity
     * @return CustomUserDetails
     */
    private CustomUserDetails buildUserDetails(User user) {
        return CustomUserDetails.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .password(user.getPassword())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .tenantId(user.getTenant() != null ? user.getTenant().getId() : null)
                .tenantCode(user.getTenant() != null ? user.getTenant().getTenantCode() : null)
                .role(user.getRole())
                .status(user.getStatus())
                .build();
    }
}
