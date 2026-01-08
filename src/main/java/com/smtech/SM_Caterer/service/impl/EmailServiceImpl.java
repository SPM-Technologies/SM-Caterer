package com.smtech.SM_Caterer.service.impl;

import com.smtech.SM_Caterer.domain.entity.EmailLog;
import com.smtech.SM_Caterer.domain.entity.Order;
import com.smtech.SM_Caterer.domain.entity.Payment;
import com.smtech.SM_Caterer.domain.entity.Tenant;
import com.smtech.SM_Caterer.domain.enums.EmailStatus;
import com.smtech.SM_Caterer.domain.enums.EmailType;
import com.smtech.SM_Caterer.domain.repository.EmailLogRepository;
import com.smtech.SM_Caterer.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Properties;

/**
 * Service implementation for email operations.
 *
 * Features:
 * - Per-tenant SMTP configuration
 * - Async email sending
 * - Email logging
 * - Thymeleaf templates
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender defaultMailSender;
    private final TemplateEngine templateEngine;
    private final EmailLogRepository emailLogRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    @Value("${spring.mail.host:}")
    private String defaultSmtpHost;

    @Override
    public boolean isEmailEnabled(Tenant tenant) {
        return tenant != null && tenant.isEmailConfigured();
    }

    @Override
    @Transactional
    public boolean sendTestEmail(Tenant tenant, String toEmail) {
        if (!isEmailEnabled(tenant)) {
            log.warn("Email not configured for tenant: {}", tenant.getId());
            return false;
        }

        try {
            JavaMailSender mailSender = createMailSender(tenant);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(tenant.getSmtpFromEmail(), tenant.getSmtpFromName());
            helper.setTo(toEmail);
            helper.setSubject("Test Email from " + tenant.getBusinessName());
            helper.setText(buildTestEmailBody(tenant), true);

            mailSender.send(message);

            // Log successful email
            createEmailLog(tenant, EmailType.TEST_EMAIL, toEmail, null,
                    "Test Email from " + tenant.getBusinessName(), null, null, EmailStatus.SENT);

            log.info("Test email sent successfully to {} for tenant {}", toEmail, tenant.getId());
            return true;

        } catch (Exception e) {
            log.error("Failed to send test email to {}: {}", toEmail, e.getMessage());
            createEmailLog(tenant, EmailType.TEST_EMAIL, toEmail, null,
                    "Test Email from " + tenant.getBusinessName(), null, null, EmailStatus.FAILED);
            return false;
        }
    }

    @Override
    @Async
    @Transactional
    public void sendOrderConfirmation(Order order) {
        Tenant tenant = order.getTenant();

        if (!isEmailEnabled(tenant)) {
            log.debug("Email not enabled for tenant: {}", tenant.getId());
            return;
        }

        if (order.getCustomer() == null || order.getCustomer().getEmail() == null) {
            log.debug("No customer email for order: {}", order.getOrderNumber());
            return;
        }

        String toEmail = order.getCustomer().getEmail();
        String toName = order.getCustomer().getName();
        String subject = "Order Confirmation - " + order.getOrderNumber();

        try {
            JavaMailSender mailSender = createMailSender(tenant);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(tenant.getSmtpFromEmail(), tenant.getSmtpFromName());
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(buildOrderConfirmationBody(order, tenant), true);

            mailSender.send(message);

            createEmailLog(tenant, EmailType.ORDER_CONFIRMATION, toEmail, toName, subject,
                    order.getId(), "ORDER", EmailStatus.SENT);

            log.info("Order confirmation email sent for order: {}", order.getOrderNumber());

        } catch (Exception e) {
            log.error("Failed to send order confirmation for {}: {}", order.getOrderNumber(), e.getMessage());
            createEmailLog(tenant, EmailType.ORDER_CONFIRMATION, toEmail, toName, subject,
                    order.getId(), "ORDER", EmailStatus.FAILED);
        }
    }

    @Override
    @Async
    @Transactional
    public void sendPaymentReceipt(Payment payment, String pdfPath) {
        Tenant tenant = payment.getTenant();
        Order order = payment.getOrder();

        if (!isEmailEnabled(tenant)) {
            log.debug("Email not enabled for tenant: {}", tenant.getId());
            return;
        }

        if (order.getCustomer() == null || order.getCustomer().getEmail() == null) {
            log.debug("No customer email for payment: {}", payment.getPaymentNumber());
            return;
        }

        String toEmail = order.getCustomer().getEmail();
        String toName = order.getCustomer().getName();
        String subject = "Payment Receipt - " + payment.getPaymentNumber();

        try {
            JavaMailSender mailSender = createMailSender(tenant);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(tenant.getSmtpFromEmail(), tenant.getSmtpFromName());
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(buildPaymentReceiptBody(payment, tenant), true);

            // Attach PDF receipt
            if (pdfPath != null) {
                File pdfFile = new File(pdfPath);
                if (pdfFile.exists()) {
                    FileSystemResource attachment = new FileSystemResource(pdfFile);
                    helper.addAttachment("Receipt-" + payment.getPaymentNumber() + ".pdf", attachment);
                }
            }

            mailSender.send(message);

            createEmailLog(tenant, EmailType.PAYMENT_RECEIPT, toEmail, toName, subject,
                    payment.getId(), "PAYMENT", EmailStatus.SENT);

            log.info("Payment receipt email sent for payment: {}", payment.getPaymentNumber());

        } catch (Exception e) {
            log.error("Failed to send payment receipt for {}: {}", payment.getPaymentNumber(), e.getMessage());
            createEmailLog(tenant, EmailType.PAYMENT_RECEIPT, toEmail, toName, subject,
                    payment.getId(), "PAYMENT", EmailStatus.FAILED);
        }
    }

    private JavaMailSender createMailSender(Tenant tenant) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(tenant.getSmtpHost());
        mailSender.setPort(tenant.getSmtpPort() != null ? tenant.getSmtpPort() : 587);
        mailSender.setUsername(tenant.getSmtpUsername());
        mailSender.setPassword(tenant.getSmtpPassword());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");

        if (tenant.getSmtpUseTls() != null && tenant.getSmtpUseTls()) {
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
        }

        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.writetimeout", "5000");

        return mailSender;
    }

    private void createEmailLog(Tenant tenant, EmailType emailType, String toEmail, String toName,
                                String subject, Long referenceId, String referenceType, EmailStatus status) {
        EmailLog emailLog = EmailLog.builder()
                .tenant(tenant)
                .emailType(emailType)
                .toEmail(toEmail)
                .toName(toName)
                .subject(subject)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .status(status)
                .build();

        if (status == EmailStatus.SENT) {
            emailLog.markAsSent();
        }

        emailLogRepository.save(emailLog);
    }

    private String buildTestEmailBody(Tenant tenant) {
        Context context = new Context();
        context.setVariable("businessName", tenant.getBusinessName());
        context.setVariable("message", "This is a test email to verify your email configuration is working correctly.");

        try {
            return templateEngine.process("email/test-email", context);
        } catch (Exception e) {
            log.warn("Template not found, using fallback HTML");
            return "<html><body><h2>Test Email</h2><p>This is a test email from " +
                    tenant.getBusinessName() + ".</p><p>Your email configuration is working correctly!</p></body></html>";
        }
    }

    private String buildOrderConfirmationBody(Order order, Tenant tenant) {
        Context context = new Context();
        context.setVariable("businessName", tenant.getBusinessName());
        context.setVariable("orderNumber", order.getOrderNumber());
        context.setVariable("customerName", order.getCustomer().getName());
        context.setVariable("eventDate", order.getEventDate() != null ?
                order.getEventDate().format(DATE_FORMATTER) : "-");
        context.setVariable("totalAmount", order.getTotalAmount() != null ?
                CURRENCY_FORMAT.format(order.getTotalAmount()) : "-");

        try {
            return templateEngine.process("email/order-confirmation", context);
        } catch (Exception e) {
            log.warn("Template not found, using fallback HTML");
            return buildFallbackOrderConfirmation(order, tenant);
        }
    }

    private String buildPaymentReceiptBody(Payment payment, Tenant tenant) {
        Context context = new Context();
        context.setVariable("businessName", tenant.getBusinessName());
        context.setVariable("paymentNumber", payment.getPaymentNumber());
        context.setVariable("customerName", payment.getCustomerName());
        context.setVariable("orderNumber", payment.getOrderNumber());
        context.setVariable("amount", CURRENCY_FORMAT.format(payment.getAmount()));
        context.setVariable("paymentDate", payment.getPaymentDate().format(DATE_FORMATTER));
        context.setVariable("paymentMethod", payment.getPaymentMethod().name());

        try {
            return templateEngine.process("email/payment-receipt", context);
        } catch (Exception e) {
            log.warn("Template not found, using fallback HTML");
            return buildFallbackPaymentReceipt(payment, tenant);
        }
    }

    private String buildFallbackOrderConfirmation(Order order, Tenant tenant) {
        return "<html><body>" +
                "<h2>Order Confirmation</h2>" +
                "<p>Dear " + order.getCustomer().getName() + ",</p>" +
                "<p>Thank you for your order with " + tenant.getBusinessName() + ".</p>" +
                "<p><strong>Order Number:</strong> " + order.getOrderNumber() + "</p>" +
                "<p><strong>Event Date:</strong> " + (order.getEventDate() != null ?
                order.getEventDate().format(DATE_FORMATTER) : "-") + "</p>" +
                "<p><strong>Total Amount:</strong> " + (order.getTotalAmount() != null ?
                CURRENCY_FORMAT.format(order.getTotalAmount()) : "-") + "</p>" +
                "<p>Thank you for choosing us!</p>" +
                "<p>Best regards,<br>" + tenant.getBusinessName() + "</p>" +
                "</body></html>";
    }

    private String buildFallbackPaymentReceipt(Payment payment, Tenant tenant) {
        return "<html><body>" +
                "<h2>Payment Receipt</h2>" +
                "<p>Dear " + payment.getCustomerName() + ",</p>" +
                "<p>We have received your payment. Thank you!</p>" +
                "<p><strong>Receipt Number:</strong> " + payment.getPaymentNumber() + "</p>" +
                "<p><strong>Order Number:</strong> " + payment.getOrderNumber() + "</p>" +
                "<p><strong>Amount:</strong> " + CURRENCY_FORMAT.format(payment.getAmount()) + "</p>" +
                "<p><strong>Payment Date:</strong> " + payment.getPaymentDate().format(DATE_FORMATTER) + "</p>" +
                "<p><strong>Payment Method:</strong> " + payment.getPaymentMethod().name() + "</p>" +
                "<p>Please find the PDF receipt attached.</p>" +
                "<p>Best regards,<br>" + tenant.getBusinessName() + "</p>" +
                "</body></html>";
    }
}
