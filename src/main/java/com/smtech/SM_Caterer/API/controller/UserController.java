package com.smtech.SM_Caterer.API.controller;

import com.smtech.SM_Caterer.API.dto.response.ApiResponse;
import com.smtech.SM_Caterer.API.dto.response.PageResponse;
import com.smtech.SM_Caterer.domain.enums.UserRole;
import com.smtech.SM_Caterer.domain.enums.UserStatus;
import com.smtech.SM_Caterer.exception.ResourceNotFoundException;
import com.smtech.SM_Caterer.service.UserService;
import com.smtech.SM_Caterer.service.dto.UserDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for User management.
 *
 * @author CloudCaters Team
 * @version 1.0
 * @since Phase 2
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User CRUD operations")
public class UserController extends BaseController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Get all users")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<PageResponse<UserDTO>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Page<UserDTO> users = userService.findAll(createPageable(page, size, sortBy, sortDir));
        return ResponseEntity.ok(success(toPageResponse(users)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<UserDTO>> getById(@PathVariable Long id) {
        UserDTO user = userService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return ResponseEntity.ok(success(user));
    }

    @GetMapping("/role/{role}")
    @Operation(summary = "Get users by role")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<UserDTO>>> getByRole(
            @PathVariable UserRole role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Page<UserDTO> users = userService.findByRole(role, createPageable(page, size, sortBy, sortDir));
        return ResponseEntity.ok(success(toPageResponse(users)));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get users by status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getByStatus(@PathVariable UserStatus status) {
        List<UserDTO> users = userService.findByStatus(status);
        return ResponseEntity.ok(success(users));
    }

    @GetMapping("/tenant/{tenantId}")
    @Operation(summary = "Get users by tenant")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<UserDTO>>> getByTenant(
            @PathVariable Long tenantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Page<UserDTO> users = userService.findByTenantId(tenantId, createPageable(page, size, sortBy, sortDir));
        return ResponseEntity.ok(success(toPageResponse(users)));
    }

    @PostMapping
    @Operation(summary = "Create new user")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<UserDTO>> create(@Valid @RequestBody UserDTO dto) {
        log.info("Creating new user");
        log.debug("Creating new user: {}", dto.getUsername());
        UserDTO created = userService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(success("User created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<UserDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody UserDTO dto) {
        log.info("Updating user: {}", id);
        UserDTO updated = userService.update(id, dto);
        return ResponseEntity.ok(success("User updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user (soft delete)")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        log.info("Deleting user: {}", id);
        userService.delete(id);
        return ResponseEntity.ok(success("User deleted successfully"));
    }

    @PostMapping("/{id}/unlock")
    @Operation(summary = "Unlock locked user")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> unlock(@PathVariable Long id) {
        log.info("Unlocking user: {}", id);
        userService.unlockAccount(id);
        return ResponseEntity.ok(success("User unlocked successfully"));
    }
}
