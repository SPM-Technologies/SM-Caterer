package com.smtech.SM_Caterer.API.controller;

import com.smtech.SM_Caterer.API.dto.response.ApiResponse;
import com.smtech.SM_Caterer.API.dto.response.PageResponse;
import com.smtech.SM_Caterer.exception.ResourceNotFoundException;
import com.smtech.SM_Caterer.service.EventTypeService;
import com.smtech.SM_Caterer.service.dto.EventTypeDTO;
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
@RequestMapping("/api/v1/event-types")
@RequiredArgsConstructor
@Tag(name = "Event Type Management", description = "Event Type CRUD operations")
@PreAuthorize("isAuthenticated()")
public class EventTypeController extends BaseController {

    private final EventTypeService eventTypeService;

    @GetMapping
    @Operation(summary = "Get all event types")
    public ResponseEntity<ApiResponse<PageResponse<EventTypeDTO>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "eventCode") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Page<EventTypeDTO> eventTypes = eventTypeService.findAll(createPageable(page, size, sortBy, sortDir));
        return ResponseEntity.ok(success(toPageResponse(eventTypes)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get event type by ID")
    public ResponseEntity<ApiResponse<EventTypeDTO>> getById(@PathVariable Long id) {
        EventTypeDTO eventType = eventTypeService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EventType", "id", id));
        return ResponseEntity.ok(success(eventType));
    }

    @GetMapping("/tenant/{tenantId}")
    @Operation(summary = "Get event types by tenant")
    public ResponseEntity<ApiResponse<List<EventTypeDTO>>> getByTenant(@PathVariable Long tenantId) {
        return ResponseEntity.ok(success(eventTypeService.findByTenantId(tenantId)));
    }

    @PostMapping
    @Operation(summary = "Create new event type")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<EventTypeDTO>> create(@Valid @RequestBody EventTypeDTO dto) {
        log.info("Creating new event type: {}", dto.getEventTypeCode());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(success("Event type created successfully", eventTypeService.create(dto)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update event type")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<EventTypeDTO>> update(@PathVariable Long id, @Valid @RequestBody EventTypeDTO dto) {
        return ResponseEntity.ok(success("Event type updated successfully", eventTypeService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete event type")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        eventTypeService.delete(id);
        return ResponseEntity.ok(success("Event type deleted successfully"));
    }
}
