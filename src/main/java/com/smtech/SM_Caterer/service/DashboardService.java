package com.smtech.SM_Caterer.service;

import com.smtech.SM_Caterer.service.dto.*;

import java.util.List;

/**
 * Service interface for Dashboard operations.
 * Provides aggregated metrics and statistics for the dashboard view.
 */
public interface DashboardService {

    /**
     * Gets all dashboard metrics for a tenant.
     * @param tenantId Tenant ID
     * @return Dashboard metrics DTO with all statistics
     */
    DashboardMetricsDTO getDashboardMetrics(Long tenantId);

    /**
     * Gets recent orders for dashboard display.
     * @param tenantId Tenant ID
     * @param limit Maximum number of orders to return
     * @return List of order summaries
     */
    List<OrderSummaryDTO> getRecentOrders(Long tenantId, int limit);

    /**
     * Gets upcoming events for dashboard display.
     * @param tenantId Tenant ID
     * @param daysAhead Number of days to look ahead
     * @return List of upcoming events
     */
    List<UpcomingEventDTO> getUpcomingEvents(Long tenantId, int daysAhead);

    /**
     * Gets low stock alerts for dashboard display.
     * @param tenantId Tenant ID
     * @return List of low stock alerts
     */
    List<LowStockAlertDTO> getLowStockAlerts(Long tenantId);

    /**
     * Refreshes cached dashboard data.
     * @param tenantId Tenant ID
     */
    void refreshDashboardCache(Long tenantId);
}
