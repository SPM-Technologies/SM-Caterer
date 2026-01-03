package com.smtech.SM_Caterer.API.controller;

import com.smtech.SM_Caterer.API.dto.request.ChangePasswordRequest;
import com.smtech.SM_Caterer.API.dto.request.ForgotPasswordRequest;
import com.smtech.SM_Caterer.API.dto.request.LoginRequest;
import com.smtech.SM_Caterer.API.dto.request.RefreshTokenRequest;
import com.smtech.SM_Caterer.API.dto.response.ApiResponse;
import com.smtech.SM_Caterer.API.dto.response.AuthResponse;
import com.smtech.SM_Caterer.domain.entity.User;
import com.smtech.SM_Caterer.domain.enums.UserStatus;
import com.smtech.SM_Caterer.domain.repository.UserRepository;
import com.smtech.SM_Caterer.exception.AuthenticationException;
import com.smtech.SM_Caterer.exception.InvalidOperationException;
import com.smtech.SM_Caterer.security.CustomUserDetails;
import com.smtech.SM_Caterer.security.jwt.JwtTokenProvider;
import com.smtech.SM_Caterer.service.dto.UserDTO;
import com.smtech.SM_Caterer.service.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Authentication Controller.
 * Handles login, logout, token refresh, and password management.
 *
 * Endpoints:
 * - POST /api/v1/auth/login - User login
 * - POST /api/v1/auth/refresh - Refresh access token
 * - POST /api/v1/auth/forgot-password - Request password reset
 * - POST /api/v1/auth/logout - User logout
 * - GET /api/v1/auth/me - Get current user info
 * - PUT /api/v1/auth/password - Change password
 *
 * @author CloudCaters Team
 * @version 1.0
 * @since Phase 2
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * User login endpoint.
     *
     * @param loginRequest Login credentials
     * @return JWT tokens and user info
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT tokens")
    @Transactional
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login attempt received");
        log.debug("Login attempt for user: {}", loginRequest.getUsername());

        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            // Update last login and reset failed attempts
            userRepository.findById(userDetails.getId()).ifPresent(user -> {
                user.resetFailedLoginAttempts();
                userRepository.save(user);
            });

            // Generate tokens
            String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
            String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

            // Build response
            UserDTO userDTO = new UserDTO();
            userDTO.setId(userDetails.getId());
            userDTO.setUsername(userDetails.getUsername());
            userDTO.setEmail(userDetails.getEmail());
            userDTO.setFirstName(userDetails.getFirstName());
            userDTO.setLastName(userDetails.getLastName());
            userDTO.setTenantId(userDetails.getTenantId());
            userDTO.setTenantCode(userDetails.getTenantCode());
            userDTO.setRole(userDetails.getRole());
            userDTO.setStatus(userDetails.getStatus());

            AuthResponse authResponse = AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtTokenProvider.getExpirationInSeconds())
                    .user(userDTO)
                    .build();

            log.info("Login successful");
            log.debug("Login successful for user: {}", userDetails.getUsername());
            return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));

        } catch (BadCredentialsException ex) {
            log.warn("Invalid credentials attempt");
            log.debug("Invalid credentials for user: {}", loginRequest.getUsername());

            // Increment failed login attempts
            userRepository.findByUsername(loginRequest.getUsername())
                    .or(() -> userRepository.findByEmail(loginRequest.getUsername()))
                    .ifPresent(user -> {
                        user.incrementFailedLoginAttempts();
                        userRepository.save(user);
                    });

            throw new AuthenticationException("Invalid username or password");

        } catch (LockedException ex) {
            log.warn("Locked account login attempt");
            log.debug("Locked account login attempt for user: {}", loginRequest.getUsername());
            throw new AuthenticationException("Account is locked. Please contact administrator.");

        } catch (DisabledException ex) {
            log.warn("Disabled account login attempt");
            log.debug("Disabled account login attempt for user: {}", loginRequest.getUsername());
            throw new AuthenticationException("Account is disabled. Please contact administrator.");
        }
    }

    /**
     * Refresh access token using refresh token.
     *
     * @param request Refresh token request
     * @return New access token
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Get new access token using refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        log.debug("Token refresh request");

        String refreshToken = request.getRefreshToken();

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new AuthenticationException("Invalid or expired refresh token");
        }

        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new AuthenticationException("Invalid token type. Refresh token required.");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("User not found"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AuthenticationException("Account is not active");
        }

        CustomUserDetails userDetails = CustomUserDetails.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .tenantId(user.getTenant() != null ? user.getTenant().getId() : null)
                .tenantCode(user.getTenant() != null ? user.getTenant().getTenantCode() : null)
                .role(user.getRole())
                .status(user.getStatus())
                .build();

        String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails);

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken) // Return same refresh token
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationInSeconds())
                .build();

        log.debug("Token refreshed for user: {}", user.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Token refreshed", authResponse));
    }

    /**
     * Forgot password endpoint.
     * Initiates password reset process by sending reset link to email.
     *
     * Security Note: Always returns success to prevent email enumeration attacks.
     *
     * @param request Forgot password request containing email
     * @return Success response (always, to prevent email enumeration)
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password", description = "Request password reset link via email")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        log.info("Password reset requested");
        log.debug("Password reset requested for email: {}", request.getEmail());

        // Find user by email (don't reveal if email exists or not)
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            if (user.getStatus() == UserStatus.ACTIVE) {
                // TODO: In production, implement email service to send reset link
                // For now, log the request (in production, generate token and send email)
                log.debug("Password reset token should be sent to user ID: {}", user.getId());

                // Generate a password reset token (placeholder for email service integration)
                // String resetToken = jwtTokenProvider.generatePasswordResetToken(user);
                // emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
            } else {
                log.warn("Password reset requested for inactive account");
                log.debug("Password reset requested for inactive account: {}", request.getEmail());
            }
        });

        // Always return success to prevent email enumeration
        return ResponseEntity.ok(ApiResponse.success(
                "If an account exists with this email, a password reset link will be sent."));
    }

    /**
     * User logout endpoint.
     * Clears security context.
     */
    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Logout current user")
    public ResponseEntity<ApiResponse<Void>> logout() {
        SecurityContextHolder.clearContext();
        log.info("User logged out");
        return ResponseEntity.ok(ApiResponse.success("Logout successful"));
    }

    /**
     * Get current authenticated user info.
     *
     * @return Current user details
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get currently authenticated user info")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationException("Not authenticated");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new AuthenticationException("User not found"));

        UserDTO userDTO = userMapper.toDto(user);

        return ResponseEntity.ok(ApiResponse.success(userDTO));
    }

    /**
     * Change password for current user.
     *
     * @param request Password change request
     * @return Success response
     */
    @PutMapping("/password")
    @Operation(summary = "Change password", description = "Change password for current user")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // Validate password confirmation
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidOperationException("New password and confirmation do not match");
        }

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new AuthenticationException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new AuthenticationException("Current password is incorrect");
        }

        // Check if new password is same as current
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new InvalidOperationException("New password must be different from current password");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("Password changed successfully");
        log.debug("Password changed for user: {} (ID: {})", user.getUsername(), user.getId());
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
    }
}
