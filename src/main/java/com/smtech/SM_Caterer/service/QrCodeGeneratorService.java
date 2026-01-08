package com.smtech.SM_Caterer.service;

import java.math.BigDecimal;

/**
 * Service interface for UPI QR code generation.
 */
public interface QrCodeGeneratorService {

    /**
     * Generates a UPI QR code image file.
     *
     * @param upiId      The UPI ID (VPA) of the payee
     * @param payeeName  The name of the payee
     * @param amount     The payment amount
     * @param note       Transaction note/reference
     * @param orderId    The order ID (for file naming)
     * @param tenantId   The tenant ID
     * @return Path to the generated QR code image
     */
    String generateQrCode(String upiId, String payeeName, BigDecimal amount,
                          String note, Long orderId, Long tenantId);

    /**
     * Generates a UPI deep link URL.
     *
     * @param upiId      The UPI ID (VPA) of the payee
     * @param payeeName  The name of the payee
     * @param amount     The payment amount
     * @param note       Transaction note/reference
     * @return The UPI deep link URL
     */
    String generateUpiDeepLink(String upiId, String payeeName, BigDecimal amount, String note);

    /**
     * Generates QR code as Base64 encoded string for inline display.
     *
     * @param upiId      The UPI ID (VPA) of the payee
     * @param payeeName  The name of the payee
     * @param amount     The payment amount
     * @param note       Transaction note/reference
     * @return Base64 encoded PNG image
     */
    String generateQrCodeBase64(String upiId, String payeeName, BigDecimal amount, String note);
}
