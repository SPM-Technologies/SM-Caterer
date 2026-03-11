package com.smtech.SM_Caterer.web.controller;

import com.smtech.SM_Caterer.domain.entity.Tenant;
import com.smtech.SM_Caterer.domain.repository.TenantRepository;
import com.smtech.SM_Caterer.security.CustomUserDetails;
import com.smtech.SM_Caterer.service.EmailService;
import com.smtech.SM_Caterer.web.dto.BrandingSettingsDTO;
import com.smtech.SM_Caterer.web.dto.EmailSettingsDTO;
import com.smtech.SM_Caterer.web.dto.PaymentSettingsDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

    @Value("${app.upload.logo-dir:uploads/logos}")
    private String logoUploadDir;

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

    // ===== Branding Settings =====

    @GetMapping("/branding")
    public String brandingSettings(@AuthenticationPrincipal CustomUserDetails userDetails,
                                    Model model) {
        Long tenantId = userDetails.getTenantId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        BrandingSettingsDTO settings = BrandingSettingsDTO.builder()
                .displayName(tenant.getDisplayName())
                .tagline(tenant.getTagline())
                .primaryColor(tenant.getPrimaryColor())
                .logoPath(tenant.getLogoPath())
                .businessName(tenant.getBusinessName())
                .hasLogo(tenant.hasLogo())
                .build();

        model.addAttribute("settings", settings);
        model.addAttribute("pageTitle", "Branding Settings");

        return "settings/branding";
    }

    @PostMapping("/branding")
    public String saveBrandingSettings(@Valid @ModelAttribute("settings") BrandingSettingsDTO settings,
                                        BindingResult bindingResult,
                                        @RequestParam(value = "logoFile", required = false) MultipartFile logoFile,
                                        @AuthenticationPrincipal CustomUserDetails userDetails,
                                        Model model,
                                        RedirectAttributes redirectAttributes) {
        Long tenantId = userDetails.getTenantId();

        if (bindingResult.hasErrors()) {
            Tenant tenant = tenantRepository.findById(tenantId)
                    .orElseThrow(() -> new RuntimeException("Tenant not found"));
            settings.setBusinessName(tenant.getBusinessName());
            settings.setHasLogo(tenant.hasLogo());
            return "settings/branding";
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        // Handle logo upload
        if (logoFile != null && !logoFile.isEmpty()) {
            try {
                String logoPath = saveLogoFile(logoFile, tenantId);
                tenant.setLogoPath(logoPath);
            } catch (IOException e) {
                log.error("Failed to upload logo for tenant {}: {}", tenantId, e.getMessage());
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to upload logo: " + e.getMessage());
                return "redirect:/settings/branding";
            }
        }

        // Update settings
        tenant.setDisplayName(settings.getDisplayName());
        tenant.setTagline(settings.getTagline());
        tenant.setPrimaryColor(settings.getPrimaryColor());

        tenantRepository.save(tenant);

        log.info("Branding settings updated for tenant: {}", tenantId);
        redirectAttributes.addFlashAttribute("successMessage", "Branding settings saved successfully");

        return "redirect:/settings/branding";
    }

    @PostMapping("/branding/remove-logo")
    public String removeLogo(@AuthenticationPrincipal CustomUserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        Long tenantId = userDetails.getTenantId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        // Delete old logo file if exists
        if (tenant.getLogoPath() != null) {
            try {
                Path oldLogoPath = Paths.get(tenant.getLogoPath());
                Files.deleteIfExists(oldLogoPath);
            } catch (IOException e) {
                log.warn("Failed to delete old logo file: {}", e.getMessage());
            }
        }

        tenant.setLogoPath(null);
        tenantRepository.save(tenant);

        log.info("Logo removed for tenant: {}", tenantId);
        redirectAttributes.addFlashAttribute("successMessage", "Logo removed successfully");

        return "redirect:/settings/branding";
    }

    /**
     * Saves the uploaded logo file and returns the path.
     */
    private String saveLogoFile(MultipartFile file, Long tenantId) throws IOException {
        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IOException("Invalid file type. Only images are allowed.");
        }

        // Validate file size (max 2MB)
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new IOException("File size exceeds 2MB limit.");
        }

        // Create upload directory if not exists
        Path uploadPath = Paths.get(logoUploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFilename = "logo_" + tenantId + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;

        // Save file
        Path filePath = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        log.info("Logo saved for tenant {}: {}", tenantId, filePath);

        return filePath.toString();
    }
}
