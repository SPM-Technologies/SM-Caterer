package com.smtech.SM_Caterer.API.controller;

import com.smtech.SM_Caterer.API.dto.response.ApiResponse;
import com.smtech.SM_Caterer.API.dto.response.PageResponse;
import com.smtech.SM_Caterer.context.TenantContext;
import com.smtech.SM_Caterer.domain.enums.UserRole;
import com.smtech.SM_Caterer.security.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Base Controller providing common functionality for all controllers.
 *
 * Features:
 * - Get current authenticated user
 * - Get current tenant ID
 * - Role checking utilities
 * - Response building helpers
 * - Pagination helpers
 *
 * @author CloudCaters Team
 * @version 1.0
 * @since Phase 2
 */
public abstract class BaseController {

    /**
     * Gets the current authenticated user.
     *
     * @return CustomUserDetails or null if not authenticated
     */
    protected CustomUserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return (CustomUserDetails) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * Gets the current user ID.
     *
     * @return User ID or null
     */
    protected Long getCurrentUserId() {
        CustomUserDetails user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    /**
     * Gets the current tenant ID from context.
     *
     * @return Tenant ID or null
     */
    protected Long getCurrentTenantId() {
        return TenantContext.getCurrentTenant();
    }

    /**
     * Checks if current user has the specified role.
     *
     * @param role Role to check
     * @return true if user has role
     */
    protected boolean hasRole(UserRole role) {
        CustomUserDetails user = getCurrentUser();
        return user != null && user.hasRole(role);
    }

    /**
     * Checks if current user is super admin.
     *
     * @return true if super admin
     */
    protected boolean isSuperAdmin() {
        CustomUserDetails user = getCurrentUser();
        return user != null && user.isSuperAdmin();
    }

    /**
     * Checks if current user is tenant admin or higher.
     *
     * @return true if tenant admin or super admin
     */
    protected boolean isTenantAdminOrHigher() {
        CustomUserDetails user = getCurrentUser();
        return user != null && user.isTenantAdminOrHigher();
    }

    /**
     * Checks if current user is manager or higher.
     *
     * @return true if manager, tenant admin, or super admin
     */
    protected boolean isManagerOrHigher() {
        CustomUserDetails user = getCurrentUser();
        return user != null && user.isManagerOrHigher();
    }

    /**
     * Builds success response with data.
     *
     * @param data Response data
     * @param <T>  Data type
     * @return ApiResponse
     */
    protected <T> ApiResponse<T> success(T data) {
        return ApiResponse.success(data);
    }

    /**
     * Builds success response with message and data.
     *
     * @param message Success message
     * @param data    Response data
     * @param <T>     Data type
     * @return ApiResponse
     */
    protected <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.success(message, data);
    }

    /**
     * Builds success response with message only.
     *
     * @param message Success message
     * @return ApiResponse
     */
    protected ApiResponse<Void> success(String message) {
        return ApiResponse.success(message);
    }

    /**
     * Builds page response from Spring Data Page.
     *
     * @param page Spring Data Page
     * @param <T>  Content type
     * @return PageResponse
     */
    protected <T> PageResponse<T> toPageResponse(Page<T> page) {
        return PageResponse.from(page);
    }

    /**
     * Creates Pageable from request parameters.
     *
     * @param page    Page number (0-based)
     * @param size    Page size
     * @param sortBy  Sort field
     * @param sortDir Sort direction (asc/desc)
     * @return Pageable
     */
    protected Pageable createPageable(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        return PageRequest.of(page, size, sort);
    }

    /**
     * Creates Pageable with default values.
     *
     * @param page Page number
     * @param size Page size
     * @return Pageable with default sort by id ascending
     */
    protected Pageable createPageable(int page, int size) {
        return createPageable(page, size, "id", "asc");
    }
}
