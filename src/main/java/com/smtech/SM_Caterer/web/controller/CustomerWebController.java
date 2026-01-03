package com.smtech.SM_Caterer.web.controller;

import com.smtech.SM_Caterer.domain.entity.Customer;
import com.smtech.SM_Caterer.domain.entity.Tenant;
import com.smtech.SM_Caterer.domain.enums.Status;
import com.smtech.SM_Caterer.domain.repository.CustomerRepository;
import com.smtech.SM_Caterer.domain.repository.TenantRepository;
import com.smtech.SM_Caterer.security.CustomUserDetails;
import com.smtech.SM_Caterer.service.CustomerService;
import com.smtech.SM_Caterer.service.dto.CustomerDTO;
import com.smtech.SM_Caterer.service.mapper.CustomerMapper;
import com.smtech.SM_Caterer.web.dto.CustomerQuickCreateDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Web Controller for Customer management.
 * Handles CRUD operations and quick customer creation for orders.
 */
@Controller
@RequestMapping("/customers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER', 'STAFF')")
@Slf4j
public class CustomerWebController {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final CustomerService customerService;
    private final TenantRepository tenantRepository;

    // ===== Customer List =====

    @GetMapping
    public String list(@AuthenticationPrincipal CustomUserDetails userDetails,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(defaultValue = "name") String sortBy,
                       @RequestParam(defaultValue = "asc") String sortDir,
                       @RequestParam(required = false) String search,
                       Model model) {
        Long tenantId = userDetails.getTenantId();

        size = Math.min(size, 100);
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Customer> customerPage = customerRepository.findByTenantId(tenantId, pageable);

        model.addAttribute("customers", customerPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", customerPage.getTotalPages());
        model.addAttribute("totalItems", customerPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("search", search);

        log.debug("Listed {} customers for tenant {} (page {}/{})",
                customerPage.getNumberOfElements(), tenantId, page + 1, customerPage.getTotalPages());

        return "customers/list";
    }

    // ===== Create Customer =====

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("customer", new CustomerDTO());
        model.addAttribute("isEdit", false);
        return "customers/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("customer") CustomerDTO customerDTO,
                         BindingResult bindingResult,
                         @AuthenticationPrincipal CustomUserDetails userDetails,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Long tenantId = userDetails.getTenantId();

        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            return "customers/form";
        }

        // Check for duplicate customer code
        if (customerRepository.existsByTenantIdAndCustomerCode(tenantId, customerDTO.getCustomerCode())) {
            bindingResult.rejectValue("customerCode", "duplicate", "Customer code already exists");
            model.addAttribute("isEdit", false);
            return "customers/form";
        }

        // Check for duplicate phone
        if (customerDTO.getPhone() != null &&
                customerRepository.existsByTenantIdAndPhone(tenantId, customerDTO.getPhone())) {
            bindingResult.rejectValue("phone", "duplicate", "Phone number already registered");
            model.addAttribute("isEdit", false);
            return "customers/form";
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        Customer customer = customerMapper.toEntity(customerDTO);
        customer.setTenant(tenant);
        if (customer.getStatus() == null) {
            customer.setStatus(Status.ACTIVE);
        }
        customerRepository.save(customer);

        redirectAttributes.addFlashAttribute("successMessage", "Customer created successfully");
        return "redirect:/customers";
    }

    // ===== Edit Customer =====

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        Long tenantId = userDetails.getTenantId();

        return customerRepository.findById(id)
                .filter(c -> c.getTenant().getId().equals(tenantId))
                .map(customer -> {
                    model.addAttribute("customer", customerMapper.toDto(customer));
                    model.addAttribute("isEdit", true);
                    return "customers/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Customer not found");
                    return "redirect:/customers";
                });
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("customer") CustomerDTO customerDTO,
                         BindingResult bindingResult,
                         @AuthenticationPrincipal CustomUserDetails userDetails,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Long tenantId = userDetails.getTenantId();

        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", true);
            return "customers/form";
        }

        return customerRepository.findById(id)
                .filter(c -> c.getTenant().getId().equals(tenantId))
                .map(existingCustomer -> {
                    // Check for duplicate customer code (excluding current)
                    if (!existingCustomer.getCustomerCode().equals(customerDTO.getCustomerCode()) &&
                            customerRepository.existsByTenantIdAndCustomerCode(tenantId, customerDTO.getCustomerCode())) {
                        bindingResult.rejectValue("customerCode", "duplicate", "Customer code already exists");
                        model.addAttribute("isEdit", true);
                        return "customers/form";
                    }

                    // Check for duplicate phone (excluding current)
                    if (customerDTO.getPhone() != null &&
                            !customerDTO.getPhone().equals(existingCustomer.getPhone()) &&
                            customerRepository.existsByTenantIdAndPhone(tenantId, customerDTO.getPhone())) {
                        bindingResult.rejectValue("phone", "duplicate", "Phone number already registered");
                        model.addAttribute("isEdit", true);
                        return "customers/form";
                    }

                    existingCustomer.setCustomerCode(customerDTO.getCustomerCode());
                    existingCustomer.setName(customerDTO.getName());
                    existingCustomer.setEmail(customerDTO.getEmail());
                    existingCustomer.setPhone(customerDTO.getPhone());
                    existingCustomer.setAddress(customerDTO.getAddress());
                    existingCustomer.setCity(customerDTO.getCity());
                    existingCustomer.setState(customerDTO.getState());
                    existingCustomer.setPincode(customerDTO.getPincode());
                    existingCustomer.setGstin(customerDTO.getGstin());
                    existingCustomer.setStatus(customerDTO.getStatus());
                    customerRepository.save(existingCustomer);

                    redirectAttributes.addFlashAttribute("successMessage", "Customer updated successfully");
                    return "redirect:/customers";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Customer not found");
                    return "redirect:/customers";
                });
    }

    // ===== Delete Customer =====

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal CustomUserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        Long tenantId = userDetails.getTenantId();

        return customerRepository.findById(id)
                .filter(c -> c.getTenant().getId().equals(tenantId))
                .map(customer -> {
                    customerRepository.delete(customer);
                    redirectAttributes.addFlashAttribute("successMessage", "Customer deleted successfully");
                    return "redirect:/customers";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Customer not found");
                    return "redirect:/customers";
                });
    }

    // ===== View Customer =====

    @GetMapping("/{id}")
    public String view(@PathVariable Long id,
                       @AuthenticationPrincipal CustomUserDetails userDetails,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        Long tenantId = userDetails.getTenantId();

        return customerRepository.findById(id)
                .filter(c -> c.getTenant().getId().equals(tenantId))
                .map(customer -> {
                    model.addAttribute("customer", customerMapper.toDto(customer));
                    return "customers/view";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Customer not found");
                    return "redirect:/customers";
                });
    }

    // ===== AJAX: Quick Create Customer (for Order Wizard) =====

    @PostMapping("/api/quick-create")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> quickCreate(
            @Valid @RequestBody CustomerQuickCreateDTO quickCreateDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long tenantId = userDetails.getTenantId();
        Map<String, Object> response = new HashMap<>();

        try {
            // Validate
            if (!quickCreateDTO.isValid()) {
                response.put("success", false);
                response.put("message", "Name and phone are required");
                return ResponseEntity.badRequest().body(response);
            }

            // Check for duplicate phone
            if (customerRepository.existsByTenantIdAndPhone(tenantId, quickCreateDTO.getPhone())) {
                // Return existing customer
                Customer existing = customerRepository.findByTenantIdAndPhone(tenantId, quickCreateDTO.getPhone())
                        .orElse(null);
                if (existing != null) {
                    response.put("success", true);
                    response.put("isExisting", true);
                    response.put("customer", mapCustomerToResponse(existing));
                    return ResponseEntity.ok(response);
                }
            }

            // Generate customer code
            String customerCode = generateCustomerCode(quickCreateDTO.getPhone());

            // Create customer DTO
            CustomerDTO customerDTO = CustomerDTO.builder()
                    .tenantId(tenantId)
                    .customerCode(customerCode)
                    .name(quickCreateDTO.getName())
                    .phone(quickCreateDTO.getPhone())
                    .email(quickCreateDTO.getEmail())
                    .address(quickCreateDTO.getAddress())
                    .city(quickCreateDTO.getCity())
                    .state(quickCreateDTO.getState())
                    .pincode(quickCreateDTO.getPincode())
                    .status(Status.ACTIVE)
                    .build();

            CustomerDTO created = customerService.create(customerDTO);

            response.put("success", true);
            response.put("isExisting", false);
            response.put("customer", Map.of(
                    "id", created.getId(),
                    "code", created.getCustomerCode(),
                    "name", created.getName(),
                    "phone", created.getPhone(),
                    "email", created.getEmail() != null ? created.getEmail() : ""
            ));

            log.info("Quick-created customer {} for tenant {}", created.getCustomerCode(), tenantId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to quick-create customer", e);
            response.put("success", false);
            response.put("message", "Failed to create customer: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ===== AJAX: Search Customers =====

    @GetMapping("/api/search")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> search(
            @RequestParam String q,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long tenantId = userDetails.getTenantId();

        List<Customer> customers;
        if (q.matches("\\d+")) {
            // Search by phone
            customers = customerRepository.searchByPhone(tenantId, q);
        } else {
            // Search by name
            customers = customerRepository.searchByName(tenantId, q);
        }

        List<Map<String, Object>> result = customers.stream()
                .filter(c -> c.getStatus() == Status.ACTIVE)
                .limit(10)
                .map(this::mapCustomerToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // ===== AJAX: Check Phone Exists =====

    @GetMapping("/api/check-phone")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkPhone(
            @RequestParam String phone,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long tenantId = userDetails.getTenantId();
        Map<String, Object> response = new HashMap<>();

        boolean exists = customerRepository.existsByTenantIdAndPhone(tenantId, phone);
        response.put("exists", exists);

        if (exists) {
            customerRepository.findByTenantIdAndPhone(tenantId, phone)
                    .ifPresent(customer -> response.put("customer", mapCustomerToResponse(customer)));
        }

        return ResponseEntity.ok(response);
    }

    // ===== Helper Methods =====

    private String generateCustomerCode(String phone) {
        String phoneSuffix = phone.substring(phone.length() - 4);
        String timestamp = String.valueOf(System.currentTimeMillis() % 10000);
        return "CUST-" + phoneSuffix + "-" + timestamp;
    }

    private Map<String, Object> mapCustomerToResponse(Customer customer) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", customer.getId());
        map.put("code", customer.getCustomerCode());
        map.put("name", customer.getName());
        map.put("phone", customer.getPhone());
        map.put("email", customer.getEmail() != null ? customer.getEmail() : "");
        map.put("address", customer.getAddress() != null ? customer.getAddress() : "");
        map.put("city", customer.getCity() != null ? customer.getCity() : "");
        map.put("state", customer.getState() != null ? customer.getState() : "");
        return map;
    }
}
