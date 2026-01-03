package com.smtech.SM_Caterer.API.controller;

import com.smtech.SM_Caterer.API.dto.response.ApiResponse;
import com.smtech.SM_Caterer.API.dto.response.PageResponse;
import com.smtech.SM_Caterer.domain.enums.TenantStatus;
import com.smtech.SM_Caterer.exception.ResourceNotFoundException;
import com.smtech.SM_Caterer.service.TenantService;
import com.smtech.SM_Caterer.service.dto.TenantDTO;
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
 * REST Controller for Tenant management.
 * Only SUPER_ADMIN can access tenant management endpoints.
 *
 * @author CloudCaters Team
 * @version 1.0
 * @since Phase 2
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
@Tag(name = "Tenant Management", description = "Tenant CRUD operations (Super Admin only)")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class TenantController extends BaseController {

    private final TenantService tenantService;

    @GetMapping
    @Operation(summary = "Get all tenants", description = "Get paginated list of all tenants")
    public ResponseEntity<ApiResponse<PageResponse<TenantDTO>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Page<TenantDTO> tenants = tenantService.findAll(createPageable(page, size, sortBy, sortDir));
        return ResponseEntity.ok(success(toPageResponse(tenants)));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active tenants")
    public ResponseEntity<ApiResponse<PageResponse<TenantDTO>>> getAllActive(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Page<TenantDTO> tenants = tenantService.findAllActive(createPageable(page, size, sortBy, sortDir));
        return ResponseEntity.ok(success(toPageResponse(tenants)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get tenant by ID")
    public ResponseEntity<ApiResponse<TenantDTO>> getById(@PathVariable Long id) {
        TenantDTO tenant = tenantService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", id));
        return ResponseEntity.ok(success(tenant));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get tenant by code")
    public ResponseEntity<ApiResponse<TenantDTO>> getByCode(@PathVariable String code) {
        TenantDTO tenant = tenantService.findByTenantCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "code", code));
        return ResponseEntity.ok(success(tenant));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get tenants by status")
    public ResponseEntity<ApiResponse<List<TenantDTO>>> getByStatus(@PathVariable TenantStatus status) {
        List<TenantDTO> tenants = tenantService.findByStatus(status);
        return ResponseEntity.ok(success(tenants));
    }

    @PostMapping
    @Operation(summary = "Create new tenant")
    public ResponseEntity<ApiResponse<TenantDTO>> create(@Valid @RequestBody TenantDTO dto) {
        log.info("Creating new tenant: {}", dto.getTenantCode());
        TenantDTO created = tenantService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(success("Tenant created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update tenant")
    public ResponseEntity<ApiResponse<TenantDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody TenantDTO dto) {
        log.info("Updating tenant: {}", id);
        TenantDTO updated = tenantService.update(id, dto);
        return ResponseEntity.ok(success("Tenant updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete tenant (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        log.info("Deleting tenant: {}", id);
        tenantService.delete(id);
        return ResponseEntity.ok(success("Tenant deleted successfully"));
    }
}
