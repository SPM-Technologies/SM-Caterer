package com.smtech.SM_Caterer.web.controller;

import com.smtech.SM_Caterer.domain.repository.EventTypeRepository;
import com.smtech.SM_Caterer.domain.repository.MaterialRepository;
import com.smtech.SM_Caterer.domain.repository.MenuRepository;
import com.smtech.SM_Caterer.domain.repository.UnitRepository;
import com.smtech.SM_Caterer.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for dashboard page.
 * Displays overview statistics for the authenticated user's tenant.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final UnitRepository unitRepository;
    private final MaterialRepository materialRepository;
    private final MenuRepository menuRepository;
    private final EventTypeRepository eventTypeRepository;

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

        Map<String, Long> stats = new HashMap<>();
        stats.put("unitsCount", unitRepository.countByTenantId(tenantId));
        stats.put("materialsCount", materialRepository.countByTenantId(tenantId));
        stats.put("menusCount", menuRepository.countByTenantId(tenantId));
        stats.put("eventTypesCount", eventTypeRepository.countByTenantId(tenantId));

        model.addAttribute("stats", stats);
        return "dashboard/index";
    }
}
