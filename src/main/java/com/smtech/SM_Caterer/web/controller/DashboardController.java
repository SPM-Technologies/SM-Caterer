package com.smtech.SM_Caterer.web.controller;

import com.smtech.SM_Caterer.domain.repository.EventTypeRepository;
import com.smtech.SM_Caterer.domain.repository.MaterialRepository;
import com.smtech.SM_Caterer.domain.repository.MenuRepository;
import com.smtech.SM_Caterer.domain.repository.UnitRepository;
import com.smtech.SM_Caterer.security.CustomUserDetails;
import com.smtech.SM_Caterer.service.DashboardService;
import com.smtech.SM_Caterer.service.dto.DashboardMetricsDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for dashboard page.
 * Displays overview statistics for the authenticated user's tenant.
 */
@Controller
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER', 'STAFF', 'VIEWER')")
@Slf4j
public class DashboardController {

    private final UnitRepository unitRepository;
    private final MaterialRepository materialRepository;
    private final MenuRepository menuRepository;
    private final EventTypeRepository eventTypeRepository;
    private final DashboardService dashboardService;

    /**
     * Display dashboard with statistics.
     */
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null) {
            log.warn("Dashboard accessed without authentication");
            return "redirect:/login";
        }
        Long tenantId = userDetails.getTenantId();
        log.debug("Loading dashboard for tenant: {}", tenantId);

        // Get comprehensive dashboard metrics
        DashboardMetricsDTO metrics = dashboardService.getDashboardMetrics(tenantId);

        // Legacy stats for backward compatibility
        Map<String, Long> stats = new HashMap<>();
        stats.put("unitsCount", unitRepository.countByTenantId(tenantId));
        stats.put("materialsCount", materialRepository.countByTenantId(tenantId));
        stats.put("menusCount", menuRepository.countByTenantId(tenantId));
        stats.put("eventTypesCount", eventTypeRepository.countByTenantId(tenantId));

        model.addAttribute("stats", stats);
        model.addAttribute("metrics", metrics);
        model.addAttribute("recentOrders", metrics.getRecentOrders());
        model.addAttribute("upcomingEvents", metrics.getUpcomingEvents());
        model.addAttribute("lowStockAlerts", metrics.getLowStockAlerts());

        return "dashboard/index";
    }

    /**
     * API endpoint for dashboard metrics (for AJAX refresh).
     */
    @GetMapping("/api/dashboard/metrics")
    @ResponseBody
    public ResponseEntity<DashboardMetricsDTO> getMetrics(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        Long tenantId = userDetails.getTenantId();
        DashboardMetricsDTO metrics = dashboardService.getDashboardMetrics(tenantId);

        return ResponseEntity.ok(metrics);
    }

    /**
     * Refresh dashboard data.
     */
    @GetMapping("/api/dashboard/refresh")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> refreshDashboard(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        Long tenantId = userDetails.getTenantId();
        dashboardService.refreshDashboardCache(tenantId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Dashboard data refreshed");

        return ResponseEntity.ok(response);
    }
}
