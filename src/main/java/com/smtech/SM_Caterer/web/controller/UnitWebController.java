package com.smtech.SM_Caterer.web.controller;

import com.smtech.SM_Caterer.domain.entity.Tenant;
import com.smtech.SM_Caterer.domain.entity.Unit;
import com.smtech.SM_Caterer.domain.enums.Status;
import com.smtech.SM_Caterer.domain.repository.TenantRepository;
import com.smtech.SM_Caterer.domain.repository.UnitRepository;
import com.smtech.SM_Caterer.security.CustomUserDetails;
import com.smtech.SM_Caterer.service.dto.UnitDTO;
import com.smtech.SM_Caterer.service.mapper.UnitMapper;
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
 * Web Controller for Unit management.
 * Handles CRUD operations for Units via Thymeleaf views.
 */
@Controller
@RequestMapping("/masters/units")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER')")
@Slf4j
public class UnitWebController {

    private final UnitRepository unitRepository;
    private final UnitMapper unitMapper;
    private final TenantRepository tenantRepository;

    private static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * List all units for current tenant with pagination.
     */
    @GetMapping
    public String list(@AuthenticationPrincipal CustomUserDetails userDetails,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(defaultValue = "unitCode") String sortBy,
                       @RequestParam(defaultValue = "asc") String sortDir,
                       Model model) {
        Long tenantId = userDetails.getTenantId();

        size = Math.min(size, 100); // Limit max page size
        Sort sort = sortDir.equalsIgnoreCase("desc")
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Unit> unitPage = unitRepository.findByTenantId(tenantId, pageable);

        model.addAttribute("units", unitPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", unitPage.getTotalPages());
        model.addAttribute("totalItems", unitPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);

        log.debug("Listed {} units for tenant {} (page {}/{})",
            unitPage.getNumberOfElements(), tenantId, page + 1, unitPage.getTotalPages());

        return "masters/units/list";
    }

    /**
     * Show form to create new unit.
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("unit", new UnitDTO());
        model.addAttribute("isEdit", false);
        return "masters/units/form";
    }

    /**
     * Show form to edit existing unit.
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        Long tenantId = userDetails.getTenantId();
        return unitRepository.findByIdAndTenantId(id, tenantId)
                .map(unit -> {
                    model.addAttribute("unit", unitMapper.toDto(unit));
                    model.addAttribute("isEdit", true);
                    return "masters/units/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Unit not found");
                    return "redirect:/masters/units";
                });
    }

    /**
     * Create new unit.
     */
    @PostMapping
    public String create(@Valid @ModelAttribute("unit") UnitDTO unitDTO,
                         BindingResult bindingResult,
                         @AuthenticationPrincipal CustomUserDetails userDetails,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            return "masters/units/form";
        }

        Long tenantId = userDetails.getTenantId();

        // Check for duplicate
        if (unitRepository.existsByTenantIdAndUnitCode(tenantId, unitDTO.getUnitCode())) {
            bindingResult.rejectValue("unitCode", "duplicate", "Unit code already exists");
            model.addAttribute("isEdit", false);
            return "masters/units/form";
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        Unit unit = unitMapper.toEntity(unitDTO);
        unit.setTenant(tenant);
        if (unit.getStatus() == null) {
            unit.setStatus(Status.ACTIVE);
        }
        unitRepository.save(unit);

        redirectAttributes.addFlashAttribute("successMessage", "Unit created successfully");
        return "redirect:/masters/units";
    }

    /**
     * Update existing unit.
     */
    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("unit") UnitDTO unitDTO,
                         BindingResult bindingResult,
                         @AuthenticationPrincipal CustomUserDetails userDetails,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", true);
            return "masters/units/form";
        }

        Long tenantId = userDetails.getTenantId();

        return unitRepository.findByIdAndTenantId(id, tenantId)
                .map(existingUnit -> {
                    // Check for duplicate (excluding current unit)
                    if (!existingUnit.getUnitCode().equals(unitDTO.getUnitCode()) &&
                            unitRepository.existsByTenantIdAndUnitCode(tenantId, unitDTO.getUnitCode())) {
                        bindingResult.rejectValue("unitCode", "duplicate", "Unit code already exists");
                        model.addAttribute("isEdit", true);
                        return "masters/units/form";
                    }

                    existingUnit.setUnitCode(unitDTO.getUnitCode());
                    existingUnit.setStatus(unitDTO.getStatus());
                    unitRepository.save(existingUnit);

                    redirectAttributes.addFlashAttribute("successMessage", "Unit updated successfully");
                    return "redirect:/masters/units";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Unit not found");
                    return "redirect:/masters/units";
                });
    }

    /**
     * Delete unit.
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal CustomUserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        Long tenantId = userDetails.getTenantId();

        return unitRepository.findByIdAndTenantId(id, tenantId)
                .map(unit -> {
                    unitRepository.delete(unit);
                    redirectAttributes.addFlashAttribute("successMessage", "Unit deleted successfully");
                    return "redirect:/masters/units";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Unit not found");
                    return "redirect:/masters/units";
                });
    }
}
