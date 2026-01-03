package com.smtech.SM_Caterer.web.controller;

import com.smtech.SM_Caterer.domain.entity.Customer;
import com.smtech.SM_Caterer.domain.entity.EventType;
import com.smtech.SM_Caterer.domain.entity.Menu;
import com.smtech.SM_Caterer.domain.entity.Utility;
import com.smtech.SM_Caterer.domain.enums.MenuCategory;
import com.smtech.SM_Caterer.domain.enums.OrderStatus;
import com.smtech.SM_Caterer.domain.enums.Status;
import com.smtech.SM_Caterer.domain.repository.CustomerRepository;
import com.smtech.SM_Caterer.domain.repository.EventTypeRepository;
import com.smtech.SM_Caterer.domain.repository.MenuRepository;
import com.smtech.SM_Caterer.domain.repository.UtilityRepository;
import com.smtech.SM_Caterer.exception.ResourceNotFoundException;
import com.smtech.SM_Caterer.security.CustomUserDetails;
import com.smtech.SM_Caterer.service.OrderService;
import com.smtech.SM_Caterer.service.dto.OrderDTO;
import com.smtech.SM_Caterer.service.dto.OrderDetailDTO;
import com.smtech.SM_Caterer.service.dto.OrderSearchCriteria;
import com.smtech.SM_Caterer.web.dto.OrderFormDTO;
import com.smtech.SM_Caterer.web.dto.OrderMenuItemFormDTO;
import com.smtech.SM_Caterer.web.dto.OrderUtilityFormDTO;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Web Controller for Order management.
 * Handles order list, multi-step wizard, view, and workflow actions.
 */
@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER', 'STAFF')")
@Slf4j
public class OrderWebController {

    private static final String ORDER_FORM_KEY = "orderForm";
    private static final String REDIRECT_ORDERS = "redirect:/orders";

    private final OrderService orderService;
    private final CustomerRepository customerRepository;
    private final EventTypeRepository eventTypeRepository;
    private final MenuRepository menuRepository;
    private final UtilityRepository utilityRepository;

    // ===== Order List =====

    @GetMapping
    public String list(@AuthenticationPrincipal CustomUserDetails userDetails,
                       @RequestParam(required = false) String orderNumber,
                       @RequestParam(required = false) String customerName,
                       @RequestParam(required = false) OrderStatus status,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate eventDateFrom,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate eventDateTo,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(defaultValue = "eventDate") String sortBy,
                       @RequestParam(defaultValue = "desc") String sortDir,
                       Model model) {
        Long tenantId = userDetails.getTenantId();

        size = Math.min(size, 100);
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // Build search criteria
        OrderSearchCriteria criteria = OrderSearchCriteria.builder()
                .orderNumber(orderNumber)
                .customerName(customerName)
                .status(status)
                .eventDateFrom(eventDateFrom)
                .eventDateTo(eventDateTo)
                .build();

        Page<OrderDTO> orderPage = orderService.searchOrders(tenantId, criteria, pageable);

        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("criteria", criteria);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("totalItems", orderPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);

        log.debug("Listed {} orders for tenant {} (page {}/{})",
                orderPage.getNumberOfElements(), tenantId, page + 1, orderPage.getTotalPages());

        return "orders/list";
    }

    // ===== Order View =====

    @GetMapping("/{id}")
    public String view(@PathVariable Long id,
                       @AuthenticationPrincipal CustomUserDetails userDetails,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        Long tenantId = userDetails.getTenantId();

        return orderService.findByIdWithDetails(id)
                .filter(order -> order.getTenantId().equals(tenantId))
                .map(order -> {
                    model.addAttribute("order", order);
                    return "orders/view";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Order not found");
                    return REDIRECT_ORDERS;
                });
    }

    // ===== Wizard: Start New Order =====

    @GetMapping("/new")
    public String startWizard(HttpSession session,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              Model model) {
        // Clear any existing form and start fresh
        OrderFormDTO form = new OrderFormDTO();
        form.setCurrentStep(1);
        session.setAttribute(ORDER_FORM_KEY, form);

        return redirectToStep(1);
    }

    // ===== Wizard: Edit Existing Order =====

