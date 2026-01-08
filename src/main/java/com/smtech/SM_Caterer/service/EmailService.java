package com.smtech.SM_Caterer.service;

import com.smtech.SM_Caterer.domain.entity.Order;
import com.smtech.SM_Caterer.domain.entity.Payment;
import com.smtech.SM_Caterer.domain.entity.Tenant;

/**
 * Service interface for email operations.
 */
public interface EmailService {

    /**
     * Sends a test email to verify SMTP configuration.
     *
     * @param tenant     The tenant
     * @param toEmail    The recipient email
     * @return true if sent successfully
     */
    boolean sendTestEmail(Tenant tenant, String toEmail);

    /**
     * Sends order confirmation email.
     *
     * @param order The order
     */
    void sendOrderConfirmation(Order order);

    /**
     * Sends payment receipt email with PDF attachment.
     *
     * @param payment  The payment
     * @param pdfPath  Path to the PDF receipt
     */
    void sendPaymentReceipt(Payment payment, String pdfPath);

    /**
     * Checks if email is enabled and configured for tenant.
     *
     * @param tenant The tenant
     * @return true if email is enabled and configured
     */
    boolean isEmailEnabled(Tenant tenant);
}
