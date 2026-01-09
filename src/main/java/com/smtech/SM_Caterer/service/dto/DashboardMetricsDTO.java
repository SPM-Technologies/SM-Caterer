package com.smtech.SM_Caterer.service.dto;

import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTO for dashboard metrics and statistics.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardMetricsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // Today's Metrics
    private Long todayOrderCount;
    private BigDecimal todayOrderValue;
    private Long todayPaymentCount;
    private BigDecimal todayPaymentValue;

    // This Month's Metrics
    private Long monthOrderCount;
    private BigDecimal monthRevenue;
    private BigDecimal monthExpectedRevenue;

    // Pending/Outstanding
    private BigDecimal totalPendingPayments;
    private Long pendingOrderCount;

    // Alerts
    private Long lowStockItemCount;
    private Long upcomingEventsCount;

    // Lists for display
    private List<OrderSummaryDTO> recentOrders;
    private List<UpcomingEventDTO> upcomingEvents;
    private List<LowStockAlertDTO> lowStockAlerts;

    // Chart Data
    private Map<String, BigDecimal> monthlyRevenueData;
    private Map<String, Long> orderStatusDistribution;
    private Map<String, Long> paymentMethodDistribution;
}
