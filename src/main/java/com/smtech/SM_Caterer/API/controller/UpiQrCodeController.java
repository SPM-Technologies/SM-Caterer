package com.smtech.SM_Caterer.API.controller;

import com.smtech.SM_Caterer.API.dto.response.ApiResponse;
import com.smtech.SM_Caterer.API.dto.response.PageResponse;
import com.smtech.SM_Caterer.exception.ResourceNotFoundException;
import com.smtech.SM_Caterer.service.UpiQrCodeService;
import com.smtech.SM_Caterer.service.dto.UpiQrCodeDTO;
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
@RequestMapping("/api/v1/upi-qr-codes")
@RequiredArgsConstructor
@Tag(name = "UPI QR Code Management", description = "UPI QR Code generation and management")
@PreAuthorize("isAuthenticated()")
public class UpiQrCodeController extends BaseController {

    private final UpiQrCodeService upiQrCodeService;

    @GetMapping
    @Operation(summary = "Get all UPI QR codes")
    public ResponseEntity<ApiResponse<PageResponse<UpiQrCodeDTO>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Page<UpiQrCodeDTO> codes = upiQrCodeService.findAll(createPageable(page, size, sortBy, sortDir));
        return ResponseEntity.ok(success(toPageResponse(codes)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get UPI QR code by ID")
    public ResponseEntity<ApiResponse<UpiQrCodeDTO>> getById(@PathVariable Long id) {
        UpiQrCodeDTO code = upiQrCodeService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UpiQrCode", "id", id));
        return ResponseEntity.ok(success(code));
    }

    @GetMapping("/tenant/{tenantId}")
    @Operation(summary = "Get QR codes by tenant")
    public ResponseEntity<ApiResponse<List<UpiQrCodeDTO>>> getByTenant(@PathVariable Long tenantId) {
        return ResponseEntity.ok(success(upiQrCodeService.findByTenantId(tenantId)));
    }

    @PostMapping
    @Operation(summary = "Generate new UPI QR code")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<UpiQrCodeDTO>> create(@Valid @RequestBody UpiQrCodeDTO dto) {
        log.info("Generating UPI QR code");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(success("QR code generated successfully", upiQrCodeService.create(dto)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update UPI QR code")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<UpiQrCodeDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpiQrCodeDTO dto) {
        return ResponseEntity.ok(success("QR code updated successfully", upiQrCodeService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete UPI QR code")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        upiQrCodeService.delete(id);
        return ResponseEntity.ok(success("QR code deleted successfully"));
    }
}
