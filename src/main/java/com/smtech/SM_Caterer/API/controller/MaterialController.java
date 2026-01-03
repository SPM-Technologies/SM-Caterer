package com.smtech.SM_Caterer.API.controller;

import com.smtech.SM_Caterer.API.dto.response.ApiResponse;
import com.smtech.SM_Caterer.API.dto.response.PageResponse;
import com.smtech.SM_Caterer.exception.ResourceNotFoundException;
import com.smtech.SM_Caterer.service.MaterialService;
import com.smtech.SM_Caterer.service.dto.MaterialDTO;
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
 * REST Controller for Material management.
 *
 * @author CloudCaters Team
 * @version 1.0
 * @since Phase 2
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/materials")
@RequiredArgsConstructor
@Tag(name = "Material Management", description = "Material CRUD operations")
@PreAuthorize("isAuthenticated()")
public class MaterialController extends BaseController {

    private final MaterialService materialService;

    @GetMapping
    @Operation(summary = "Get all materials")
    public ResponseEntity<ApiResponse<PageResponse<MaterialDTO>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Page<MaterialDTO> materials = materialService.findAll(createPageable(page, size, sortBy, sortDir));
        return ResponseEntity.ok(success(toPageResponse(materials)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get material by ID")
    public ResponseEntity<ApiResponse<MaterialDTO>> getById(@PathVariable Long id) {
        MaterialDTO material = materialService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material", "id", id));
        return ResponseEntity.ok(success(material));
    }

    @GetMapping("/tenant/{tenantId}")
    @Operation(summary = "Get materials by tenant")
    public ResponseEntity<ApiResponse<List<MaterialDTO>>> getByTenant(@PathVariable Long tenantId) {
        List<MaterialDTO> materials = materialService.findByTenantId(tenantId);
        return ResponseEntity.ok(success(materials));
    }

    @GetMapping("/low-stock/{tenantId}")
    @Operation(summary = "Get materials with low stock for tenant")
    public ResponseEntity<ApiResponse<List<MaterialDTO>>> getLowStock(@PathVariable Long tenantId) {
        List<MaterialDTO> materials = materialService.findLowStockMaterials(tenantId);
        return ResponseEntity.ok(success(materials));
    }

    @PostMapping
    @Operation(summary = "Create new material")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<MaterialDTO>> create(@Valid @RequestBody MaterialDTO dto) {
        log.info("Creating new material: {}", dto.getMaterialCode());
        MaterialDTO created = materialService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(success("Material created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update material")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<MaterialDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody MaterialDTO dto) {
        log.info("Updating material: {}", id);
        MaterialDTO updated = materialService.update(id, dto);
        return ResponseEntity.ok(success("Material updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete material (soft delete)")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        log.info("Deleting material: {}", id);
        materialService.delete(id);
        return ResponseEntity.ok(success("Material deleted successfully"));
    }
}
