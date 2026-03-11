package com.smtech.SM_Caterer.web;

import com.smtech.SM_Caterer.base.BaseControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for navigation flows across different user roles.
 *
 * Validates that:
 * - Unauthenticated users are redirected to login
 * - Authenticated users can access pages appropriate for their role
 * - Cross-role isolation is enforced (users cannot access pages outside their role)
 *
 * Security rules (from SecurityConfig web filter chain):
 * - /admin/**           -> SUPER_ADMIN only
 * - /masters/**         -> SUPER_ADMIN, TENANT_ADMIN, MANAGER
 * - /orders/**          -> TENANT_ADMIN, MANAGER, STAFF
 * - /customers/**       -> TENANT_ADMIN, MANAGER, STAFF
 * - /payments/**        -> TENANT_ADMIN, MANAGER, STAFF
 * - /reports/**         -> TENANT_ADMIN, MANAGER
 * - /settings/**        -> TENANT_ADMIN only
 * - /dashboard/**       -> any authenticated user
 * - /profile/**         -> any authenticated user
 */
@DisplayName("Navigation Flow Tests")
class NavigationFlowTest extends BaseControllerTest {

    // ========================================================================
    // A) LOGIN FLOW
    // ========================================================================

    @Nested
    @DisplayName("A) Login Flow")
    class LoginFlow {

        @Test
        @DisplayName("GET /login when unauthenticated returns 200 with login page")
        void getLogin_whenUnauthenticated_returns200() throws Exception {
            mockMvc.perform(get("/login"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /login when already authenticated as TENANT_ADMIN redirects to /dashboard")
        @WithMockUser(username = "tenant_admin", roles = "TENANT_ADMIN")
        void getLogin_whenAuthenticatedAsTenantAdmin_redirectsToDashboard() throws Exception {
            mockMvc.perform(get("/login"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/dashboard"));
        }

        @Test
        @DisplayName("GET /login when already authenticated as SUPER_ADMIN redirects to /admin")
        @WithMockUser(username = "super_admin", roles = "SUPER_ADMIN")
        void getLogin_whenAuthenticatedAsSuperAdmin_redirectsToAdmin() throws Exception {
            mockMvc.perform(get("/login"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin"));
        }
    }

    // ========================================================================
    // B) SUPER_ADMIN NAVIGATION
    // ========================================================================

    @Nested
    @DisplayName("B) SUPER_ADMIN Navigation")
    @WithMockUser(username = "super_admin", roles = "SUPER_ADMIN")
    class SuperAdminNavigation {

        @Test
        @DisplayName("GET /admin returns success (admin dashboard view)")
        void getAdmin_returnsSuccess() throws Exception {
            mockMvc.perform(get("/admin"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /admin/tenants returns success (tenant list)")
        void getAdminTenants_returnsSuccess() throws Exception {
            mockMvc.perform(get("/admin/tenants"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /admin/users returns success (user list)")
        void getAdminUsers_returnsSuccess() throws Exception {
            mockMvc.perform(get("/admin/users"))
                    .andExpect(status().isOk());
        }
    }

    // ========================================================================
    // C) TENANT_ADMIN FULL NAVIGATION
    // ========================================================================

    @Nested
    @DisplayName("C) TENANT_ADMIN Full Navigation")
    @WithMockUser(username = "tenant_admin", roles = "TENANT_ADMIN")
    class TenantAdminNavigation {

        @Test
        @DisplayName("GET /dashboard returns success")
        void getDashboard_returnsSuccess() throws Exception {
            mockMvc.perform(get("/dashboard"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /orders returns success")
        void getOrders_returnsSuccess() throws Exception {
            mockMvc.perform(get("/orders"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /customers returns success")
        void getCustomers_returnsSuccess() throws Exception {
            mockMvc.perform(get("/customers"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /payments returns success")
        void getPayments_returnsSuccess() throws Exception {
            mockMvc.perform(get("/payments"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /masters/units returns success")
        void getMastersUnits_returnsSuccess() throws Exception {
            mockMvc.perform(get("/masters/units"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /masters/materials returns success")
        void getMastersMaterials_returnsSuccess() throws Exception {
            mockMvc.perform(get("/masters/materials"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /masters/menus returns success")
        void getMastersMenus_returnsSuccess() throws Exception {
            mockMvc.perform(get("/masters/menus"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /masters/event-types returns success")
        void getMastersEventTypes_returnsSuccess() throws Exception {
            mockMvc.perform(get("/masters/event-types"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /masters/recipes returns success")
        void getMastersRecipes_returnsSuccess() throws Exception {
            mockMvc.perform(get("/masters/recipes"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /masters/upi-qr returns success")
        void getMastersUpiQr_returnsSuccess() throws Exception {
            mockMvc.perform(get("/masters/upi-qr"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /reports returns success or redirect")
        void getReports_returnsSuccessOrRedirect() throws Exception {
            mockMvc.perform(get("/reports"))
                    .andExpect(status().is2xxSuccessful());
        }

        @Test
        @DisplayName("GET /reports/orders returns success")
        void getReportsOrders_returnsSuccess() throws Exception {
            mockMvc.perform(get("/reports/orders"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /reports/payments returns success")
        void getReportsPayments_returnsSuccess() throws Exception {
            mockMvc.perform(get("/reports/payments"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /settings/branding returns success")
        void getSettingsBranding_returnsSuccess() throws Exception {
            mockMvc.perform(get("/settings/branding"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /settings/email returns success")
        void getSettingsEmail_returnsSuccess() throws Exception {
            mockMvc.perform(get("/settings/email"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /profile returns success")
        void getProfile_returnsSuccess() throws Exception {
            mockMvc.perform(get("/profile"))
                    .andExpect(status().isOk());
        }
    }

    // ========================================================================
    // D) CROSS-ROLE ISOLATION
    // ========================================================================

    @Nested
    @DisplayName("D) Cross-Role Isolation")
    class CrossRoleIsolation {

        @Test
        @DisplayName("STAFF cannot access /masters/units - returns 403")
        @WithMockUser(username = "staff_user", roles = "STAFF")
        void staff_cannotAccessMastersUnits() throws Exception {
            mockMvc.perform(get("/masters/units"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("STAFF cannot access /reports - returns 403")
        @WithMockUser(username = "staff_user", roles = "STAFF")
        void staff_cannotAccessReports() throws Exception {
            mockMvc.perform(get("/reports"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("VIEWER cannot access /orders - returns 403")
        @WithMockUser(username = "viewer_user", roles = "VIEWER")
        void viewer_cannotAccessOrders() throws Exception {
            mockMvc.perform(get("/orders"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("MANAGER cannot access /admin - returns 403")
        @WithMockUser(username = "manager_user", roles = "MANAGER")
        void manager_cannotAccessAdmin() throws Exception {
            mockMvc.perform(get("/admin"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("MANAGER cannot access /settings/branding - returns 403")
        @WithMockUser(username = "manager_user", roles = "MANAGER")
        void manager_cannotAccessSettingsBranding() throws Exception {
            mockMvc.perform(get("/settings/branding"))
                    .andExpect(status().isForbidden());
        }
    }
}
