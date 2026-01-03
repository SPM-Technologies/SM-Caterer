package com.smtech.SM_Caterer.web.controller;

import com.smtech.SM_Caterer.domain.entity.Menu;
import com.smtech.SM_Caterer.domain.entity.Tenant;
import com.smtech.SM_Caterer.domain.enums.MenuCategory;
import com.smtech.SM_Caterer.domain.enums.Status;
import com.smtech.SM_Caterer.domain.repository.MenuRepository;
import com.smtech.SM_Caterer.domain.repository.TenantRepository;
import com.smtech.SM_Caterer.security.CustomUserDetails;
import com.smtech.SM_Caterer.service.dto.MenuDTO;
import com.smtech.SM_Caterer.service.mapper.MenuMapper;
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
 * Web Controller for Menu management.
 */
@Controller
@RequestMapping("/masters/menus")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER')")
@Slf4j
public class MenuWebController {

    private final MenuRepository menuRepository;
    private final MenuMapper menuMapper;
    private final TenantRepository tenantRepository;

    @GetMapping
    public String list(@AuthenticationPrincipal CustomUserDetails userDetails,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(defaultValue = "menuCode") String sortBy,
                       @RequestParam(defaultValue = "asc") String sortDir,
                       Model model) {
        Long tenantId = userDetails.getTenantId();

        size = Math.min(size, 100);
        Sort sort = sortDir.equalsIgnoreCase("desc")
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Menu> menuPage = menuRepository.findByTenantId(tenantId, pageable);

        model.addAttribute("menus", menuPage.getContent());
        model.addAttribute("categories", MenuCategory.values());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", menuPage.getTotalPages());
        model.addAttribute("totalItems", menuPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);

        log.debug("Listed {} menus for tenant {} (page {}/{})",
            menuPage.getNumberOfElements(), tenantId, page + 1, menuPage.getTotalPages());

        return "masters/menus/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("menu", new MenuDTO());
        model.addAttribute("categories", MenuCategory.values());
        model.addAttribute("isEdit", false);
        return "masters/menus/form";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        Long tenantId = userDetails.getTenantId();
        return menuRepository.findByIdAndTenantId(id, tenantId)
                .map(menu -> {
                    model.addAttribute("menu", menuMapper.toDto(menu));
                    model.addAttribute("categories", MenuCategory.values());
                    model.addAttribute("isEdit", true);
                    return "masters/menus/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Menu not found");
                    return "redirect:/masters/menus";
                });
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("menu") MenuDTO menuDTO,
                         BindingResult bindingResult,
                         @AuthenticationPrincipal CustomUserDetails userDetails,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Long tenantId = userDetails.getTenantId();

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", MenuCategory.values());
            model.addAttribute("isEdit", false);
            return "masters/menus/form";
        }

        if (menuRepository.existsByTenantIdAndMenuCode(tenantId, menuDTO.getMenuCode())) {
            bindingResult.rejectValue("menuCode", "duplicate", "Menu code already exists");
            model.addAttribute("categories", MenuCategory.values());
            model.addAttribute("isEdit", false);
            return "masters/menus/form";
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        Menu menu = menuMapper.toEntity(menuDTO);
        menu.setTenant(tenant);
        if (menu.getStatus() == null) {
            menu.setStatus(Status.ACTIVE);
        }
        menuRepository.save(menu);

        redirectAttributes.addFlashAttribute("successMessage", "Menu created successfully");
        return "redirect:/masters/menus";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("menu") MenuDTO menuDTO,
                         BindingResult bindingResult,
                         @AuthenticationPrincipal CustomUserDetails userDetails,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Long tenantId = userDetails.getTenantId();

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", MenuCategory.values());
            model.addAttribute("isEdit", true);
            return "masters/menus/form";
        }

        return menuRepository.findByIdAndTenantId(id, tenantId)
                .map(existingMenu -> {
                    if (!existingMenu.getMenuCode().equals(menuDTO.getMenuCode()) &&
                            menuRepository.existsByTenantIdAndMenuCode(tenantId, menuDTO.getMenuCode())) {
                        bindingResult.rejectValue("menuCode", "duplicate", "Menu code already exists");
                        model.addAttribute("categories", MenuCategory.values());
                        model.addAttribute("isEdit", true);
                        return "masters/menus/form";
                    }

                    existingMenu.setMenuCode(menuDTO.getMenuCode());
                    existingMenu.setCategory(menuDTO.getCategory());
                    existingMenu.setCostPerServe(menuDTO.getCostPerServe());
                    existingMenu.setServesCount(menuDTO.getServesCount());
                    existingMenu.setStatus(menuDTO.getStatus());
                    menuRepository.save(existingMenu);

                    redirectAttributes.addFlashAttribute("successMessage", "Menu updated successfully");
                    return "redirect:/masters/menus";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Menu not found");
                    return "redirect:/masters/menus";
                });
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal CustomUserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        Long tenantId = userDetails.getTenantId();

        return menuRepository.findByIdAndTenantId(id, tenantId)
                .map(menu -> {
                    menuRepository.delete(menu);
                    redirectAttributes.addFlashAttribute("successMessage", "Menu deleted successfully");
                    return "redirect:/masters/menus";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Menu not found");
                    return "redirect:/masters/menus";
                });
    }
}
