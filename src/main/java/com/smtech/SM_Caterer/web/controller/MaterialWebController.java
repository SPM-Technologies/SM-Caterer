package com.smtech.SM_Caterer.web.controller;

import com.smtech.SM_Caterer.domain.entity.Material;
import com.smtech.SM_Caterer.domain.entity.Tenant;
import com.smtech.SM_Caterer.domain.entity.Unit;
import com.smtech.SM_Caterer.domain.enums.Status;
import com.smtech.SM_Caterer.domain.repository.MaterialRepository;
import com.smtech.SM_Caterer.domain.repository.TenantRepository;
import com.smtech.SM_Caterer.domain.repository.UnitRepository;
import com.smtech.SM_Caterer.security.CustomUserDetails;
import com.smtech.SM_Caterer.service.dto.MaterialDTO;
import com.smtech.SM_Caterer.service.mapper.MaterialMapper;
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
 * Web Controller for Material management.
 */
@Controller
@RequestMapping("/masters/materials")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER')")
@Slf4j
public class MaterialWebController {

    private final MaterialRepository materialRepository;
    private final UnitRepository unitRepository;
    private final MaterialMapper materialMapper;
    private final TenantRepository tenantRepository;

    @GetMapping
    public String list(@AuthenticationPrincipal CustomUserDetails userDetails,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(defaultValue = "materialCode") String sortBy,
                       @RequestParam(defaultValue = "asc") String sortDir,
                       Model model) {
        Long tenantId = userDetails.getTenantId();

        size = Math.min(size, 100);
        Sort sort = sortDir.equalsIgnoreCase("desc")
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Material> materialPage = materialRepository.findByTenantId(tenantId, pageable);

        model.addAttribute("materials", materialPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", materialPage.getTotalPages());
        model.addAttribute("totalItems", materialPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);

        log.debug("Listed {} materials for tenant {} (page {}/{})",
            materialPage.getNumberOfElements(), tenantId, page + 1, materialPage.getTotalPages());

        return "masters/materials/list";
    }

    @GetMapping("/new")
    public String showCreateForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("material", new MaterialDTO());
        model.addAttribute("units", unitRepository.findByTenantId(userDetails.getTenantId()));
        model.addAttribute("isEdit", false);
        return "masters/materials/form";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        Long tenantId = userDetails.getTenantId();
        return materialRepository.findByIdAndTenantId(id, tenantId)
                .map(material -> {
                    model.addAttribute("material", materialMapper.toDto(material));
                    model.addAttribute("units", unitRepository.findByTenantId(tenantId));
                    model.addAttribute("isEdit", true);
                    return "masters/materials/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Material not found");
                    return "redirect:/masters/materials";
                });
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("material") MaterialDTO materialDTO,
                         BindingResult bindingResult,
                         @AuthenticationPrincipal CustomUserDetails userDetails,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Long tenantId = userDetails.getTenantId();

        if (bindingResult.hasErrors()) {
            model.addAttribute("units", unitRepository.findByTenantId(tenantId));
            model.addAttribute("isEdit", false);
            return "masters/materials/form";
        }

        if (materialRepository.existsByTenantIdAndMaterialCode(tenantId, materialDTO.getMaterialCode())) {
            bindingResult.rejectValue("materialCode", "duplicate", "Material code already exists");
            model.addAttribute("units", unitRepository.findByTenantId(tenantId));
            model.addAttribute("isEdit", false);
            return "masters/materials/form";
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        Unit unit = unitRepository.findById(materialDTO.getUnitId())
                .orElseThrow(() -> new RuntimeException("Unit not found"));

        Material material = materialMapper.toEntity(materialDTO);
        material.setTenant(tenant);
        material.setUnit(unit);
        if (material.getStatus() == null) {
            material.setStatus(Status.ACTIVE);
        }
        materialRepository.save(material);

        redirectAttributes.addFlashAttribute("successMessage", "Material created successfully");
        return "redirect:/masters/materials";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("material") MaterialDTO materialDTO,
                         BindingResult bindingResult,
                         @AuthenticationPrincipal CustomUserDetails userDetails,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Long tenantId = userDetails.getTenantId();

        if (bindingResult.hasErrors()) {
            model.addAttribute("units", unitRepository.findByTenantId(tenantId));
            model.addAttribute("isEdit", true);
            return "masters/materials/form";
        }

        return materialRepository.findByIdAndTenantId(id, tenantId)
                .map(existingMaterial -> {
                    if (!existingMaterial.getMaterialCode().equals(materialDTO.getMaterialCode()) &&
                            materialRepository.existsByTenantIdAndMaterialCode(tenantId, materialDTO.getMaterialCode())) {
                        bindingResult.rejectValue("materialCode", "duplicate", "Material code already exists");
                        model.addAttribute("units", unitRepository.findByTenantId(tenantId));
                        model.addAttribute("isEdit", true);
                        return "masters/materials/form";
                    }

                    Unit unit = unitRepository.findById(materialDTO.getUnitId())
                            .orElseThrow(() -> new RuntimeException("Unit not found"));

                    existingMaterial.setMaterialCode(materialDTO.getMaterialCode());
                    existingMaterial.setUnit(unit);
                    existingMaterial.setMinimumStock(materialDTO.getMinimumStock());
                    existingMaterial.setCurrentStock(materialDTO.getCurrentStock());
                    existingMaterial.setCostPerUnit(materialDTO.getCostPerUnit());
                    existingMaterial.setStatus(materialDTO.getStatus());
                    materialRepository.save(existingMaterial);

                    redirectAttributes.addFlashAttribute("successMessage", "Material updated successfully");
                    return "redirect:/masters/materials";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Material not found");
                    return "redirect:/masters/materials";
                });
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal CustomUserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        Long tenantId = userDetails.getTenantId();

        return materialRepository.findByIdAndTenantId(id, tenantId)
                .map(material -> {
                    materialRepository.delete(material);
                    redirectAttributes.addFlashAttribute("successMessage", "Material deleted successfully");
                    return "redirect:/masters/materials";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Material not found");
                    return "redirect:/masters/materials";
                });
    }
}