    @GetMapping("/{id}/edit")
    public String editOrder(@PathVariable Long id,
                            @AuthenticationPrincipal CustomUserDetails userDetails,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        Long tenantId = userDetails.getTenantId();

        return orderService.findByIdWithDetails(id)
                .filter(order -> order.getTenantId().equals(tenantId))
                .map(order -> {
                    if (!order.isEditable()) {
                        redirectAttributes.addFlashAttribute("errorMessage",
                                "Order cannot be edited in status: " + order.getStatus());
                        return "redirect:/orders/" + id;
                    }

                    // Convert order to form DTO
                    OrderFormDTO form = convertToFormDTO(order);
                    form.setCurrentStep(1);
                    session.setAttribute(ORDER_FORM_KEY, form);
                    session.setAttribute("editingOrderId", id);

                    return redirectToStep(1);
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Order not found");
                    return REDIRECT_ORDERS;
                });
    }

    // ===== Wizard Step 1: Customer Selection =====

    @GetMapping("/wizard/step1")
    public String wizardStep1(@AuthenticationPrincipal CustomUserDetails userDetails,
                              HttpSession session,
                              Model model) {
        OrderFormDTO form = getFormFromSession(session);
        Long tenantId = userDetails.getTenantId();

        // Load active customers for selection
        List<Customer> customers = customerRepository.findByTenantIdAndStatus(tenantId, Status.ACTIVE);

        model.addAttribute("form", form);
        model.addAttribute("customers", customers);
        model.addAttribute("currentStep", 1);
        model.addAttribute("isEditing", session.getAttribute("editingOrderId") != null);

        return "orders/wizard/step1";
    }

    @PostMapping("/wizard/step1")
    public String processStep1(@ModelAttribute("form") OrderFormDTO form,
                               BindingResult bindingResult,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               HttpSession session,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        OrderFormDTO sessionForm = getFormFromSession(session);
        Long tenantId = userDetails.getTenantId();

        // Update session form with step 1 data
        sessionForm.setCreateNewCustomer(form.isCreateNewCustomer());
        sessionForm.setCustomerId(form.getCustomerId());
        sessionForm.setNewCustomer(form.getNewCustomer());

        // Validate step 1
        if (!sessionForm.isStep1Valid()) {
            model.addAttribute("form", sessionForm);
            model.addAttribute("customers", customerRepository.findByTenantIdAndStatus(tenantId, Status.ACTIVE));
            model.addAttribute("currentStep", 1);
            model.addAttribute("errorMessage", "Please select or create a customer");
            return "orders/wizard/step1";
        }

        // If using existing customer, load display info
        if (!sessionForm.isCreateNewCustomer() && sessionForm.getCustomerId() != null) {
            customerRepository.findById(sessionForm.getCustomerId())
                    .ifPresent(customer -> {
                        sessionForm.setCustomerName(customer.getName());
                        sessionForm.setCustomerPhone(customer.getPhone());
                        sessionForm.setCustomerCode(customer.getCustomerCode());
                    });
        }

        sessionForm.setCurrentStep(2);
        session.setAttribute(ORDER_FORM_KEY, sessionForm);

        return redirectToStep(2);
    }

    // ===== Wizard Step 2: Event Details =====

    @GetMapping("/wizard/step2")
    public String wizardStep2(@AuthenticationPrincipal CustomUserDetails userDetails,
                              HttpSession session,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        OrderFormDTO form = getFormFromSession(session);

        // Validate previous step completed
        if (!form.isStep1Valid()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please complete step 1 first");
            return redirectToStep(1);
        }

        Long tenantId = userDetails.getTenantId();
        List<EventType> eventTypes = eventTypeRepository.findByTenantIdAndStatus(tenantId, Status.ACTIVE);

        model.addAttribute("form", form);
        model.addAttribute("eventTypes", eventTypes);
        model.addAttribute("currentStep", 2);

        return "orders/wizard/step2";
    }

