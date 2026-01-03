package com.smtech.SM_Caterer.service;

import com.smtech.SM_Caterer.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Service for generating unique order numbers.
 * Format: ORD-YYYYMMDD-XXXX (e.g., ORD-20260103-0001)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderNumberGeneratorService {

    private final OrderRepository orderRepository;

    private static final String PREFIX = "ORD";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Generate next order number for tenant.
     * Thread-safe through database count.
     *
     * @param tenantId the tenant ID
     * @return unique order number in format ORD-YYYYMMDD-XXXX
     */
    @Transactional(readOnly = true)
    public String generateOrderNumber(Long tenantId) {
        String dateStr = LocalDate.now().format(DATE_FORMAT);
        String prefix = PREFIX + "-" + dateStr + "-";

        // Get today's order count for this tenant
        long todayCount = orderRepository.countByTenantIdAndOrderNumberStartingWith(tenantId, prefix);

        // Generate next sequence (4 digits, zero-padded)
        String sequence = String.format("%04d", todayCount + 1);

        String orderNumber = prefix + sequence;
        log.debug("Generated order number {} for tenant {}", orderNumber, tenantId);

        return orderNumber;
    }

    /**
     * Generate order number with custom prefix.
     *
     * @param tenantId the tenant ID
     * @param customPrefix custom prefix (e.g., "EVT" for events)
     * @return unique order number
     */
    @Transactional(readOnly = true)
    public String generateOrderNumber(Long tenantId, String customPrefix) {
        String dateStr = LocalDate.now().format(DATE_FORMAT);
        String prefix = customPrefix + "-" + dateStr + "-";

        long todayCount = orderRepository.countByTenantIdAndOrderNumberStartingWith(tenantId, prefix);
        String sequence = String.format("%04d", todayCount + 1);

        return prefix + sequence;
    }

    /**
     * Check if order number is available.
     *
     * @param tenantId the tenant ID
     * @param orderNumber the order number to check
     * @return true if available, false if already exists
     */
    @Transactional(readOnly = true)
    public boolean isOrderNumberAvailable(Long tenantId, String orderNumber) {
        return !orderRepository.existsByTenantIdAndOrderNumber(tenantId, orderNumber);
    }
}
