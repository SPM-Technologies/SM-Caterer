package com.smtech.SM_Caterer.web.controller;

import com.smtech.SM_Caterer.domain.enums.PaymentMethod;
import com.smtech.SM_Caterer.domain.enums.PaymentStatus;
import com.smtech.SM_Caterer.exception.InvalidOperationException;
import com.smtech.SM_Caterer.security.CustomUserDetails;
import com.smtech.SM_Caterer.service.OrderService;
import com.smtech.SM_Caterer.service.PaymentService;
import com.smtech.SM_Caterer.service.QrCodeGeneratorService;
import com.smtech.SM_Caterer.service.dto.OrderDetailDTO;
import com.smtech.SM_Caterer.service.dto.PaymentDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
import java.util.Map;

/**
 * Web Controller for Payment management.
 * Handles payment list, recording payments, receipt downloads, and email sending.
 */
@Controller
@RequestMapping("/payments")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER', 'STAFF')")
@Slf4j
public class PaymentWebController {

    private static final String REDIRECT_PAYMENTS = "redirect:/payments";

    private final PaymentService paymentService;
    private final OrderService orderService;
    private final QrCodeGeneratorService qrCodeGeneratorService;

    // ===== Payment List =====

    @GetMapping
    public String list(@AuthenticationPrincipal CustomUserDetails userDetails,
                       @RequestParam(required = false) Long orderId,
                       @RequestParam(required = false) PaymentStatus status,
                       @RequestParam(required = false) PaymentMethod method,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "20") int size,
                       @RequestParam(defaultValue = "paymentDate") String sortBy,
                       @RequestParam(defaultValue = "desc") String sortDir,
                       Model model) {
        Long tenantId = userDetails.getTenantId();

        size = Math.min(size, 100);
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<PaymentDTO> paymentPage = paymentService.searchPayments(
                tenantId, orderId, status, method, dateFrom, dateTo, pageable);

        model.addAttribute("payments", paymentPage.getContent());
        model.addAttribute("statuses", PaymentStatus.values());
        model.addAttribute("methods", PaymentMethod.values());
        model.addAttribute("orderId", orderId);
        model.addAttribute("status", status);
        model.addAttribute("method", method);
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", paymentPage.getTotalPages());
        model.addAttribute("totalItems", paymentPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);

        log.debug("Listed {} payments for tenant {} (page {}/{})",
                paymentPage.getNumberOfElements(), tenantId, page + 1, paymentPage.getTotalPages());

        return "payments/list";
    }

    // ===== View Payment =====

    @GetMapping("/{id}")
    public String view(@PathVariable Long id,
                       @AuthenticationPrincipal CustomUserDetails userDetails,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        Long tenantId = userDetails.getTenantId();

        return paymentService.findByIdAndTenantId(id, tenantId)
                .map(payment -> {
                    model.addAttribute("payment", payment);
                    return "payments/view";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Payment not found");
                    return REDIRECT_PAYMENTS;
                });
    }

    // ===== New Payment Form =====

    @GetMapping("/new")
    public String newPaymentForm(@RequestParam(required = false) Long orderId,
                                  @AuthenticationPrincipal CustomUserDetails userDetails,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        Long tenantId = userDetails.getTenantId();

        PaymentDTO payment = new PaymentDTO();
        payment.setPaymentDate(LocalDate.now());
        payment.setPaymentMethod(PaymentMethod.CASH);
        payment.setStatus(PaymentStatus.COMPLETED);

        if (orderId != null) {
            OrderDetailDTO order = orderService.findByIdWithDetails(orderId)
                    .filter(o -> o.getTenantId().equals(tenantId))
                    .orElse(null);

            if (order == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Order not found");
                return REDIRECT_PAYMENTS;
            }

            payment.setOrderId(orderId);
            payment.setOrderNumber(order.getOrderNumber());

            // Calculate balance due
            BigDecimal balance = paymentService.calculateBalanceDue(orderId);
            model.addAttribute("order", order);
            model.addAttribute("balanceDue", balance);
            model.addAttribute("totalPaid", paymentService.calculateTotalPaidForOrder(orderId));
        }

        model.addAttribute("payment", payment);
        model.addAttribute("methods", PaymentMethod.values());
        model.addAttribute("statuses", PaymentStatus.values());

        return "payments/form";
    }

    // ===== Create Payment =====

    @PostMapping
    public String createPayment(@Valid @ModelAttribute("payment") PaymentDTO payment,
                                 BindingResult bindingResult,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        Long tenantId = userDetails.getTenantId();

        if (bindingResult.hasErrors()) {
            model.addAttribute("methods", PaymentMethod.values());
            model.addAttribute("statuses", PaymentStatus.values());
            if (payment.getOrderId() != null) {
                orderService.findByIdWithDetails(payment.getOrderId())
                        .ifPresent(order -> {
                            model.addAttribute("order", order);
                            model.addAttribute("balanceDue", paymentService.calculateBalanceDue(payment.getOrderId()));
                            model.addAttribute("totalPaid", paymentService.calculateTotalPaidForOrder(payment.getOrderId()));
                        });
            }
            return "payments/form";
        }

        try {
            PaymentDTO created = paymentService.createPaymentWithWorkflow(payment);
            redirectAttributes.addFlashAttribute("successMessage", "Payment recorded successfully");
            return "redirect:/payments/" + created.getId();
        } catch (InvalidOperationException e) {
            log.error("Failed to create payment: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            model.addAttribute("methods", PaymentMethod.values());
            model.addAttribute("statuses", PaymentStatus.values());
            return "payments/form";
        }
    }

    // ===== Download Receipt PDF =====

    @GetMapping("/{id}/receipt")
    public ResponseEntity<byte[]> downloadReceipt(@PathVariable Long id,
                                                   @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long tenantId = userDetails.getTenantId();

        try {
            byte[] pdfBytes = paymentService.downloadReceipt(id, tenantId);

            PaymentDTO payment = paymentService.findByIdAndTenantId(id, tenantId)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            String filename = "Receipt-" + payment.getPaymentNumber() + ".pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (Exception e) {
            log.error("Failed to download receipt for payment {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // ===== Resend Email =====

    @PostMapping("/{id}/resend-email")
    public String resendEmail(@PathVariable Long id,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        Long tenantId = userDetails.getTenantId();

        boolean sent = paymentService.resendReceiptEmail(id, tenantId);

        if (sent) {
            redirectAttributes.addFlashAttribute("successMessage", "Email sent successfully");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to send email. Please check email configuration.");
        }

        return "redirect:/payments/" + id;
    }

    // ===== UPI QR Code =====

    @GetMapping("/qrcode")
    public String qrCodePage(@RequestParam Long orderId,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        Long tenantId = userDetails.getTenantId();

        return orderService.findByIdWithDetails(orderId)
                .filter(order -> order.getTenantId().equals(tenantId))
                .map(order -> {
                    BigDecimal balance = paymentService.calculateBalanceDue(orderId);

                    model.addAttribute("order", order);
                    model.addAttribute("balanceDue", balance);
                    model.addAttribute("totalPaid", paymentService.calculateTotalPaidForOrder(orderId));

                    return "payments/qrcode";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Order not found");
                    return REDIRECT_PAYMENTS;
                });
    }

    @PostMapping("/qrcode/generate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> generateQrCode(@RequestBody Map<String, Object> request,
                                                               @AuthenticationPrincipal CustomUserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();

        try {
            String upiId = (String) request.get("upiId");
            String payeeName = (String) request.get("payeeName");
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String note = (String) request.get("note");

            String qrCodeBase64 = qrCodeGeneratorService.generateQrCodeBase64(upiId, payeeName, amount, note);
            String upiLink = qrCodeGeneratorService.generateUpiDeepLink(upiId, payeeName, amount, note);

            response.put("success", true);
            response.put("qrCode", qrCodeBase64);
            response.put("upiLink", upiLink);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to generate QR code: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Failed to generate QR code");
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ===== Order Payments =====

    @GetMapping("/order/{orderId}")
    public String orderPayments(@PathVariable Long orderId,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        Long tenantId = userDetails.getTenantId();

        return orderService.findByIdWithDetails(orderId)
                .filter(order -> order.getTenantId().equals(tenantId))
                .map(order -> {
                    model.addAttribute("order", order);
                    model.addAttribute("payments", paymentService.findByOrderId(orderId));
                    model.addAttribute("totalPaid", paymentService.calculateTotalPaidForOrder(orderId));
                    model.addAttribute("balanceDue", paymentService.calculateBalanceDue(orderId));
                    return "payments/order-payments";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Order not found");
                    return REDIRECT_PAYMENTS;
                });
    }
}
