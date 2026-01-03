package com.smtech.SM_Caterer.API.controller;

import com.smtech.SM_Caterer.API.dto.response.ApiResponse;
import com.smtech.SM_Caterer.API.dto.response.PageResponse;
import com.smtech.SM_Caterer.domain.enums.PaymentStatus;
import com.smtech.SM_Caterer.exception.ResourceNotFoundException;
import com.smtech.SM_Caterer.service.PaymentService;
import com.smtech.SM_Caterer.service.dto.PaymentDTO;
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
 * REST Controller for Payment management.
 *
 * @author CloudCaters Team
 * @version 1.0
 * @since Phase 2
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Management", description = "Payment CRUD operations")
@PreAuthorize("isAuthenticated()")
public class PaymentController extends BaseController {

    private final PaymentService paymentService;

    @GetMapping
    @Operation(summary = "Get all payments")
    public ResponseEntity<ApiResponse<PageResponse<PaymentDTO>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Page<PaymentDTO> payments = paymentService.findAll(createPageable(page, size, sortBy, sortDir));
        return ResponseEntity.ok(success(toPageResponse(payments)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID")
    public ResponseEntity<ApiResponse<PaymentDTO>> getById(@PathVariable Long id) {
        PaymentDTO payment = paymentService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));
        return ResponseEntity.ok(success(payment));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payments by order")
    public ResponseEntity<ApiResponse<List<PaymentDTO>>> getByOrder(@PathVariable Long orderId) {
        List<PaymentDTO> payments = paymentService.findByOrderId(orderId);
        return ResponseEntity.ok(success(payments));
    }

    @GetMapping("/tenant/{tenantId}")
    @Operation(summary = "Get payments by tenant")
    public ResponseEntity<ApiResponse<List<PaymentDTO>>> getByTenant(@PathVariable Long tenantId) {
        List<PaymentDTO> payments = paymentService.findByTenantId(tenantId);
        return ResponseEntity.ok(success(payments));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get payments by status")
    public ResponseEntity<ApiResponse<List<PaymentDTO>>> getByStatus(@PathVariable PaymentStatus status) {
        List<PaymentDTO> payments = paymentService.findByStatus(status);
        return ResponseEntity.ok(success(payments));
    }

    @PostMapping
    @Operation(summary = "Record new payment")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<PaymentDTO>> create(@Valid @RequestBody PaymentDTO dto) {
        log.info("Recording new payment for order: {}", dto.getOrderId());
        PaymentDTO created = paymentService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(success("Payment recorded successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update payment")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<PaymentDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody PaymentDTO dto) {
        log.info("Updating payment: {}", id);
        PaymentDTO updated = paymentService.update(id, dto);
        return ResponseEntity.ok(success("Payment updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete payment")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        log.info("Deleting payment: {}", id);
        paymentService.delete(id);
        return ResponseEntity.ok(success("Payment deleted successfully"));
    }
}
