package com.smtech.SM_Caterer.API.controller;

import com.smtech.SM_Caterer.API.dto.response.ApiResponse;
import com.smtech.SM_Caterer.API.dto.response.PageResponse;
import com.smtech.SM_Caterer.exception.ResourceNotFoundException;
import com.smtech.SM_Caterer.service.RecipeItemService;
import com.smtech.SM_Caterer.service.dto.RecipeItemDTO;
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
@RequestMapping("/api/v1/recipe-items")
@RequiredArgsConstructor
@Tag(name = "Recipe Item Management", description = "Recipe Item (Menu-Material mapping) CRUD operations")
@PreAuthorize("isAuthenticated()")
public class RecipeItemController extends BaseController {

    private final RecipeItemService recipeItemService;

    @GetMapping
    @Operation(summary = "Get all recipe items")
    public ResponseEntity<ApiResponse<PageResponse<RecipeItemDTO>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Page<RecipeItemDTO> items = recipeItemService.findAll(createPageable(page, size, sortBy, sortDir));
        return ResponseEntity.ok(success(toPageResponse(items)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get recipe item by ID")
    public ResponseEntity<ApiResponse<RecipeItemDTO>> getById(@PathVariable Long id) {
        RecipeItemDTO item = recipeItemService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RecipeItem", "id", id));
        return ResponseEntity.ok(success(item));
    }

    @GetMapping("/menu/{menuId}")
    @Operation(summary = "Get recipe items by menu")
    public ResponseEntity<ApiResponse<List<RecipeItemDTO>>> getByMenu(@PathVariable Long menuId) {
        return ResponseEntity.ok(success(recipeItemService.findByMenuId(menuId)));
    }

    @GetMapping("/material/{materialId}")
    @Operation(summary = "Get recipe items by material")
    public ResponseEntity<ApiResponse<List<RecipeItemDTO>>> getByMaterial(@PathVariable Long materialId) {
        return ResponseEntity.ok(success(recipeItemService.findByMaterialId(materialId)));
    }

    @PostMapping
    @Operation(summary = "Create new recipe item")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<RecipeItemDTO>> create(@Valid @RequestBody RecipeItemDTO dto) {
        log.info("Creating recipe item for menu: {}", dto.getMenuId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(success("Recipe item created successfully", recipeItemService.create(dto)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update recipe item")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<RecipeItemDTO>> update(@PathVariable Long id, @Valid @RequestBody RecipeItemDTO dto) {
        return ResponseEntity.ok(success("Recipe item updated successfully", recipeItemService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete recipe item")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        recipeItemService.delete(id);
        return ResponseEntity.ok(success("Recipe item deleted successfully"));
    }
}
