package com.smtech.SM_Caterer.service;

import com.smtech.SM_Caterer.domain.entity.Payment;

/**
 * Service interface for PDF receipt generation.
 */
public interface PdfReceiptService {

    /**
     * Generates a PDF receipt for a payment.
     *
     * @param payment The payment entity
     * @return Path to the generated PDF file
     */
    String generateReceipt(Payment payment);

    /**
     * Generates a PDF receipt as byte array.
     *
     * @param payment The payment entity
     * @return PDF content as byte array
     */
    byte[] generateReceiptBytes(Payment payment);
}
