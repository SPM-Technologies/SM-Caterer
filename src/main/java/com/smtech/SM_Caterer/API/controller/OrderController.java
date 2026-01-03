package com.smtech.SM_Caterer.API.controller;

import com.smtech.SM_Caterer.API.dto.response.ApiResponse;
import com.smtech.SM_Caterer.API.dto.response.PageResponse;
import com.smtech.SM_Caterer.domain.enums.OrderStatus;
import com.smtech.SM_Caterer.exception.ResourceNotFoundException;
import com.smtech.SM_Caterer.service.OrderService;
import com.smtech.SM_Caterer.service.dto.OrderDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for Order management.
 *
 * @author CloudCaters Team
 * @version 1.0
 * @since Phase 2
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order Management", description = "Order CRUD and workflow operations")
@PreAuthorize("isAuthenticated()")
public class OrderController extends BaseController {

    private final OrderService orderService;

    @GetMapping
    @Operation(summary = "Get all orders")
    public ResponseEntity<ApiResponse<PageResponse<OrderDTO>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Page<OrderDTO> orders = orderService.findAll(createPageable(page, size, sortBy, sortDir));
        return ResponseEntity.ok(success(toPageResponse(orders)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<ApiResponse<OrderDTO>> getById(@PathVariable Long id) {
        OrderDTO order = orderService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        return ResponseEntity.ok(success(order));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get orders by status")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getByStatus(@PathVariable OrderStatus status) {
        List<OrderDTO> orders = orderService.findByStatus(status);
        return ResponseEntity.ok(success(orders));
    }

    @GetMapping("/date/{eventDate}")
    @Operation(summary = "Get orders by event date")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getByEventDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate eventDate) {
        List<OrderDTO> orders = orderService.findByEventDate(eventDate);
        return ResponseEntity.ok(success(orders));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get orders by customer")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getByCustomer(@PathVariable Long customerId) {
        List<OrderDTO> orders = orderService.findByCustomerId(customerId);
        return ResponseEntity.ok(success(orders));
    }

    @GetMapping("/tenant/{tenantId}")
    @Operation(summary = "Get orders by tenant")
    public ResponseEntity<ApiResponse<PageResponse<OrderDTO>>> getByTenant(
            @PathVariable Long tenantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Page<OrderDTO> orders = orderService.findByTenantId(tenantId, createPageable(page, size, sortBy, sortDir));
        return ResponseEntity.ok(success(toPageResponse(orders)));
    }

    @PostMapping
    @Operation(summary = "Create new order")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<OrderDTO>> create(@Valid @RequestBody OrderDTO dto) {
        log.info("Creating new order");
        OrderDTO created = orderService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(success("Order created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update order")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<OrderDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody OrderDTO dto) {
        log.info("Updating order: {}", id);
        OrderDTO updated = orderService.update(id, dto);
        return ResponseEntity.ok(success("Order updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete order (soft delete)")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        log.info("Deleting order: {}", id);
        orderService.delete(id);
        return ResponseEntity.ok(success("Order deleted successfully"));
    }
}
