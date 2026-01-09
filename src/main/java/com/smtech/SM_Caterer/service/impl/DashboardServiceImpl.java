package com.smtech.SM_Caterer.service.impl;

import com.smtech.SM_Caterer.domain.entity.Material;
import com.smtech.SM_Caterer.domain.entity.Order;
import com.smtech.SM_Caterer.domain.enums.OrderStatus;
import com.smtech.SM_Caterer.domain.repository.CustomerRepository;
import com.smtech.SM_Caterer.domain.repository.MaterialRepository;
import com.smtech.SM_Caterer.domain.repository.OrderRepository;
import com.smtech.SM_Caterer.domain.repository.PaymentRepository;
import com.smtech.SM_Caterer.service.DashboardService;
import com.smtech.SM_Caterer.service.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation for Dashboard operations.
 * Provides aggregated metrics and statistics for the dashboard view.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final MaterialRepository materialRepository;
    private final CustomerRepository customerRepository;

    private static final int DEFAULT_RECENT_ORDERS_LIMIT = 10;
    private static final int DEFAULT_UPCOMING_EVENTS_DAYS = 7;
    private static final int MONTHS_FOR_CHART = 6;

    @Override
    public DashboardMetricsDTO getDashboardMetrics(Long tenantId) {
        log.debug("Building dashboard metrics for tenant: {}", tenantId);

        LocalDate today = LocalDate.now();
        int currentMonth = today.getMonthValue();
        int currentYear = today.getYear();

        // Build metrics
        DashboardMetricsDTO metrics = DashboardMetricsDTO.builder()
                // Today's metrics
                .todayOrderCount(getOrDefault(orderRepository.countOrdersByCreatedDate(tenantId, today), 0L))
                .todayOrderValue(getOrDefault(orderRepository.sumOrderTotalByCreatedDate(tenantId, today), BigDecimal.ZERO))
                .todayPaymentCount(getOrDefault(paymentRepository.countPaymentsByDate(tenantId, today), 0L))
                .todayPaymentValue(getOrDefault(paymentRepository.sumTodayPayments(tenantId, today), BigDecimal.ZERO))

                // Monthly metrics
                .monthOrderCount(getOrDefault(orderRepository.countOrdersByMonth(tenantId, currentMonth, currentYear), 0L))
                .monthRevenue(getOrDefault(orderRepository.sumOrderTotalByMonth(tenantId, currentMonth, currentYear), BigDecimal.ZERO))
                .monthExpectedRevenue(calculateMonthExpectedRevenue(tenantId, currentMonth, currentYear))

                // Pending amounts
                .totalPendingPayments(getOrDefault(orderRepository.sumPendingBalance(tenantId), BigDecimal.ZERO))
                .pendingOrderCount(getOrDefault(orderRepository.countOrdersWithPendingBalance(tenantId), 0L))

                // Alerts
                .lowStockItemCount(getOrDefault(materialRepository.countLowStockMaterials(tenantId), 0L))
                .upcomingEventsCount(countUpcomingEvents(tenantId, DEFAULT_UPCOMING_EVENTS_DAYS))

                // Lists
                .recentOrders(getRecentOrders(tenantId, DEFAULT_RECENT_ORDERS_LIMIT))
                .upcomingEvents(getUpcomingEvents(tenantId, DEFAULT_UPCOMING_EVENTS_DAYS))
                .lowStockAlerts(getLowStockAlerts(tenantId))

                // Chart data
                .monthlyRevenueData(getMonthlyRevenueData(tenantId))
                .orderStatusDistribution(getOrderStatusDistribution(tenantId))
                .paymentMethodDistribution(getPaymentMethodDistribution(tenantId))
                .build();

        log.debug("Dashboard metrics built successfully for tenant: {}", tenantId);
        return metrics;
    }

    @Override
    public List<OrderSummaryDTO> getRecentOrders(Long tenantId, int limit) {
        List<Order> orders = orderRepository.findRecentOrders(tenantId, PageRequest.of(0, limit));
        return orders.stream()
                .map(this::mapToOrderSummary)
                .collect(Collectors.toList());
    }

    @Override
    public List<UpcomingEventDTO> getUpcomingEvents(Long tenantId, int daysAhead) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(daysAhead);

        List<Order> orders = orderRepository.findUpcomingEventsInRange(tenantId, today, endDate);
        return orders.stream()
                .map(this::mapToUpcomingEvent)
                .collect(Collectors.toList());
    }

    @Override
    public List<LowStockAlertDTO> getLowStockAlerts(Long tenantId) {
        List<Material> lowStockMaterials = materialRepository.findLowStockMaterials(tenantId);
        return lowStockMaterials.stream()
                .map(this::mapToLowStockAlert)
                .sorted(Comparator.comparing(LowStockAlertDTO::getSeverity)
                        .thenComparing(a -> a.getShortfall().negate()))
                .limit(20) // Limit to 20 alerts
                .collect(Collectors.toList());
    }

    @Override
    public void refreshDashboardCache(Long tenantId) {
        // Future: Implement caching if needed
        log.debug("Dashboard cache refresh requested for tenant: {}", tenantId);
    }

    // ===== Private Helper Methods =====

    private OrderSummaryDTO mapToOrderSummary(Order order) {
        return OrderSummaryDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerName(order.getCustomer() != null ? order.getCustomer().getName() : null)
                .customerPhone(order.getCustomer() != null ? order.getCustomer().getPhone() : null)
                .eventDate(order.getEventDate())
                .guestCount(order.getGuestCount())
                .grandTotal(order.getGrandTotal())
                .advanceAmount(order.getAdvanceAmount())
                .balanceAmount(order.getBalanceAmount())
                .status(order.getStatus() != null ? order.getStatus().name() : null)
                .createdAt(order.getCreatedAt())
                .build();
    }

    private UpcomingEventDTO mapToUpcomingEvent(Order order) {
        LocalDate today = LocalDate.now();
        long daysUntil = ChronoUnit.DAYS.between(today, order.getEventDate());

        String eventTypeName = null;
        if (order.getEventType() != null) {
            // Try to get translation, fallback to code
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

        return UpcomingEventDTO.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerName(order.getCustomer() != null ? order.getCustomer().getName() : null)
                .eventDate(order.getEventDate())
                .eventTime(order.getEventTime())
                .eventTypeName(eventTypeName)
                .venueName(order.getVenueName())
                .guestCount(order.getGuestCount())
                .balance(order.getBalanceAmount())
                .daysUntilEvent(daysUntil)
                .build();
    }

    private LowStockAlertDTO mapToLowStockAlert(Material material) {
        BigDecimal currentStock = material.getCurrentStock() != null ? material.getCurrentStock() : BigDecimal.ZERO;
        BigDecimal minimumStock = material.getMinimumStock() != null ? material.getMinimumStock() : BigDecimal.ZERO;
        BigDecimal shortfall = minimumStock.subtract(currentStock);

        // Determine severity: CRITICAL if below 50% of minimum or zero, WARNING otherwise
        String severity = "WARNING";
        if (currentStock.compareTo(BigDecimal.ZERO) <= 0 ||
            (minimumStock.compareTo(BigDecimal.ZERO) > 0 &&
             currentStock.compareTo(minimumStock.multiply(new BigDecimal("0.5"))) < 0)) {
            severity = "CRITICAL";
        }

        String groupName = null;
        if (material.getMaterialGroup() != null) {
            groupName = material.getMaterialGroup().getGroupCode();
        }

        String unitSymbol = null;
        if (material.getUnit() != null) {
            unitSymbol = material.getUnit().getUnitCode();
        }

        return LowStockAlertDTO.builder()
                .materialId(material.getId())
                .materialCode(material.getMaterialCode())
                .materialName(getMaterialName(material))
                .groupName(groupName)
                .unitSymbol(unitSymbol)
                .currentStock(currentStock)
                .minimumStock(minimumStock)
                .shortfall(shortfall)
                .severity(severity)
                .build();
    }

    private String getMaterialName(Material material) {
        if (material.getTranslations() != null && !material.getTranslations().isEmpty()) {
            return material.getTranslations().stream()
                    .filter(t -> t.getLanguageCode() != null && "en".equals(t.getLanguageCode().name()))
                    .findFirst()
                    .map(t -> t.getMaterialName())
                    .orElse(material.getMaterialCode());
        }
        return material.getMaterialCode();
    }

    private Long countUpcomingEvents(Long tenantId, int daysAhead) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(daysAhead);
        return (long) orderRepository.findUpcomingEventsInRange(tenantId, today, endDate).size();
    }

    private BigDecimal calculateMonthExpectedRevenue(Long tenantId, int month, int year) {
        // Sum of all order grand totals for the month (excluding cancelled/draft)
        return getOrDefault(orderRepository.sumOrderTotalByMonth(tenantId, month, year), BigDecimal.ZERO);
    }

    private Map<String, BigDecimal> getMonthlyRevenueData(Long tenantId) {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(MONTHS_FOR_CHART);
        List<Object[]> stats = orderRepository.getMonthlyOrderStats(tenantId, startDate);

        Map<String, BigDecimal> monthlyData = new LinkedHashMap<>();
        for (Object[] row : stats) {
            int month = ((Number) row[0]).intValue();
            int year = ((Number) row[1]).intValue();
            BigDecimal total = row[3] != null ? (BigDecimal) row[3] : BigDecimal.ZERO;
            String key = String.format("%d-%02d", year, month);
            monthlyData.put(key, total);
        }

        return monthlyData;
    }

    private Map<String, Long> getOrderStatusDistribution(Long tenantId) {
        List<Object[]> distribution = orderRepository.getOrderStatusDistribution(tenantId);
        Map<String, Long> statusMap = new HashMap<>();

        for (Object[] row : distribution) {
            OrderStatus status = (OrderStatus) row[0];
            Long count = ((Number) row[1]).longValue();
            statusMap.put(status.name(), count);
        }

        return statusMap;
    }

    private Map<String, Long> getPaymentMethodDistribution(Long tenantId) {
        List<Object[]> distribution = paymentRepository.getPaymentMethodDistribution(tenantId);
        Map<String, Long> methodMap = new HashMap<>();

        for (Object[] row : distribution) {
            String method = row[0] != null ? row[0].toString() : "OTHER";
            Long count = ((Number) row[1]).longValue();
            methodMap.put(method, count);
        }

        return methodMap;
    }

    private Long getOrDefault(Long value, Long defaultValue) {
        return value != null ? value : defaultValue;
    }

    private BigDecimal getOrDefault(BigDecimal value, BigDecimal defaultValue) {
        return value != null ? value : defaultValue;
    }
}
