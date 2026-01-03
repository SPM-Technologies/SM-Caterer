package com.smtech.SM_Caterer.API.controller;

import com.smtech.SM_Caterer.API.dto.response.ApiResponse;
import com.smtech.SM_Caterer.API.dto.response.PageResponse;
import com.smtech.SM_Caterer.exception.ResourceNotFoundException;
import com.smtech.SM_Caterer.service.OrderMenuItemService;
import com.smtech.SM_Caterer.service.dto.OrderMenuItemDTO;
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
@RequestMapping("/api/v1/order-menu-items")
@RequiredArgsConstructor
@Tag(name = "Order Menu Item Management", description = "Order Menu Item CRUD operations")
@PreAuthorize("isAuthenticated()")
public class OrderMenuItemController extends BaseController {

    private final OrderMenuItemService orderMenuItemService;

    @GetMapping
    @Operation(summary = "Get all order menu items")
    public ResponseEntity<ApiResponse<PageResponse<OrderMenuItemDTO>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Page<OrderMenuItemDTO> items = orderMenuItemService.findAll(createPageable(page, size, sortBy, sortDir));
        return ResponseEntity.ok(success(toPageResponse(items)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order menu item by ID")
    public ResponseEntity<ApiResponse<OrderMenuItemDTO>> getById(@PathVariable Long id) {
        OrderMenuItemDTO item = orderMenuItemService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OrderMenuItem", "id", id));
        return ResponseEntity.ok(success(item));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get menu items by order")
    public ResponseEntity<ApiResponse<List<OrderMenuItemDTO>>> getByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(success(orderMenuItemService.findByOrderId(orderId)));
    }

    @PostMapping
    @Operation(summary = "Add menu item to order")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<OrderMenuItemDTO>> create(@Valid @RequestBody OrderMenuItemDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(success("Menu item added to order", orderMenuItemService.create(dto)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update order menu item")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<OrderMenuItemDTO>> update(@PathVariable Long id, @Valid @RequestBody OrderMenuItemDTO dto) {
        return ResponseEntity.ok(success("Order menu item updated", orderMenuItemService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove menu item from order")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        orderMenuItemService.delete(id);
        return ResponseEntity.ok(success("Menu item removed from order"));
    }
}
