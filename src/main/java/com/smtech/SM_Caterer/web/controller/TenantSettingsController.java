package com.smtech.SM_Caterer.web.controller;

import com.smtech.SM_Caterer.domain.entity.Tenant;
import com.smtech.SM_Caterer.domain.repository.TenantRepository;
import com.smtech.SM_Caterer.security.CustomUserDetails;
import com.smtech.SM_Caterer.service.EmailService;
import com.smtech.SM_Caterer.web.dto.EmailSettingsDTO;
import com.smtech.SM_Caterer.web.dto.PaymentSettingsDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

/**
 * Web Controller for Tenant Settings management.
 * Handles email and payment configuration for tenants.
 */
@Controller
@RequestMapping("/settings")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')")
@Slf4j
public class TenantSettingsController {

    private final TenantRepository tenantRepository;
    private final EmailService emailService;

    // ===== Email Settings =====

    @GetMapping("/email")
    public String emailSettings(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 Model model) {
        Long tenantId = userDetails.getTenantId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        EmailSettingsDTO settings = EmailSettingsDTO.builder()
                .emailEnabled(tenant.getEmailEnabled() != null && tenant.getEmailEnabled())
                .smtpHost(tenant.getSmtpHost())
                .smtpPort(tenant.getSmtpPort())
                .smtpUsername(tenant.getSmtpUsername())
                .smtpPassword("") // Don't show existing password
                .smtpFromEmail(tenant.getSmtpFromEmail())
                .smtpFromName(tenant.getSmtpFromName())
                .smtpUseTls(tenant.getSmtpUseTls() != null && tenant.getSmtpUseTls())
                .build();

        model.addAttribute("settings", settings);
        model.addAttribute("isConfigured", tenant.isEmailConfigured());

        return "settings/email";
    }

    @PostMapping("/email")
    public String saveEmailSettings(@Valid @ModelAttribute("settings") EmailSettingsDTO settings,
                                     BindingResult bindingResult,
                                     @AuthenticationPrincipal CustomUserDetails userDetails,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "settings/email";
        }

        Long tenantId = userDetails.getTenantId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        // Update settings
        tenant.setEmailEnabled(settings.isEmailEnabled());
        tenant.setSmtpHost(settings.getSmtpHost());
        tenant.setSmtpPort(settings.getSmtpPort());
        tenant.setSmtpUsername(settings.getSmtpUsername());

        // Only update password if provided
        if (settings.getSmtpPassword() != null && !settings.getSmtpPassword().isBlank()) {
            tenant.setSmtpPassword(settings.getSmtpPassword());
        }

        tenant.setSmtpFromEmail(settings.getSmtpFromEmail());
        tenant.setSmtpFromName(settings.getSmtpFromName());
        tenant.setSmtpUseTls(settings.isSmtpUseTls());

        tenantRepository.save(tenant);

        log.info("Email settings updated for tenant: {}", tenantId);
        redirectAttributes.addFlashAttribute("successMessage", "Email settings saved successfully");

        return "redirect:/settings/email";
    }

    @PostMapping("/email/test")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> testEmailSettings(@RequestBody Map<String, String> request,
                                                                  @AuthenticationPrincipal CustomUserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        Long tenantId = userDetails.getTenantId();

        String testEmail = request.get("testEmail");
        if (testEmail == null || testEmail.isBlank()) {
            response.put("success", false);
            response.put("message", "Test email address is required");
            return ResponseEntity.badRequest().body(response);
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        if (!tenant.isEmailConfigured()) {
            response.put("success", false);
            response.put("message", "Email is not configured. Please save settings first.");
            return ResponseEntity.badRequest().body(response);
        }

        boolean sent = emailService.sendTestEmail(tenant, testEmail);

        if (sent) {
            response.put("success", true);
            response.put("message", "Test email sent successfully to " + testEmail);
        } else {
            response.put("success", false);
            response.put("message", "Failed to send test email. Please check your SMTP settings.");
        }

        return ResponseEntity.ok(response);
    }

    // ===== Payment Settings =====

    @GetMapping("/payment")
    public String paymentSettings(@AuthenticationPrincipal CustomUserDetails userDetails,
                                   Model model) {
        Long tenantId = userDetails.getTenantId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        PaymentSettingsDTO settings = PaymentSettingsDTO.builder()
                .paymentEnabled(tenant.getPaymentEnabled() != null && tenant.getPaymentEnabled())
                .defaultUpiId(tenant.getDefaultUpiId())
                .upiPayeeName(tenant.getUpiPayeeName())
                .build();

        model.addAttribute("settings", settings);
        model.addAttribute("isUpiConfigured", tenant.isUpiConfigured());

        return "settings/payment";
    }

    @PostMapping("/payment")
    public String savePaymentSettings(@Valid @ModelAttribute("settings") PaymentSettingsDTO settings,
                                       BindingResult bindingResult,
                                       @AuthenticationPrincipal CustomUserDetails userDetails,
                                       Model model,
                                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "settings/payment";
        }

        Long tenantId = userDetails.getTenantId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        // Update settings
        tenant.setPaymentEnabled(settings.isPaymentEnabled());
        tenant.setDefaultUpiId(settings.getDefaultUpiId());
        tenant.setUpiPayeeName(settings.getUpiPayeeName());

        tenantRepository.save(tenant);

        log.info("Payment settings updated for tenant: {}", tenantId);
        redirectAttributes.addFlashAttribute("successMessage", "Payment settings saved successfully");

        return "redirect:/settings/payment";
    }
}
