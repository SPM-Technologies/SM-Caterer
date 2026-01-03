package com.smtech.SM_Caterer.API.controller;

import com.smtech.SM_Caterer.API.dto.response.ApiResponse;
import com.smtech.SM_Caterer.API.dto.response.PageResponse;
import com.smtech.SM_Caterer.exception.ResourceNotFoundException;
import com.smtech.SM_Caterer.service.UnitService;
import com.smtech.SM_Caterer.service.dto.UnitDTO;
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

@Slf4j
@RestController
@RequestMapping("/api/v1/units")
@RequiredArgsConstructor
@Tag(name = "Unit Management", description = "Unit CRUD operations")
@PreAuthorize("isAuthenticated()")
public class UnitController extends BaseController {

    private final UnitService unitService;

    @GetMapping
    @Operation(summary = "Get all units")
    public ResponseEntity<ApiResponse<PageResponse<UnitDTO>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "unitCode") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Page<UnitDTO> units = unitService.findAll(createPageable(page, size, sortBy, sortDir));
        return ResponseEntity.ok(success(toPageResponse(units)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get unit by ID")
    public ResponseEntity<ApiResponse<UnitDTO>> getById(@PathVariable Long id) {
        UnitDTO unit = unitService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit", "id", id));
        return ResponseEntity.ok(success(unit));
    }

    @GetMapping("/tenant/{tenantId}")
    @Operation(summary = "Get units by tenant")
    public ResponseEntity<ApiResponse<List<UnitDTO>>> getByTenant(@PathVariable Long tenantId) {
        return ResponseEntity.ok(success(unitService.findByTenantId(tenantId)));
    }

    @PostMapping
    @Operation(summary = "Create new unit")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<UnitDTO>> create(@Valid @RequestBody UnitDTO dto) {
        log.info("Creating new unit: {}", dto.getUnitCode());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(success("Unit created successfully", unitService.create(dto)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update unit")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<UnitDTO>> update(@PathVariable Long id, @Valid @RequestBody UnitDTO dto) {
        return ResponseEntity.ok(success("Unit updated successfully", unitService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete unit")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        unitService.delete(id);
        return ResponseEntity.ok(success("Unit deleted successfully"));
    }
}