    @PostMapping("/wizard/step2")
    public String processStep2(@ModelAttribute("form") OrderFormDTO form,
                               BindingResult bindingResult,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               HttpSession session,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        OrderFormDTO sessionForm = getFormFromSession(session);
        Long tenantId = userDetails.getTenantId();

        // Update session form with step 2 data
        sessionForm.setEventTypeId(form.getEventTypeId());
        sessionForm.setEventDate(form.getEventDate());
        sessionForm.setEventTime(form.getEventTime());
        sessionForm.setVenueName(form.getVenueName());
        sessionForm.setVenueAddress(form.getVenueAddress());
        sessionForm.setGuestCount(form.getGuestCount());

        // Validate step 2
        if (!sessionForm.isStep2Valid()) {
            model.addAttribute("form", sessionForm);
            model.addAttribute("eventTypes", eventTypeRepository.findByTenantIdAndStatus(tenantId, Status.ACTIVE));
            model.addAttribute("currentStep", 2);
            model.addAttribute("errorMessage", "Please fill all required event details");
            return "orders/wizard/step2";
        }

        // Load event type display info
        if (sessionForm.getEventTypeId() != null) {
            eventTypeRepository.findById(sessionForm.getEventTypeId())
                    .ifPresent(eventType -> {
                        sessionForm.setEventTypeName(eventType.getEventCode());
                        sessionForm.setEventTypeCode(eventType.getEventCode());
                    });
        }

        sessionForm.setCurrentStep(3);
        session.setAttribute(ORDER_FORM_KEY, sessionForm);

        return redirectToStep(3);
    }

    // ===== Wizard Step 3: Menu Items =====

    @GetMapping("/wizard/step3")
    public String wizardStep3(@AuthenticationPrincipal CustomUserDetails userDetails,
                              HttpSession session,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        OrderFormDTO form = getFormFromSession(session);

        // Validate previous steps completed
        if (!form.isStep2Valid()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please complete step 2 first");
            return redirectToStep(2);
        }

        Long tenantId = userDetails.getTenantId();
        List<Menu> menus = menuRepository.findByTenantIdAndStatus(tenantId, Status.ACTIVE);

        model.addAttribute("form", form);
        model.addAttribute("menus", menus);
        model.addAttribute("categories", MenuCategory.values());
        model.addAttribute("currentStep", 3);

        return "orders/wizard/step3";
    }

    @PostMapping("/wizard/step3")
    public String processStep3(@AuthenticationPrincipal CustomUserDetails userDetails,
                               HttpSession session,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        OrderFormDTO sessionForm = getFormFromSession(session);

        // Validate step 3
        if (!sessionForm.isStep3Valid()) {
            Long tenantId = userDetails.getTenantId();
            model.addAttribute("form", sessionForm);
            model.addAttribute("menus", menuRepository.findByTenantIdAndStatus(tenantId, Status.ACTIVE));
            model.addAttribute("categories", MenuCategory.values());
            model.addAttribute("currentStep", 3);
            model.addAttribute("errorMessage", "Please add at least one menu item");
            return "orders/wizard/step3";
        }

        sessionForm.setCurrentStep(4);
        session.setAttribute(ORDER_FORM_KEY, sessionForm);

        return redirectToStep(4);
    }

