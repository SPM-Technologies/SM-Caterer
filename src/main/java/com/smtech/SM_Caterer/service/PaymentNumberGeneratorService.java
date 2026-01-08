package com.smtech.SM_Caterer.service;

/**
 * Service interface for generating unique payment numbers.
 */
public interface PaymentNumberGeneratorService {

    /**
     * Generates a unique payment number for the tenant.
     * Format: PAY-YYYYMMDD-XXXX (e.g., PAY-20260108-0001)
     *
     * @param tenantId The tenant ID
     * @return The generated payment number
     */
    String generatePaymentNumber(Long tenantId);
}
