package com.smtech.SM_Caterer.service.impl;

import com.smtech.SM_Caterer.domain.entity.Customer;
import com.smtech.SM_Caterer.domain.entity.Material;
import com.smtech.SM_Caterer.domain.entity.Order;
import com.smtech.SM_Caterer.domain.entity.Payment;
import com.smtech.SM_Caterer.domain.enums.OrderStatus;
import com.smtech.SM_Caterer.domain.enums.PaymentMethod;
import com.smtech.SM_Caterer.domain.enums.PaymentStatus;
import com.smtech.SM_Caterer.domain.enums.Status;
import com.smtech.SM_Caterer.domain.repository.CustomerRepository;
import com.smtech.SM_Caterer.domain.repository.MaterialRepository;
import com.smtech.SM_Caterer.domain.repository.OrderRepository;
import com.smtech.SM_Caterer.domain.repository.PaymentRepository;
import com.smtech.SM_Caterer.service.ReportService;
import com.smtech.SM_Caterer.service.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation for Report operations.
 * Provides filtered report data for various entities.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final MaterialRepository materialRepository;
    private final CustomerRepository customerRepository;

    // ===== Order Reports =====

    @Override
    public Page<OrderReportDTO> getOrderReport(Long tenantId, OrderStatus status, Long customerId,
                                                Long eventTypeId, LocalDate fromDate, LocalDate toDate,
                                                Pageable pageable) {
        log.debug("Getting order report for tenant: {}, status: {}, dateRange: {} to {}",
                tenantId, status, fromDate, toDate);

        Page<Order> orders = orderRepository.findOrdersForReport(
                tenantId, status, customerId, eventTypeId, fromDate, toDate, pageable);

        return orders.map(this::mapToOrderReportDTO);
    }

    @Override
    public List<OrderReportDTO> getOrderReportForExport(Long tenantId, OrderStatus status, Long customerId,
                                                         Long eventTypeId, LocalDate fromDate, LocalDate toDate) {
        // Get all data without pagination for export
        Page<Order> orders = orderRepository.findOrdersForReport(
                tenantId, status, customerId, eventTypeId, fromDate, toDate, Pageable.unpaged());

        return orders.getContent().stream()
                .map(this::mapToOrderReportDTO)
                .collect(Collectors.toList());
    }

    // ===== Payment Reports =====

    @Override
    public Page<PaymentReportDTO> getPaymentReport(Long tenantId, PaymentStatus status, PaymentMethod method,
                                                    Long customerId, LocalDate fromDate, LocalDate toDate,
                                                    Pageable pageable) {
        log.debug("Getting payment report for tenant: {}, status: {}, method: {}",
                tenantId, status, method);

        Page<Payment> payments = paymentRepository.findPaymentsForReport(
                tenantId, status, method, customerId, fromDate, toDate, pageable);

        return payments.map(this::mapToPaymentReportDTO);
    }

    @Override
    public List<PaymentReportDTO> getPaymentReportForExport(Long tenantId, PaymentStatus status, PaymentMethod method,
                                                             Long customerId, LocalDate fromDate, LocalDate toDate) {
        Page<Payment> payments = paymentRepository.findPaymentsForReport(
                tenantId, status, method, customerId, fromDate, toDate, Pageable.unpaged());

        return payments.getContent().stream()
                .map(this::mapToPaymentReportDTO)
                .collect(Collectors.toList());
    }

    // ===== Stock Reports =====

    @Override
    public Page<StockReportDTO> getStockReport(Long tenantId, Long materialGroupId, Status status,
                                                String stockStatus, Pageable pageable) {
        log.debug("Getting stock report for tenant: {}, groupId: {}, status: {}, stockStatus: {}",
                tenantId, materialGroupId, status, stockStatus);

        if (stockStatus != null && !stockStatus.isEmpty()) {
            // Filter by stock status
            List<Material> materials = materialRepository.findByStockStatus(tenantId, stockStatus);
            List<StockReportDTO> dtos = materials.stream()
                    .map(this::mapToStockReportDTO)
                    .collect(Collectors.toList());

            // Manual pagination
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), dtos.size());

            if (start >= dtos.size()) {
                return new PageImpl<>(Collections.emptyList(), pageable, dtos.size());
            }

            return new PageImpl<>(dtos.subList(start, end), pageable, dtos.size());
        }

        Page<Material> materials = materialRepository.findMaterialsForReport(
                tenantId, materialGroupId, status, pageable);

        return materials.map(this::mapToStockReportDTO);
    }

    @Override
    public List<StockReportDTO> getStockReportForExport(Long tenantId, Long materialGroupId, Status status,
                                                         String stockStatus) {
        if (stockStatus != null && !stockStatus.isEmpty()) {
            List<Material> materials = materialRepository.findByStockStatus(tenantId, stockStatus);
            return materials.stream()
                    .map(this::mapToStockReportDTO)
                    .collect(Collectors.toList());
        }

        List<Material> materials = materialRepository.findAllForStockReport(tenantId);
        return materials.stream()
                .filter(m -> materialGroupId == null ||
                        (m.getMaterialGroup() != null && m.getMaterialGroup().getId().equals(materialGroupId)))
                .filter(m -> status == null || m.getStatus() == status)
                .map(this::mapToStockReportDTO)
                .collect(Collectors.toList());
    }

    // ===== Customer Reports =====

    @Override
    public Page<CustomerReportDTO> getCustomerReport(Long tenantId, Status status, Pageable pageable) {
        log.debug("Getting customer report for tenant: {}, status: {}", tenantId, status);

        Page<Customer> customers = customerRepository.findCustomersForReport(tenantId, status, pageable);

        // Get order statistics for all customers
        Map<Long, Object[]> customerStats = getCustomerOrderStatsMap(tenantId);

        return customers.map(c -> mapToCustomerReportDTO(c, customerStats.get(c.getId())));
    }

    @Override
    public List<CustomerReportDTO> getCustomerReportForExport(Long tenantId, Status status) {
        Page<Customer> customers = customerRepository.findCustomersForReport(tenantId, status, Pageable.unpaged());
        Map<Long, Object[]> customerStats = getCustomerOrderStatsMap(tenantId);

        return customers.getContent().stream()
                .map(c -> mapToCustomerReportDTO(c, customerStats.get(c.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public List<CustomerReportDTO> getCustomersWithPendingBalance(Long tenantId) {
        List<Object[]> results = customerRepository.getCustomersWithPendingBalance(tenantId);
        Map<Long, Object[]> customerStats = getCustomerOrderStatsMap(tenantId);

        return results.stream()
                .map(row -> {
                    Customer customer = (Customer) row[0];
                    BigDecimal pendingBalance = (BigDecimal) row[1];
                    Object[] stats = customerStats.get(customer.getId());
                    CustomerReportDTO dto = mapToCustomerReportDTO(customer, stats);
                    dto.setTotalBalance(pendingBalance);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // ===== Private Mapping Methods =====

    private OrderReportDTO mapToOrderReportDTO(Order order) {
        String eventTypeName = null;
        if (order.getEventType() != null) {
            eventTypeName = order.getEventType().getEventCode();
            if (order.getEventType().getTranslations() != null &&
                !order.getEventType().getTranslations().isEmpty()) {
                eventTypeName = order.getEventType().getTranslations().stream()
                        .filter(t -> t.getLanguageCode() != null && "en".equals(t.getLanguageCode().name()))
                        .findFirst()
                        .map(t -> t.getEventName())
                        .orElse(order.getEventType().getEventCode());
            }
        }

        String createdByName = null;
        if (order.getCreatedByUser() != null) {
            createdByName = order.getCreatedByUser().getFullName();
        }

        return OrderReportDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerName(order.getCustomer() != null ? order.getCustomer().getName() : null)
                .customerPhone(order.getCustomer() != null ? order.getCustomer().getPhone() : null)
                .customerEmail(order.getCustomer() != null ? order.getCustomer().getEmail() : null)
                .eventDate(order.getEventDate())
                .eventTypeName(eventTypeName)
                .guestCount(order.getGuestCount())
                .menuSubtotal(order.getMenuSubtotal())
                .utilitySubtotal(order.getUtilitySubtotal())
                .grandTotal(order.getGrandTotal())
                .advanceAmount(order.getAdvanceAmount())
                .balanceAmount(order.getBalanceAmount())
                .status(order.getStatus() != null ? order.getStatus().name() : null)
                .createdAt(order.getCreatedAt())
                .createdByName(createdByName)
                .build();
    }

    private PaymentReportDTO mapToPaymentReportDTO(Payment payment) {
        String customerName = null;
        String orderNumber = null;

        if (payment.getOrder() != null) {
            orderNumber = payment.getOrder().getOrderNumber();
            if (payment.getOrder().getCustomer() != null) {
                customerName = payment.getOrder().getCustomer().getName();
            }
        }

        String createdByName = null;
        if (payment.getCreatedBy() != null) {
            // Note: Would need to load user for actual name
            createdByName = "User #" + payment.getCreatedBy();
        }

        return PaymentReportDTO.builder()
                .id(payment.getId())
                .paymentNumber(payment.getPaymentNumber())
                .orderNumber(orderNumber)
                .customerName(customerName)
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : null)
                .transactionReference(payment.getTransactionReference())
                .upiId(payment.getUpiId())
                .paymentDate(payment.getPaymentDate())
                .status(payment.getStatus() != null ? payment.getStatus().name() : null)
                .notes(payment.getNotes())
                .createdAt(payment.getCreatedAt())
                .createdByName(createdByName)
                .build();
    }

    private StockReportDTO mapToStockReportDTO(Material material) {
        BigDecimal currentStock = material.getCurrentStock() != null ? material.getCurrentStock() : BigDecimal.ZERO;
        BigDecimal minimumStock = material.getMinimumStock() != null ? material.getMinimumStock() : BigDecimal.ZERO;
        BigDecimal costPerUnit = material.getCostPerUnit() != null ? material.getCostPerUnit() : BigDecimal.ZERO;
        BigDecimal totalValue = currentStock.multiply(costPerUnit);

        // Determine stock status
        String stockStatus;
        if (currentStock.compareTo(BigDecimal.ZERO) <= 0) {
            stockStatus = "OUT_OF_STOCK";
        } else if (currentStock.compareTo(minimumStock) < 0) {
            stockStatus = "LOW_STOCK";
        } else {
            stockStatus = "IN_STOCK";
        }

        String groupName = null;
        if (material.getMaterialGroup() != null) {
            groupName = material.getMaterialGroup().getGroupCode();
            if (material.getMaterialGroup().getTranslations() != null &&
                !material.getMaterialGroup().getTranslations().isEmpty()) {
                groupName = material.getMaterialGroup().getTranslations().stream()
                        .filter(t -> t.getLanguageCode() != null && "en".equals(t.getLanguageCode().name()))
                        .findFirst()
                        .map(t -> t.getGroupName())
                        .orElse(material.getMaterialGroup().getGroupCode());
            }
        }

        String unitName = null;
        String unitSymbol = null;
        if (material.getUnit() != null) {
            unitSymbol = material.getUnit().getUnitCode();
            unitName = material.getUnit().getUnitCode();
            if (material.getUnit().getTranslations() != null &&
                !material.getUnit().getTranslations().isEmpty()) {
                unitName = material.getUnit().getTranslations().stream()
                        .filter(t -> t.getLanguageCode() != null && "en".equals(t.getLanguageCode().name()))
                        .findFirst()
                        .map(t -> t.getUnitName())
                        .orElse(material.getUnit().getUnitCode());
            }
        }

        String materialName = material.getMaterialCode();
        if (material.getTranslations() != null && !material.getTranslations().isEmpty()) {
            materialName = material.getTranslations().stream()
                    .filter(t -> t.getLanguageCode() != null && "en".equals(t.getLanguageCode().name()))
                    .findFirst()
                    .map(t -> t.getMaterialName())
                    .orElse(material.getMaterialCode());
        }

        return StockReportDTO.builder()
                .materialId(material.getId())
                .materialCode(material.getMaterialCode())
                .materialName(materialName)
                .groupName(groupName)
                .unitName(unitName)
                .unitSymbol(unitSymbol)
                .currentStock(currentStock)
                .minimumStock(minimumStock)
                .costPerUnit(costPerUnit)
                .totalValue(totalValue)
                .stockStatus(stockStatus)
                .isActive(material.getStatus() == Status.ACTIVE)
                .build();
    }

    private CustomerReportDTO mapToCustomerReportDTO(Customer customer, Object[] stats) {
        Long totalOrders = 0L;
        BigDecimal totalValue = BigDecimal.ZERO;
        BigDecimal totalPaid = BigDecimal.ZERO;
        BigDecimal totalBalance = BigDecimal.ZERO;

        if (stats != null) {
            totalOrders = stats[1] != null ? ((Number) stats[1]).longValue() : 0L;
            totalValue = stats[2] != null ? (BigDecimal) stats[2] : BigDecimal.ZERO;
            totalPaid = stats[3] != null ? (BigDecimal) stats[3] : BigDecimal.ZERO;
            totalBalance = stats[4] != null ? (BigDecimal) stats[4] : BigDecimal.ZERO;
        }

        return CustomerReportDTO.builder()
                .id(customer.getId())
                .name(customer.getName())
                .phone(customer.getPhone())
                .email(customer.getEmail())
                .address(customer.getAddress())
                .totalOrders(totalOrders)
                .totalValue(totalValue)
                .totalPaid(totalPaid)
                .totalBalance(totalBalance)
                .isActive(customer.getStatus() == Status.ACTIVE)
                .build();
    }

    private Map<Long, Object[]> getCustomerOrderStatsMap(Long tenantId) {
        List<Object[]> stats = customerRepository.getCustomerOrderStats(tenantId);
        Map<Long, Object[]> map = new HashMap<>();
        for (Object[] row : stats) {
            Long customerId = ((Number) row[0]).longValue();
            map.put(customerId, row);
        }
        return map;
    }
}
