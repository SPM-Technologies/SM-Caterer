package com.smtech.SM_Caterer.web;

import com.smtech.SM_Caterer.base.BaseControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Security and Routing Tests")
class SecurityAndRoutingTest extends BaseControllerTest {

    // ========================================
    // PUBLIC ROUTES
    // ========================================
    @Nested
    @DisplayName("Public Routes")
    class PublicRoutes {

        @Test
        @DisplayName("Login page should be accessible without authentication")
        void loginPageShouldBeAccessible() throws Exception {
            mockMvc.perform(get("/login"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Static CSS resources should be accessible")
        void staticCssShouldBeAccessible() throws Exception {
            mockMvc.perform(get("/css/style.css"))
                    .andExpect(status().isOk());
        }
    }

    // ========================================
    // UNAUTHENTICATED ACCESS
    // ========================================
    @Nested
    @DisplayName("Unauthenticated Access - Should Redirect to Login")
    class UnauthenticatedAccess {

        @Test
        @DisplayName("Dashboard should redirect to login")
        void dashboardShouldRedirectToLogin() throws Exception {
            mockMvc.perform(get("/dashboard"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("**/login"));
        }

        @Test
        @DisplayName("Orders should redirect to login")
        void ordersShouldRedirectToLogin() throws Exception {
            mockMvc.perform(get("/orders"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("**/login"));
        }

        @Test
        @DisplayName("Admin should redirect to login")
        void adminShouldRedirectToLogin() throws Exception {
            mockMvc.perform(get("/admin"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("**/login"));
        }

        @Test
        @DisplayName("Customers should redirect to login")
        void customersShouldRedirectToLogin() throws Exception {
            mockMvc.perform(get("/customers"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("**/login"));
        }

        @Test
        @DisplayName("Profile should redirect to login")
        void profileShouldRedirectToLogin() throws Exception {
            mockMvc.perform(get("/profile"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("**/login"));
        }
    }

    // ========================================
    // SUPER_ADMIN ROLE
    // ========================================
    @Nested
    @DisplayName("SUPER_ADMIN Role Access")
    class SuperAdminAccess {

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        @DisplayName("SUPER_ADMIN can access /admin")
        void canAccessAdmin() throws Exception {
            mockMvc.perform(get("/admin"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        @DisplayName("SUPER_ADMIN can access /admin/tenants")
        void canAccessAdminTenants() throws Exception {
            mockMvc.perform(get("/admin/tenants"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        @DisplayName("SUPER_ADMIN can access /admin/users")
        void canAccessAdminUsers() throws Exception {
            mockMvc.perform(get("/admin/users"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        @DisplayName("SUPER_ADMIN CANNOT access /orders (tenant-scoped)")
        void cannotAccessOrders() throws Exception {
            mockMvc.perform(get("/orders"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        @DisplayName("SUPER_ADMIN CANNOT access /customers (tenant-scoped)")
        void cannotAccessCustomers() throws Exception {
            mockMvc.perform(get("/customers"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        @DisplayName("SUPER_ADMIN CANNOT access /payments (tenant-scoped)")
        void cannotAccessPayments() throws Exception {
            mockMvc.perform(get("/payments"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        @DisplayName("SUPER_ADMIN CANNOT access /settings (tenant-scoped)")
        void cannotAccessSettings() throws Exception {
            mockMvc.perform(get("/settings/branding"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        @DisplayName("SUPER_ADMIN can access /dashboard")
        void canAccessDashboard() throws Exception {
            mockMvc.perform(get("/dashboard"))
                    .andExpect(status().isOk());
        }
    }

    // ========================================
    // TENANT_ADMIN ROLE
    // ========================================
    @Nested
    @DisplayName("TENANT_ADMIN Role Access")
    class TenantAdminAccess {

        @Test
        @WithMockUser(roles = "TENANT_ADMIN")
        @DisplayName("TENANT_ADMIN CANNOT access /admin")
        void cannotAccessAdmin() throws Exception {
            mockMvc.perform(get("/admin"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "TENANT_ADMIN")
        @DisplayName("TENANT_ADMIN can access /dashboard")
        void canAccessDashboard() throws Exception {
            mockMvc.perform(get("/dashboard"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "TENANT_ADMIN")
        @DisplayName("TENANT_ADMIN can access /orders")
        void canAccessOrders() throws Exception {
            mockMvc.perform(get("/orders"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "TENANT_ADMIN")
        @DisplayName("TENANT_ADMIN can access /customers")
        void canAccessCustomers() throws Exception {
            mockMvc.perform(get("/customers"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "TENANT_ADMIN")
        @DisplayName("TENANT_ADMIN can access /payments")
        void canAccessPayments() throws Exception {
            mockMvc.perform(get("/payments"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "TENANT_ADMIN")
        @DisplayName("TENANT_ADMIN can access /masters/units")
        void canAccessMastersUnits() throws Exception {
            mockMvc.perform(get("/masters/units"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "TENANT_ADMIN")
        @DisplayName("TENANT_ADMIN can access /reports/orders")
        void canAccessReports() throws Exception {
            mockMvc.perform(get("/reports/orders"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "TENANT_ADMIN")
        @DisplayName("TENANT_ADMIN can access /settings/branding")
        void canAccessSettings() throws Exception {
            mockMvc.perform(get("/settings/branding"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "TENANT_ADMIN")
        @DisplayName("TENANT_ADMIN can access /profile")
        void canAccessProfile() throws Exception {
            mockMvc.perform(get("/profile"))
                    .andExpect(status().isOk());
        }
    }

    // ========================================
    // MANAGER ROLE
    // ========================================
    @Nested
    @DisplayName("MANAGER Role Access")
    class ManagerAccess {

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("MANAGER can access /orders")
        void canAccessOrders() throws Exception {
            mockMvc.perform(get("/orders"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("MANAGER can access /customers")
        void canAccessCustomers() throws Exception {
            mockMvc.perform(get("/customers"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("MANAGER can access /masters/units")
        void canAccessMasters() throws Exception {
            mockMvc.perform(get("/masters/units"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("MANAGER can access /reports")
        void canAccessReports() throws Exception {
            mockMvc.perform(get("/reports/orders"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("MANAGER CANNOT access /settings")
        void cannotAccessSettings() throws Exception {
            mockMvc.perform(get("/settings/branding"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("MANAGER CANNOT access /admin")
        void cannotAccessAdmin() throws Exception {
            mockMvc.perform(get("/admin"))
                    .andExpect(status().isForbidden());
        }
    }

    // ========================================
    // STAFF ROLE
    // ========================================
    @Nested
    @DisplayName("STAFF Role Access")
    class StaffAccess {

        @Test
        @WithMockUser(roles = "STAFF")
        @DisplayName("STAFF can access /orders")
        void canAccessOrders() throws Exception {
            mockMvc.perform(get("/orders"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "STAFF")
        @DisplayName("STAFF can access /customers")
        void canAccessCustomers() throws Exception {
            mockMvc.perform(get("/customers"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "STAFF")
        @DisplayName("STAFF CANNOT access /masters")
        void cannotAccessMasters() throws Exception {
            mockMvc.perform(get("/masters/units"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "STAFF")
        @DisplayName("STAFF CANNOT access /reports")
        void cannotAccessReports() throws Exception {
            mockMvc.perform(get("/reports/orders"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "STAFF")
        @DisplayName("STAFF CANNOT access /settings")
        void cannotAccessSettings() throws Exception {
            mockMvc.perform(get("/settings/branding"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "STAFF")
        @DisplayName("STAFF CANNOT access /admin")
        void cannotAccessAdmin() throws Exception {
            mockMvc.perform(get("/admin"))
                    .andExpect(status().isForbidden());
        }
    }

    // ========================================
    // VIEWER ROLE
    // ========================================
    @Nested
    @DisplayName("VIEWER Role Access")
    class ViewerAccess {

        @Test
        @WithMockUser(roles = "VIEWER")
        @DisplayName("VIEWER can access /dashboard")
        void canAccessDashboard() throws Exception {
            mockMvc.perform(get("/dashboard"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "VIEWER")
        @DisplayName("VIEWER can access /profile")
        void canAccessProfile() throws Exception {
            mockMvc.perform(get("/profile"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "VIEWER")
        @DisplayName("VIEWER CANNOT access /orders")
        void cannotAccessOrders() throws Exception {
            mockMvc.perform(get("/orders"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "VIEWER")
        @DisplayName("VIEWER CANNOT access /customers")
        void cannotAccessCustomers() throws Exception {
            mockMvc.perform(get("/customers"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "VIEWER")
        @DisplayName("VIEWER CANNOT access /masters")
        void cannotAccessMasters() throws Exception {
            mockMvc.perform(get("/masters/units"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "VIEWER")
        @DisplayName("VIEWER CANNOT access /reports")
        void cannotAccessReports() throws Exception {
            mockMvc.perform(get("/reports/orders"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "VIEWER")
        @DisplayName("VIEWER CANNOT access /payments")
        void cannotAccessPayments() throws Exception {
            mockMvc.perform(get("/payments"))
                    .andExpect(status().isForbidden());
        }
    }

    // ========================================
    // ROOT REDIRECT
    // ========================================
    @Nested
    @DisplayName("Root Redirect")
    class RootRedirect {

        @Test
        @WithMockUser(roles = "TENANT_ADMIN")
        @DisplayName("Root path should redirect authenticated users")
        void rootShouldRedirect() throws Exception {
            mockMvc.perform(get("/"))
                    .andExpect(status().is3xxRedirection());
        }

        @Test
        @WithMockUser(roles = "TENANT_ADMIN")
        @DisplayName("/home should redirect authenticated users")
        void homeShouldRedirect() throws Exception {
            mockMvc.perform(get("/home"))
                    .andExpect(status().is3xxRedirection());
        }
    }
}
