package com.smtech.SM_Caterer.API.controller;

import com.smtech.SM_Caterer.API.dto.response.ApiResponse;
import com.smtech.SM_Caterer.API.dto.response.PageResponse;
import com.smtech.SM_Caterer.exception.ResourceNotFoundException;
import com.smtech.SM_Caterer.service.UtilityTranslationService;
import com.smtech.SM_Caterer.service.dto.UtilityTranslationDTO;
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
@RequestMapping("/api/v1/utility-translations")
@RequiredArgsConstructor
@Tag(name = "Utility Translations", description = "Utility Translation CRUD")
@PreAuthorize("isAuthenticated()")
public class UtilityTranslationController extends BaseController {

    private final UtilityTranslationService service;

    @GetMapping
    @Operation(summary = "Get all translations")
    public ResponseEntity<ApiResponse<PageResponse<UtilityTranslationDTO>>> getAll(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy, @RequestParam(defaultValue = "asc") String sortDir) {
        Page<UtilityTranslationDTO> items = service.findAll(createPageable(page, size, sortBy, sortDir));
        return ResponseEntity.ok(success(toPageResponse(items)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get translation by ID")
    public ResponseEntity<ApiResponse<UtilityTranslationDTO>> getById(@PathVariable Long id) {
        UtilityTranslationDTO dto = service.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UtilityTranslation", "id", id));
        return ResponseEntity.ok(success(dto));
    }

    @GetMapping("/utility/{utilityId}")
    @Operation(summary = "Get translations by utility")
    public ResponseEntity<ApiResponse<List<UtilityTranslationDTO>>> getByUtility(@PathVariable Long utilityId) {
        return ResponseEntity.ok(success(service.findByUtilityId(utilityId)));
    }

    @PostMapping
    @Operation(summary = "Create translation")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<UtilityTranslationDTO>> create(@Valid @RequestBody UtilityTranslationDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(success("Translation created", service.create(dto)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update translation")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<UtilityTranslationDTO>> update(@PathVariable Long id, @Valid @RequestBody UtilityTranslationDTO dto) {
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
