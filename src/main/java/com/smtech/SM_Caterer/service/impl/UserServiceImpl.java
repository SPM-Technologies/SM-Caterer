package com.smtech.SM_Caterer.service.impl;

import com.smtech.SM_Caterer.domain.entity.Tenant;
import com.smtech.SM_Caterer.domain.entity.User;
import com.smtech.SM_Caterer.domain.enums.UserRole;
import com.smtech.SM_Caterer.domain.enums.UserStatus;
import com.smtech.SM_Caterer.domain.repository.TenantRepository;
import com.smtech.SM_Caterer.domain.repository.UserRepository;
import com.smtech.SM_Caterer.exception.DuplicateResourceException;
import com.smtech.SM_Caterer.exception.ResourceNotFoundException;
import com.smtech.SM_Caterer.service.UserService;
import com.smtech.SM_Caterer.service.base.BaseServiceImpl;
import com.smtech.SM_Caterer.service.dto.UserDTO;
import com.smtech.SM_Caterer.service.mapper.EntityMapper;
import com.smtech.SM_Caterer.service.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service implementation for User operations.
 *
 * SECURITY:
 * - All passwords are BCrypt encoded before saving
 * - Password field is never returned in DTOs
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl extends BaseServiceImpl<User, UserDTO, Long>
        implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    protected JpaRepository<User, Long> getRepository() {
        return userRepository;
    }

    @Override
    protected EntityMapper<UserDTO, User> getMapper() {
        return userMapper;
    }

    @Override
    protected String getEntityName() {
        return "User";
    }

    @Override
    @Transactional
    public UserDTO create(UserDTO dto) {
        log.debug("Creating new user: {}", dto.getUsername());

        // Validate unique constraints
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new DuplicateResourceException("User", "username", dto.getUsername());
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("User", "email", dto.getEmail());
        }

        // Map DTO to entity
        User user = userMapper.toEntity(dto);

        // Set tenant reference
        if (dto.getTenantId() != null) {
            Tenant tenant = tenantRepository.findById(dto.getTenantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", dto.getTenantId()));
            user.setTenant(tenant);
        }

        // Encode password
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            user.setPasswordChangedAt(LocalDateTime.now());
        }

        User savedUser = userRepository.save(user);
        log.info("User created: {} (ID: {})", savedUser.getUsername(), savedUser.getId());

        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    public UserDTO update(Long id, UserDTO dto) {
        log.debug("Updating user ID: {}", id);

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Check username uniqueness (if changed)
        if (!existingUser.getUsername().equals(dto.getUsername()) &&
            userRepository.existsByUsername(dto.getUsername())) {
            throw new DuplicateResourceException("User", "username", dto.getUsername());
        }

        // Check email uniqueness (if changed)
        if (!existingUser.getEmail().equals(dto.getEmail()) &&
            userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("User", "email", dto.getEmail());
        }

        // Update fields
        existingUser.setUsername(dto.getUsername());
        existingUser.setEmail(dto.getEmail());
        existingUser.setFirstName(dto.getFirstName());
        existingUser.setLastName(dto.getLastName());
        existingUser.setPhone(dto.getPhone());
        existingUser.setRole(dto.getRole());
        existingUser.setStatus(dto.getStatus());
        existingUser.setLanguagePreference(dto.getLanguagePreference());

        // Update password only if provided
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(dto.getPassword()));
            existingUser.setPasswordChangedAt(LocalDateTime.now());
        }

        // Update tenant if changed
        if (dto.getTenantId() != null &&
            (existingUser.getTenant() == null || !existingUser.getTenant().getId().equals(dto.getTenantId()))) {
            Tenant tenant = tenantRepository.findById(dto.getTenantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", dto.getTenantId()));
            existingUser.setTenant(tenant);
        }

        User updatedUser = userRepository.save(existingUser);
        log.info("User updated: {} (ID: {})", updatedUser.getUsername(), updatedUser.getId());

        return userMapper.toDto(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDTO> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(userMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDTO> findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> findByTenantId(Long tenantId, Pageable pageable) {
        return userRepository.findByTenantId(tenantId, pageable)
                .map(userMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> findByRole(UserRole role, Pageable pageable) {
        return userRepository.findByRole(role, pageable)
                .map(userMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> findByStatus(UserStatus status) {
        return userMapper.toDto(userRepository.findByStatus(status));
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String newPassword) {
        log.debug("Changing password for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("Password changed for user: {}", user.getUsername());
    }

    @Override
    @Transactional
    public void recordSuccessfulLogin(Long userId) {
        log.debug("Recording successful login for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setLastLogin(LocalDateTime.now());
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void recordFailedLogin(Long userId) {
        log.debug("Recording failed login for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);

        // Lock account after 5 failed attempts
        if (user.getFailedLoginAttempts() >= 5) {
            user.setStatus(UserStatus.LOCKED);
            log.warn("User account locked due to failed login attempts: {}", user.getUsername());
        }

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void unlockAccount(Long userId) {
        log.debug("Unlocking user account ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setStatus(UserStatus.ACTIVE);
        user.setFailedLoginAttempts(0);
        userRepository.save(user);

        log.info("User account unlocked: {}", user.getUsername());
    }
}
