package com.smtech.SM_Caterer.service.impl;

import com.smtech.SM_Caterer.domain.repository.PaymentRepository;
import com.smtech.SM_Caterer.service.PaymentNumberGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Service implementation for generating unique payment numbers.
 *
 * Format: PAY-YYYYMMDD-XXXX
 * - PAY: Fixed prefix
 * - YYYYMMDD: Current date
 * - XXXX: Sequential number (0001-9999) per tenant per day
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentNumberGeneratorServiceImpl implements PaymentNumberGeneratorService {

    private static final String PREFIX = "PAY";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final PaymentRepository paymentRepository;

    @Override
    @Transactional(readOnly = true)
    public String generatePaymentNumber(Long tenantId) {
        String datePrefix = PREFIX + "-" + LocalDate.now().format(DATE_FORMATTER) + "-";

        // Count existing payments with this prefix for today
        long count = paymentRepository.countByTenantIdAndPaymentNumberStartingWith(tenantId, datePrefix);

        // Generate next sequence number (1-based)
        String sequenceNumber = String.format("%04d", count + 1);

        String paymentNumber = datePrefix + sequenceNumber;
        log.debug("Generated payment number: {} for tenant: {}", paymentNumber, tenantId);

        return paymentNumber;
    }
}
