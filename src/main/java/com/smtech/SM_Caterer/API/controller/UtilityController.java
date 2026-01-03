package com.smtech.SM_Caterer.API.controller;

import com.smtech.SM_Caterer.API.dto.response.ApiResponse;
import com.smtech.SM_Caterer.API.dto.response.PageResponse;
import com.smtech.SM_Caterer.exception.ResourceNotFoundException;
import com.smtech.SM_Caterer.service.UtilityService;
import com.smtech.SM_Caterer.service.dto.UtilityDTO;
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
@RequestMapping("/api/v1/utilities")
@RequiredArgsConstructor
@Tag(name = "Utility Management", description = "Utility CRUD operations")
@PreAuthorize("isAuthenticated()")
public class UtilityController extends BaseController {

    private final UtilityService utilityService;

    @GetMapping
    @Operation(summary = "Get all utilities")
    public ResponseEntity<ApiResponse<PageResponse<UtilityDTO>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "utilityCode") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Page<UtilityDTO> utilities = utilityService.findAll(createPageable(page, size, sortBy, sortDir));
        return ResponseEntity.ok(success(toPageResponse(utilities)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get utility by ID")
    public ResponseEntity<ApiResponse<UtilityDTO>> getById(@PathVariable Long id) {
        UtilityDTO utility = utilityService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utility", "id", id));
        return ResponseEntity.ok(success(utility));
    }

    @GetMapping("/tenant/{tenantId}")
    @Operation(summary = "Get utilities by tenant")
    public ResponseEntity<ApiResponse<List<UtilityDTO>>> getByTenant(@PathVariable Long tenantId) {
        return ResponseEntity.ok(success(utilityService.findByTenantId(tenantId)));
    }

    @PostMapping
    @Operation(summary = "Create new utility")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<UtilityDTO>> create(@Valid @RequestBody UtilityDTO dto) {
        log.info("Creating new utility: {}", dto.getUtilityCode());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(success("Utility created successfully", utilityService.create(dto)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update utility")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<UtilityDTO>> update(@PathVariable Long id, @Valid @RequestBody UtilityDTO dto) {
        return ResponseEntity.ok(success("Utility updated successfully", utilityService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete utility")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        utilityService.delete(id);
        return ResponseEntity.ok(success("Utility deleted successfully"));
    }
}
