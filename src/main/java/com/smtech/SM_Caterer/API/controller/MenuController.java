package com.smtech.SM_Caterer.API.controller;

import com.smtech.SM_Caterer.API.dto.response.ApiResponse;
import com.smtech.SM_Caterer.API.dto.response.PageResponse;
import com.smtech.SM_Caterer.exception.ResourceNotFoundException;
import com.smtech.SM_Caterer.service.MenuService;
import com.smtech.SM_Caterer.service.dto.MenuDTO;
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
 * REST Controller for Menu management.
 *
 * @author CloudCaters Team
 * @version 1.0
 * @since Phase 2
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/menus")
@RequiredArgsConstructor
@Tag(name = "Menu Management", description = "Menu CRUD operations")
@PreAuthorize("isAuthenticated()")
public class MenuController extends BaseController {

    private final MenuService menuService;

    @GetMapping
    @Operation(summary = "Get all menus")
    public ResponseEntity<ApiResponse<PageResponse<MenuDTO>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Page<MenuDTO> menus = menuService.findAll(createPageable(page, size, sortBy, sortDir));
        return ResponseEntity.ok(success(toPageResponse(menus)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get menu by ID")
    public ResponseEntity<ApiResponse<MenuDTO>> getById(@PathVariable Long id) {
        MenuDTO menu = menuService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu", "id", id));
        return ResponseEntity.ok(success(menu));
    }

    @GetMapping("/tenant/{tenantId}")
    @Operation(summary = "Get menus by tenant")
    public ResponseEntity<ApiResponse<List<MenuDTO>>> getByTenant(@PathVariable Long tenantId) {
        List<MenuDTO> menus = menuService.findByTenantId(tenantId);
        return ResponseEntity.ok(success(menus));
    }

    @PostMapping
    @Operation(summary = "Create new menu")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<MenuDTO>> create(@Valid @RequestBody MenuDTO dto) {
        log.info("Creating new menu: {}", dto.getMenuCode());
        MenuDTO created = menuService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(success("Menu created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update menu")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<MenuDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody MenuDTO dto) {
        log.info("Updating menu: {}", id);
        MenuDTO updated = menuService.update(id, dto);
        return ResponseEntity.ok(success("Menu updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete menu (soft delete)")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        log.info("Deleting menu: {}", id);
        menuService.delete(id);
        return ResponseEntity.ok(success("Menu deleted successfully"));
    }
}
