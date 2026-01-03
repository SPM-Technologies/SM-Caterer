package com.smtech.SM_Caterer.API.controller;

import com.smtech.SM_Caterer.API.dto.response.ApiResponse;
import com.smtech.SM_Caterer.API.dto.response.PageResponse;
import com.smtech.SM_Caterer.exception.ResourceNotFoundException;
import com.smtech.SM_Caterer.service.UnitTranslationService;
import com.smtech.SM_Caterer.service.dto.UnitTranslationDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/unit-translations")
@RequiredArgsConstructor
@Tag(name = "Unit Translations", description = "Unit Translation CRUD")
@PreAuthorize("isAuthenticated()")
public class UnitTranslationController extends BaseController {

    private final UnitTranslationService service;

    @GetMapping
    @Operation(summary = "Get all translations")
    public ResponseEntity<ApiResponse<PageResponse<UnitTranslationDTO>>> getAll(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy, @RequestParam(defaultValue = "asc") String sortDir) {
        Page<UnitTranslationDTO> items = service.findAll(createPageable(page, size, sortBy, sortDir));
        return ResponseEntity.ok(success(toPageResponse(items)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get translation by ID")
    public ResponseEntity<ApiResponse<UnitTranslationDTO>> getById(@PathVariable Long id) {
        UnitTranslationDTO dto = service.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UnitTranslation", "id", id));
        return ResponseEntity.ok(success(dto));
    }

    @GetMapping("/unit/{unitId}")
    @Operation(summary = "Get translations by unit")
    public ResponseEntity<ApiResponse<List<UnitTranslationDTO>>> getByUnit(@PathVariable Long unitId) {
        return ResponseEntity.ok(success(service.findByUnitId(unitId)));
    }

    @PostMapping
    @Operation(summary = "Create translation")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<UnitTranslationDTO>> create(@Valid @RequestBody UnitTranslationDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(success("Translation created", service.create(dto)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update translation")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<UnitTranslationDTO>> update(@PathVariable Long id, @Valid @RequestBody UnitTranslationDTO dto) {
        return ResponseEntity.ok(success("Translation updated", service.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete translation")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(success("Translation deleted"));
    }
}
