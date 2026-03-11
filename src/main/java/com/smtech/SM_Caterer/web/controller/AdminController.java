package com.smtech.SM_Caterer.web.controller;

import com.smtech.SM_Caterer.domain.enums.TenantStatus;
import com.smtech.SM_Caterer.domain.enums.UserRole;
import com.smtech.SM_Caterer.domain.enums.UserStatus;
import com.smtech.SM_Caterer.security.CustomUserDetails;
import com.smtech.SM_Caterer.service.TenantService;
import com.smtech.SM_Caterer.service.UserService;
import com.smtech.SM_Caterer.service.dto.TenantDTO;
import com.smtech.SM_Caterer.service.dto.UserDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

/**
 * Web Controller for SUPER_ADMIN administration functions.
 * Manages tenants and users across the system.
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
@Slf4j
public class AdminController {

    private static final String REDIRECT_ADMIN_TENANTS = "redirect:/admin/tenants";
    private static final String REDIRECT_ADMIN_USERS = "redirect:/admin/users";

    private final TenantService tenantService;
    private final UserService userService;

    // ===== Admin Dashboard =====

    @GetMapping
    public String adminDashboard(Model model) {
        long totalTenants = tenantService.countAll();
        long activeTenants = tenantService.countByStatus(TenantStatus.ACTIVE);
        long totalUsers = userService.countAll();

        model.addAttribute("totalTenants", totalTenants);
        model.addAttribute("activeTenants", activeTenants);
        model.addAttribute("totalUsers", totalUsers);

        Page<TenantDTO> recentTenants = tenantService.findAll(PageRequest.of(0, 5, Sort.by("createdAt").descending()));
        model.addAttribute("recentTenants", recentTenants.getContent());

        return "admin/dashboard";
    }

    // ===== Tenant Management =====

    @GetMapping("/tenants")
    public String listTenants(@RequestParam(required = false) TenantStatus status,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "20") int size,
                               @RequestParam(defaultValue = "id") String sortBy,
                               @RequestParam(defaultValue = "desc") String sortDir,
                               Model model) {
        size = Math.min(size, 100);
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<TenantDTO> tenantPage;
        if (status != null) {
            tenantPage = tenantService.findByStatus(status, pageable);
        } else {
            tenantPage = tenantService.findAll(pageable);
        }

        model.addAttribute("tenants", tenantPage.getContent());
        model.addAttribute("statuses", TenantStatus.values());
        model.addAttribute("status", status);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", tenantPage.getTotalPages());
        model.addAttribute("totalItems", tenantPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);

        return "admin/tenants/list";
    }

    @GetMapping("/tenants/new")
    public String newTenantForm(Model model) {
        TenantDTO tenant = new TenantDTO();
        tenant.setStatus(TenantStatus.ACTIVE);
        tenant.setSubscriptionStartDate(LocalDate.now());
        tenant.setSubscriptionEndDate(LocalDate.now().plusYears(1));

        model.addAttribute("tenant", tenant);
        model.addAttribute("statuses", TenantStatus.values());
        model.addAttribute("isNew", true);

        return "admin/tenants/form";
    }

    @PostMapping("/tenants")
    public String createTenant(@Valid @ModelAttribute("tenant") TenantDTO tenant,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("statuses", TenantStatus.values());
            model.addAttribute("isNew", true);
            return "admin/tenants/form";
        }

        try {
            tenantService.create(tenant);
            redirectAttributes.addFlashAttribute("successMessage", "Tenant created successfully");
            return REDIRECT_ADMIN_TENANTS;
        } catch (Exception e) {
            log.error("Failed to create tenant: {}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("statuses", TenantStatus.values());
            model.addAttribute("isNew", true);
            return "admin/tenants/form";
        }
    }

    @GetMapping("/tenants/{id}/edit")
    public String editTenantForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return tenantService.findById(id)
                .map(tenant -> {
                    model.addAttribute("tenant", tenant);
                    model.addAttribute("statuses", TenantStatus.values());
                    model.addAttribute("isNew", false);
                    return "admin/tenants/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Tenant not found");
                    return REDIRECT_ADMIN_TENANTS;
                });
    }

    @PostMapping("/tenants/{id}")
    public String updateTenant(@PathVariable Long id,
                                @Valid @ModelAttribute("tenant") TenantDTO tenant,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("statuses", TenantStatus.values());
            model.addAttribute("isNew", false);
            return "admin/tenants/form";
        }

        try {
            tenantService.update(id, tenant);
            redirectAttributes.addFlashAttribute("successMessage", "Tenant updated successfully");
            return REDIRECT_ADMIN_TENANTS;
        } catch (Exception e) {
            log.error("Failed to update tenant: {}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("statuses", TenantStatus.values());
            model.addAttribute("isNew", false);
            return "admin/tenants/form";
        }
    }

    @PostMapping("/tenants/{id}/delete")
    public String deleteTenant(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            tenantService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Tenant deleted successfully");
        } catch (Exception e) {
            log.error("Failed to delete tenant: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return REDIRECT_ADMIN_TENANTS;
    }

    // ===== User Management =====

    @GetMapping("/users")
    public String listUsers(@RequestParam(required = false) Long tenantId,
                            @RequestParam(required = false) UserRole role,
                            @RequestParam(required = false) UserStatus status,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "20") int size,
                            @RequestParam(defaultValue = "id") String sortBy,
                            @RequestParam(defaultValue = "desc") String sortDir,
                            Model model) {
        size = Math.min(size, 100);
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<UserDTO> userPage = userService.findAllUsers(tenantId, role, status, pageable);

        List<TenantDTO> tenants = tenantService.findAll(PageRequest.of(0, 100)).getContent();

        model.addAttribute("users", userPage.getContent());
        model.addAttribute("tenants", tenants);
        model.addAttribute("roles", UserRole.values());
        model.addAttribute("statuses", UserStatus.values());
        model.addAttribute("tenantId", tenantId);
        model.addAttribute("role", role);
        model.addAttribute("status", status);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("totalItems", userPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);

        return "admin/users/list";
    }

    @GetMapping("/users/new")
    public String newUserForm(Model model) {
        UserDTO user = new UserDTO();
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(UserRole.STAFF);

        List<TenantDTO> tenants = tenantService.findAll(PageRequest.of(0, 100)).getContent();

        model.addAttribute("user", user);
        model.addAttribute("tenants", tenants);
        model.addAttribute("roles", UserRole.values());
        model.addAttribute("statuses", UserStatus.values());
        model.addAttribute("isNew", true);

        return "admin/users/form";
    }

    @PostMapping("/users")
    public String createUser(@Valid @ModelAttribute("user") UserDTO user,
                              BindingResult bindingResult,
                              @RequestParam(required = false) String rawPassword,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            List<TenantDTO> tenants = tenantService.findAll(PageRequest.of(0, 100)).getContent();
            model.addAttribute("tenants", tenants);
            model.addAttribute("roles", UserRole.values());
            model.addAttribute("statuses", UserStatus.values());
            model.addAttribute("isNew", true);
            return "admin/users/form";
        }

        try {
            if (rawPassword != null && !rawPassword.isBlank()) {
                user.setPassword(rawPassword);
            }
            userService.createUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "User created successfully");
            return REDIRECT_ADMIN_USERS;
        } catch (Exception e) {
            log.error("Failed to create user: {}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            List<TenantDTO> tenants = tenantService.findAll(PageRequest.of(0, 100)).getContent();
            model.addAttribute("tenants", tenants);
            model.addAttribute("roles", UserRole.values());
            model.addAttribute("statuses", UserStatus.values());
            model.addAttribute("isNew", true);
            return "admin/users/form";
        }
    }

    @GetMapping("/users/{id}/edit")
    public String editUserForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return userService.findById(id)
                .map(user -> {
                    List<TenantDTO> tenants = tenantService.findAll(PageRequest.of(0, 100)).getContent();
                    model.addAttribute("user", user);
                    model.addAttribute("tenants", tenants);
                    model.addAttribute("roles", UserRole.values());
                    model.addAttribute("statuses", UserStatus.values());
                    model.addAttribute("isNew", false);
                    return "admin/users/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "User not found");
                    return REDIRECT_ADMIN_USERS;
                });
    }

    @PostMapping("/users/{id}")
    public String updateUser(@PathVariable Long id,
                              @Valid @ModelAttribute("user") UserDTO user,
                              BindingResult bindingResult,
                              @RequestParam(required = false) String rawPassword,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            List<TenantDTO> tenants = tenantService.findAll(PageRequest.of(0, 100)).getContent();
            model.addAttribute("tenants", tenants);
            model.addAttribute("roles", UserRole.values());
            model.addAttribute("statuses", UserStatus.values());
            model.addAttribute("isNew", false);
            return "admin/users/form";
        }

        try {
            if (rawPassword != null && !rawPassword.isBlank()) {
                user.setPassword(rawPassword);
            }
            userService.updateUser(id, user);
            redirectAttributes.addFlashAttribute("successMessage", "User updated successfully");
            return REDIRECT_ADMIN_USERS;
        } catch (Exception e) {
            log.error("Failed to update user: {}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            List<TenantDTO> tenants = tenantService.findAll(PageRequest.of(0, 100)).getContent();
            model.addAttribute("tenants", tenants);
            model.addAttribute("roles", UserRole.values());
            model.addAttribute("statuses", UserStatus.values());
            model.addAttribute("isNew", false);
            return "admin/users/form";
        }
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id,
                              @AuthenticationPrincipal CustomUserDetails currentUser,
                              RedirectAttributes redirectAttributes) {
        if (currentUser.getId().equals(id)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete your own account");
            return REDIRECT_ADMIN_USERS;
        }

        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully");
        } catch (Exception e) {
            log.error("Failed to delete user: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return REDIRECT_ADMIN_USERS;
    }
}
