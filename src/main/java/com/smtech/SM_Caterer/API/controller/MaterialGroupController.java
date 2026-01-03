package com.smtech.SM_Caterer.API.controller;

import com.smtech.SM_Caterer.API.dto.response.ApiResponse;
import com.smtech.SM_Caterer.API.dto.response.PageResponse;
import com.smtech.SM_Caterer.exception.ResourceNotFoundException;
import com.smtech.SM_Caterer.service.MaterialGroupService;
import com.smtech.SM_Caterer.service.dto.MaterialGroupDTO;
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
 * REST Controller for Material Group management.
 *
 * @author CloudCaters Team
 * @version 1.0
 * @since Phase 2
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/material-groups")
@RequiredArgsConstructor
@Tag(name = "Material Group Management", description = "Material Group CRUD operations")
@PreAuthorize("isAuthenticated()")
public class MaterialGroupController extends BaseController {

    private final MaterialGroupService materialGroupService;

    @GetMapping
    @Operation(summary = "Get all material groups")
    public ResponseEntity<ApiResponse<PageResponse<MaterialGroupDTO>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "groupCode") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Page<MaterialGroupDTO> groups = materialGroupService.findAll(createPageable(page, size, sortBy, sortDir));
        return ResponseEntity.ok(success(toPageResponse(groups)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get material group by ID")
    public ResponseEntity<ApiResponse<MaterialGroupDTO>> getById(@PathVariable Long id) {
        MaterialGroupDTO group = materialGroupService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MaterialGroup", "id", id));
        return ResponseEntity.ok(success(group));
    }

    @GetMapping("/tenant/{tenantId}")
    @Operation(summary = "Get material groups by tenant")
    public ResponseEntity<ApiResponse<List<MaterialGroupDTO>>> getByTenant(@PathVariable Long tenantId) {
        List<MaterialGroupDTO> groups = materialGroupService.findByTenantId(tenantId);
        return ResponseEntity.ok(success(groups));
    }

    @PostMapping
    @Operation(summary = "Create new material group")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<MaterialGroupDTO>> create(@Valid @RequestBody MaterialGroupDTO dto) {
        log.info("Creating new material group: {}", dto.getGroupCode());
        MaterialGroupDTO created = materialGroupService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(success("Material group created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update material group")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<MaterialGroupDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody MaterialGroupDTO dto) {
        log.info("Updating material group: {}", id);
        MaterialGroupDTO updated = materialGroupService.update(id, dto);
        return ResponseEntity.ok(success("Material group updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete material group (soft delete)")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        log.info("Deleting material group: {}", id);
        materialGroupService.delete(id);
        return ResponseEntity.ok(success("Material group deleted successfully"));
    }
}