    @PostMapping("/wizard/step3/add-item")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addMenuItem(@RequestBody OrderMenuItemFormDTO item,
                                                           @AuthenticationPrincipal CustomUserDetails userDetails,
                                                           HttpSession session) {
        OrderFormDTO form = getFormFromSession(session);
        Map<String, Object> response = new HashMap<>();

        // Validate menu exists
        Menu menu = menuRepository.findById(item.getMenuId()).orElse(null);
        if (menu == null) {
            response.put("success", false);
            response.put("message", "Menu not found");
            return ResponseEntity.badRequest().body(response);
        }

        // Set display info
        item.setMenuCode(menu.getMenuCode());
        item.setMenuName(menu.getMenuCode()); // Will use translation in view
        item.setMenuCategory(menu.getCategory() != null ? menu.getCategory().name() : null);
        item.calculateSubtotal();

        form.addMenuItem(item);
        form.recalculateTotals();
        session.setAttribute(ORDER_FORM_KEY, form);

        response.put("success", true);
        response.put("menuItems", form.getMenuItems());
        response.put("menuSubtotal", form.getMenuSubtotal());
        response.put("grandTotal", form.getGrandTotal());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/wizard/step3/remove-item/{index}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeMenuItem(@PathVariable int index,
                                                              HttpSession session) {
        OrderFormDTO form = getFormFromSession(session);
        Map<String, Object> response = new HashMap<>();

        form.removeMenuItem(index);
        form.recalculateTotals();
        session.setAttribute(ORDER_FORM_KEY, form);

        response.put("success", true);
        response.put("menuItems", form.getMenuItems());
        response.put("menuSubtotal", form.getMenuSubtotal());
        response.put("grandTotal", form.getGrandTotal());

        return ResponseEntity.ok(response);
    }

    // ===== Wizard Step 4: Utilities =====

    @GetMapping("/wizard/step4")
    public String wizardStep4(@AuthenticationPrincipal CustomUserDetails userDetails,
                              HttpSession session,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        OrderFormDTO form = getFormFromSession(session);

        // Validate previous steps completed
        if (!form.isStep3Valid()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please complete step 3 first");
            return redirectToStep(3);
        }

        Long tenantId = userDetails.getTenantId();
        List<Utility> utilities = utilityRepository.findByTenantIdAndStatus(tenantId, Status.ACTIVE);

        model.addAttribute("form", form);
        model.addAttribute("utilities", utilities);
        model.addAttribute("currentStep", 4);

        return "orders/wizard/step4";
    }

    @PostMapping("/wizard/step4")
    public String processStep4(HttpSession session,
                               RedirectAttributes redirectAttributes) {
        OrderFormDTO sessionForm = getFormFromSession(session);

        // Utilities are optional, so step 4 is always valid if previous steps are valid
        sessionForm.setCurrentStep(5);
        session.setAttribute(ORDER_FORM_KEY, sessionForm);

        return redirectToStep(5);
    }

    @PostMapping("/wizard/step4/add-item")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addUtilityItem(@RequestBody OrderUtilityFormDTO item,
                                                              @AuthenticationPrincipal CustomUserDetails userDetails,
                                                              HttpSession session) {
        OrderFormDTO form = getFormFromSession(session);
        Map<String, Object> response = new HashMap<>();

        // Validate utility exists
        Utility utility = utilityRepository.findById(item.getUtilityId()).orElse(null);
        if (utility == null) {
            response.put("success", false);
            response.put("message", "Utility not found");
            return ResponseEntity.badRequest().body(response);
        }

        // Set display info
        item.setUtilityCode(utility.getUtilityCode());
        item.setUtilityName(utility.getUtilityCode()); // Will use translation in view
        item.calculateSubtotal();

        form.addUtility(item);
        form.recalculateTotals();
        session.setAttribute(ORDER_FORM_KEY, form);

        response.put("success", true);
        response.put("utilities", form.getUtilities());
        response.put("utilitySubtotal", form.getUtilitySubtotal());
        response.put("grandTotal", form.getGrandTotal());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/wizard/step4/remove-item/{index}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeUtilityItem(@PathVariable int index,
                                                                 HttpSession session) {
        OrderFormDTO form = getFormFromSession(session);
        Map<String, Object> response = new HashMap<>();

        form.removeUtility(index);
        form.recalculateTotals();
        session.setAttribute(ORDER_FORM_KEY, form);

        response.put("success", true);
        response.put("utilities", form.getUtilities());
        response.put("utilitySubtotal", form.getUtilitySubtotal());
        response.put("grandTotal", form.getGrandTotal());

        return ResponseEntity.ok(response);
    }

    // ===== Wizard Step 5: Pricing & Summary =====

    @GetMapping("/wizard/step5")
    public String wizardStep5(HttpSession session,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        OrderFormDTO form = getFormFromSession(session);

        // Validate previous steps completed
        if (!form.isStep3Valid()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please complete step 3 first");
            return redirectToStep(3);
        }

        form.recalculateTotals();

        model.addAttribute("form", form);
        model.addAttribute("currentStep", 5);

        return "orders/wizard/step5";
    }

    @PostMapping("/wizard/step5")
    public String processStep5(@ModelAttribute("form") OrderFormDTO form,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        OrderFormDTO sessionForm = getFormFromSession(session);
        Long tenantId = userDetails.getTenantId();
        Long userId = userDetails.getId();

        // Update pricing and notes
        sessionForm.setDiscountPercent(form.getDiscountPercent() != null ? form.getDiscountPercent() : BigDecimal.ZERO);
        sessionForm.setTaxPercent(form.getTaxPercent() != null ? form.getTaxPercent() : BigDecimal.ZERO);
        sessionForm.setNotes(form.getNotes());
        sessionForm.recalculateTotals();

        // Validate all steps
        if (!sessionForm.isAllStepsValid()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Order form is incomplete");
            return redirectToStep(1);
        }

        try {
            OrderDTO savedOrder;
            Long editingOrderId = (Long) session.getAttribute("editingOrderId");

            if (editingOrderId != null) {
                // Update existing order
                savedOrder = orderService.updateFromForm(editingOrderId, sessionForm, userId);
                redirectAttributes.addFlashAttribute("successMessage", "Order updated successfully");
            } else {
                // Create new order
                savedOrder = orderService.createFromForm(sessionForm, tenantId, userId);
                redirectAttributes.addFlashAttribute("successMessage", "Order created successfully");
            }

            // Clean up session
            session.removeAttribute(ORDER_FORM_KEY);
            session.removeAttribute("editingOrderId");

            return "redirect:/orders/" + savedOrder.getId();
        } catch (Exception e) {
            log.error("Failed to save order", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to save order: " + e.getMessage());
            return redirectToStep(5);
        }
    }

    @PostMapping("/wizard/step5/recalculate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> recalculateTotals(@RequestBody Map<String, BigDecimal> pricing,
                                                                  HttpSession session) {
        OrderFormDTO form = getFormFromSession(session);
        Map<String, Object> response = new HashMap<>();

        if (pricing.containsKey("discountPercent")) {
            form.setDiscountPercent(pricing.get("discountPercent"));
        }
        if (pricing.containsKey("taxPercent")) {
            form.setTaxPercent(pricing.get("taxPercent"));
        }

        form.recalculateTotals();
        session.setAttribute(ORDER_FORM_KEY, form);

        response.put("success", true);
        response.put("menuSubtotal", form.getMenuSubtotal());
        response.put("utilitySubtotal", form.getUtilitySubtotal());
        response.put("subtotal", form.getSubtotal());
        response.put("discountAmount", form.getDiscountAmount());
        response.put("taxAmount", form.getTaxAmount());
        response.put("grandTotal", form.getGrandTotal());

        return ResponseEntity.ok(response);
    }

    // ===== Wizard Navigation =====

    @GetMapping("/wizard/back/{step}")
    public String wizardBack(@PathVariable int step, HttpSession session) {
        OrderFormDTO form = getFormFromSession(session);
        int targetStep = Math.max(1, step - 1);
        form.setCurrentStep(targetStep);
        session.setAttribute(ORDER_FORM_KEY, form);
        return redirectToStep(targetStep);
    }

    @PostMapping("/wizard/cancel")
    public String cancelWizard(HttpSession session, RedirectAttributes redirectAttributes) {
        session.removeAttribute(ORDER_FORM_KEY);
        session.removeAttribute("editingOrderId");
        redirectAttributes.addFlashAttribute("infoMessage", "Order creation cancelled");
        return REDIRECT_ORDERS;
    }

    // ===== Workflow Actions =====

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER', 'STAFF')")
    public String submitOrder(@PathVariable Long id,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        try {
            orderService.submit(id, userDetails.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Order submitted for approval");
        } catch (Exception e) {
            log.error("Failed to submit order", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to submit order: " + e.getMessage());
        }
        return "redirect:/orders/" + id;
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER')")
    public String approveOrder(@PathVariable Long id,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            orderService.approve(id, userDetails.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Order approved");
        } catch (Exception e) {
            log.error("Failed to approve order", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to approve order: " + e.getMessage());
        }
        return "redirect:/orders/" + id;
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER')")
    public String rejectOrder(@PathVariable Long id,
                              @RequestParam String reason,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        try {
            orderService.reject(id, userDetails.getId(), reason);
            redirectAttributes.addFlashAttribute("successMessage", "Order rejected and returned to draft");
        } catch (Exception e) {
            log.error("Failed to reject order", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to reject order: " + e.getMessage());
        }
        return "redirect:/orders/" + id;
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER')")
    public String cancelOrder(@PathVariable Long id,
                              @RequestParam String reason,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        try {
            orderService.cancel(id, userDetails.getId(), reason);
            redirectAttributes.addFlashAttribute("successMessage", "Order cancelled");
        } catch (Exception e) {
            log.error("Failed to cancel order", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to cancel order: " + e.getMessage());
        }
        return "redirect:/orders/" + id;
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER')")
    public String startOrder(@PathVariable Long id,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        try {
            orderService.startProgress(id, userDetails.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Order started");
        } catch (Exception e) {
            log.error("Failed to start order", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to start order: " + e.getMessage());
        }
        return "redirect:/orders/" + id;
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER')")
    public String completeOrder(@PathVariable Long id,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        try {
            orderService.complete(id, userDetails.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Order completed");
        } catch (Exception e) {
            log.error("Failed to complete order", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to complete order: " + e.getMessage());
        }
        return "redirect:/orders/" + id;
    }

    // ===== Clone Order =====

    @PostMapping("/{id}/clone")
    public String cloneOrder(@PathVariable Long id,
                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newEventDate,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        try {
            OrderDTO clonedOrder = orderService.cloneOrder(id, newEventDate, userDetails.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Order cloned successfully");
            return "redirect:/orders/" + clonedOrder.getId();
        } catch (Exception e) {
            log.error("Failed to clone order", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to clone order: " + e.getMessage());
            return "redirect:/orders/" + id;
        }
    }

    // ===== Print Order =====

    @GetMapping("/{id}/print")
    public String printOrder(@PathVariable Long id,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        Long tenantId = userDetails.getTenantId();

        return orderService.findByIdWithDetails(id)
                .filter(order -> order.getTenantId().equals(tenantId))
                .map(order -> {
                    model.addAttribute("order", order);
                    return "orders/print";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Order not found");
                    return REDIRECT_ORDERS;
                });
    }

    // ===== AJAX Endpoints for Customer Search =====

    @GetMapping("/api/customers/search")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> searchCustomers(
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
                .limit(10)
                .map(c -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", c.getId());
                    map.put("code", c.getCustomerCode());
                    map.put("name", c.getName());
                    map.put("phone", c.getPhone());
                    map.put("email", c.getEmail());
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // ===== Helper Methods =====

    private OrderFormDTO getFormFromSession(HttpSession session) {
        OrderFormDTO form = (OrderFormDTO) session.getAttribute(ORDER_FORM_KEY);
        if (form == null) {
            form = new OrderFormDTO();
            form.setCurrentStep(1);
            session.setAttribute(ORDER_FORM_KEY, form);
        }
        return form;
    }

    private String redirectToStep(int step) {
        return "redirect:/orders/wizard/step" + step;
    }

    private OrderFormDTO convertToFormDTO(OrderDetailDTO order) {
        OrderFormDTO form = new OrderFormDTO();

        // Step 1: Customer
        form.setCustomerId(order.getCustomerId());
        form.setCustomerName(order.getCustomerName());
        form.setCustomerPhone(order.getCustomerPhone());
        form.setCustomerCode(order.getCustomerCode());
        form.setCreateNewCustomer(false);

        // Step 2: Event details
        form.setEventTypeId(order.getEventTypeId());
        form.setEventTypeName(order.getEventTypeName());
        form.setEventTypeCode(order.getEventTypeCode());
        form.setEventDate(order.getEventDate());
        form.setEventTime(order.getEventTime());
        form.setVenueName(order.getVenueName());
        form.setVenueAddress(order.getVenueAddress());
        form.setGuestCount(order.getGuestCount());

        // Step 3: Menu items
        if (order.getMenuItems() != null) {
            for (var menuItem : order.getMenuItems()) {
                OrderMenuItemFormDTO item = OrderMenuItemFormDTO.builder()
                        .menuId(menuItem.getMenuId())
                        .menuCode(menuItem.getMenuCode())
                        .menuName(menuItem.getMenuName())
                        .quantity(menuItem.getQuantity())
                        .pricePerItem(menuItem.getPricePerItem())
                        .subtotal(menuItem.getSubtotal())
                        .build();
                form.addMenuItem(item);
            }
        }

        // Step 4: Utilities
        if (order.getUtilities() != null) {
            for (var utility : order.getUtilities()) {
                OrderUtilityFormDTO item = OrderUtilityFormDTO.builder()
                        .utilityId(utility.getUtilityId())
                        .utilityCode(utility.getUtilityCode())
                        .utilityName(utility.getUtilityName())
                        .quantity(utility.getQuantity())
                        .pricePerUnit(utility.getPricePerUnit())
                        .subtotal(utility.getSubtotal())
                        .build();
                form.addUtility(item);
            }
        }

        // Step 5: Pricing
        form.setDiscountPercent(order.getDiscountPercent());
        form.setTaxPercent(order.getTaxPercent());
        form.setNotes(order.getNotes());
        form.recalculateTotals();

        return form;
    }
}
