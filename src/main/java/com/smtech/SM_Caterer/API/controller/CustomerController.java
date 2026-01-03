package com.smtech.SM_Caterer.API.controller;

import com.smtech.SM_Caterer.API.dto.response.ApiResponse;
import com.smtech.SM_Caterer.API.dto.response.PageResponse;
import com.smtech.SM_Caterer.exception.ResourceNotFoundException;
import com.smtech.SM_Caterer.service.CustomerService;
import com.smtech.SM_Caterer.service.dto.CustomerDTO;
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
 * REST Controller for Customer management.
 *
 * @author CloudCaters Team
 * @version 1.0
 * @since Phase 2
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customer Management", description = "Customer CRUD operations")
@PreAuthorize("isAuthenticated()")
public class CustomerController extends BaseController {

    private final CustomerService customerService;

    @GetMapping
    @Operation(summary = "Get all customers")
    public ResponseEntity<ApiResponse<PageResponse<CustomerDTO>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Page<CustomerDTO> customers = customerService.findAll(createPageable(page, size, sortBy, sortDir));
        return ResponseEntity.ok(success(toPageResponse(customers)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID")
    public ResponseEntity<ApiResponse<CustomerDTO>> getById(@PathVariable Long id) {
        CustomerDTO customer = customerService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        return ResponseEntity.ok(success(customer));
    }

    @GetMapping("/phone/{phone}")
    @Operation(summary = "Find customers by phone")
    public ResponseEntity<ApiResponse<List<CustomerDTO>>> findByPhone(@PathVariable String phone) {
        List<CustomerDTO> customers = customerService.findByPhone(phone);
        return ResponseEntity.ok(success(customers));
    }

    @GetMapping("/tenant/{tenantId}")
    @Operation(summary = "Find customers by tenant")
    public ResponseEntity<ApiResponse<List<CustomerDTO>>> findByTenant(@PathVariable Long tenantId) {
        List<CustomerDTO> customers = customerService.findByTenantId(tenantId);
        return ResponseEntity.ok(success(customers));
    }

    @PostMapping
    @Operation(summary = "Create new customer")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<CustomerDTO>> create(@Valid @RequestBody CustomerDTO dto) {
        log.info("Creating new customer: {}", dto.getName());
        CustomerDTO created = customerService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(success("Customer created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update customer")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<CustomerDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody CustomerDTO dto) {
        log.info("Updating customer: {}", id);
        CustomerDTO updated = customerService.update(id, dto);
        return ResponseEntity.ok(success("Customer updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete customer (soft delete)")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        log.info("Deleting customer: {}", id);
        customerService.delete(id);
        return ResponseEntity.ok(success("Customer deleted successfully"));
    }
}
