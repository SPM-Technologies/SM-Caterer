package com.smtech.SM_Caterer.web.controller;

import com.smtech.SM_Caterer.domain.entity.EventType;
import com.smtech.SM_Caterer.domain.entity.Tenant;
import com.smtech.SM_Caterer.domain.enums.Status;
import com.smtech.SM_Caterer.domain.repository.EventTypeRepository;
import com.smtech.SM_Caterer.domain.repository.TenantRepository;
import com.smtech.SM_Caterer.security.CustomUserDetails;
import com.smtech.SM_Caterer.service.dto.EventTypeDTO;
import com.smtech.SM_Caterer.service.mapper.EventTypeMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Web Controller for Event Type management.
 */
@Controller
@RequestMapping("/masters/event-types")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER')")
@Slf4j
public class EventTypeWebController {

    private final EventTypeRepository eventTypeRepository;
    private final EventTypeMapper eventTypeMapper;
    private final TenantRepository tenantRepository;

    @GetMapping
    public String list(@AuthenticationPrincipal CustomUserDetails userDetails,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(defaultValue = "eventCode") String sortBy,
                       @RequestParam(defaultValue = "asc") String sortDir,
                       Model model) {
        Long tenantId = userDetails.getTenantId();

        size = Math.min(size, 100);
        Sort sort = sortDir.equalsIgnoreCase("desc")
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<EventType> eventTypePage = eventTypeRepository.findByTenantId(tenantId, pageable);

        model.addAttribute("eventTypes", eventTypePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", eventTypePage.getTotalPages());
        model.addAttribute("totalItems", eventTypePage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);

        log.debug("Listed {} event types for tenant {} (page {}/{})",
            eventTypePage.getNumberOfElements(), tenantId, page + 1, eventTypePage.getTotalPages());

        return "masters/event-types/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("eventType", new EventTypeDTO());
        model.addAttribute("isEdit", false);
        return "masters/event-types/form";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        Long tenantId = userDetails.getTenantId();
        return eventTypeRepository.findByIdAndTenantId(id, tenantId)
                .map(eventType -> {
                    model.addAttribute("eventType", eventTypeMapper.toDto(eventType));
                    model.addAttribute("isEdit", true);
                    return "masters/event-types/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Event Type not found");
                    return "redirect:/masters/event-types";
                });
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("eventType") EventTypeDTO eventTypeDTO,
                         BindingResult bindingResult,
                         @AuthenticationPrincipal CustomUserDetails userDetails,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Long tenantId = userDetails.getTenantId();

        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            return "masters/event-types/form";
        }

        if (eventTypeRepository.existsByTenantIdAndEventCode(tenantId, eventTypeDTO.getEventTypeCode())) {
            bindingResult.rejectValue("eventTypeCode", "duplicate", "Event type code already exists");
            model.addAttribute("isEdit", false);
            return "masters/event-types/form";
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        EventType eventType = eventTypeMapper.toEntity(eventTypeDTO);
        eventType.setTenant(tenant);
        if (eventType.getStatus() == null) {
            eventType.setStatus(Status.ACTIVE);
        }
        eventTypeRepository.save(eventType);

        redirectAttributes.addFlashAttribute("successMessage", "Event Type created successfully");
        return "redirect:/masters/event-types";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("eventType") EventTypeDTO eventTypeDTO,
                         BindingResult bindingResult,
                         @AuthenticationPrincipal CustomUserDetails userDetails,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Long tenantId = userDetails.getTenantId();

        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", true);
            return "masters/event-types/form";
        }

        return eventTypeRepository.findByIdAndTenantId(id, tenantId)
                .map(existingEventType -> {
                    if (!existingEventType.getEventCode().equals(eventTypeDTO.getEventTypeCode()) &&
                            eventTypeRepository.existsByTenantIdAndEventCode(tenantId, eventTypeDTO.getEventTypeCode())) {
                        bindingResult.rejectValue("eventTypeCode", "duplicate", "Event type code already exists");
                        model.addAttribute("isEdit", true);
                        return "masters/event-types/form";
                    }

                    existingEventType.setEventCode(eventTypeDTO.getEventTypeCode());
                    existingEventType.setStatus(eventTypeDTO.getStatus());
                    eventTypeRepository.save(existingEventType);

                    redirectAttributes.addFlashAttribute("successMessage", "Event Type updated successfully");
                    return "redirect:/masters/event-types";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Event Type not found");
                    return "redirect:/masters/event-types";
                });
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal CustomUserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        Long tenantId = userDetails.getTenantId();

        return eventTypeRepository.findByIdAndTenantId(id, tenantId)
                .map(eventType -> {
                    eventTypeRepository.delete(eventType);
                    redirectAttributes.addFlashAttribute("successMessage", "Event Type deleted successfully");
                    return "redirect:/masters/event-types";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Event Type not found");
                    return "redirect:/masters/event-types";
                });
    }
}
