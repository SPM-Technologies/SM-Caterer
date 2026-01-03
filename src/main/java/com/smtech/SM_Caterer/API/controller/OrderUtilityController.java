package com.smtech.SM_Caterer.API.controller;

import com.smtech.SM_Caterer.API.dto.response.ApiResponse;
import com.smtech.SM_Caterer.API.dto.response.PageResponse;
import com.smtech.SM_Caterer.exception.ResourceNotFoundException;
import com.smtech.SM_Caterer.service.OrderUtilityService;
import com.smtech.SM_Caterer.service.dto.OrderUtilityDTO;
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
@RequestMapping("/api/v1/order-utilities")
@RequiredArgsConstructor
@Tag(name = "Order Utility Management", description = "Order Utility CRUD operations")
@PreAuthorize("isAuthenticated()")
public class OrderUtilityController extends BaseController {

    private final OrderUtilityService orderUtilityService;

    @GetMapping
    @Operation(summary = "Get all order utilities")
    public ResponseEntity<ApiResponse<PageResponse<OrderUtilityDTO>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Page<OrderUtilityDTO> items = orderUtilityService.findAll(createPageable(page, size, sortBy, sortDir));
        return ResponseEntity.ok(success(toPageResponse(items)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order utility by ID")
    public ResponseEntity<ApiResponse<OrderUtilityDTO>> getById(@PathVariable Long id) {
        OrderUtilityDTO item = orderUtilityService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OrderUtility", "id", id));
        return ResponseEntity.ok(success(item));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get utilities by order")
    public ResponseEntity<ApiResponse<List<OrderUtilityDTO>>> getByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(success(orderUtilityService.findByOrderId(orderId)));
    }

    @PostMapping
    @Operation(summary = "Add utility to order")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<OrderUtilityDTO>> create(@Valid @RequestBody OrderUtilityDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(success("Utility added to order", orderUtilityService.create(dto)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update order utility")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<OrderUtilityDTO>> update(@PathVariable Long id, @Valid @RequestBody OrderUtilityDTO dto) {
        return ResponseEntity.ok(success("Order utility updated", orderUtilityService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove utility from order")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        orderUtilityService.delete(id);
        return ResponseEntity.ok(success("Utility removed from order"));
    }
}
