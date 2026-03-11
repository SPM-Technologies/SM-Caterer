package com.smtech.SM_Caterer.api;

import com.smtech.SM_Caterer.base.BaseControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Comprehensive API endpoint security tests.
 *
 * Validates the API filter chain (Order 1) security configuration:
 * - Public endpoints are accessible without authentication
 * - Protected endpoints reject unauthenticated requests with 401
 * - Role-based access control enforces correct permissions
 * - CSRF is disabled for all /api/** endpoints
 *
 * Uses @WithMockUser to simulate different roles against the Spring Security
 * filter chain via MockMvc. The JWT filter is bypassed because Spring Security
 * Test injects the authentication before the filter chain executes.
 *
 * @author CloudCaters Team
 * @since Phase 7
 */
@DisplayName("API Endpoint Security Tests")
class ApiEndpointSecurityTest extends BaseControllerTest {

    // ========================================================================
    // A) PUBLIC API ENDPOINTS
    // ========================================================================

    @Nested
    @DisplayName("A) Public API Endpoints")
    class PublicApiEndpoints {

        @Test
        @DisplayName("GET /health should return 200 without authentication")
        void healthEndpointShouldBeAccessibleWithoutAuth() throws Exception {
            mockMvc.perform(get("/health")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST /api/v1/auth/login with JSON body should not return 401 (auth not required)")
        void loginEndpointShouldNotRequireAuthentication() throws Exception {
            String loginJson = """
                    {
                        "username": "nonexistent@test.com",
                        "password": "wrongpassword"
                    }
                    """;

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(loginJson))
                    .andExpect(status().is(org.hamcrest.Matchers.not(401)));
        }

        @Test
        @DisplayName("POST /api/v1/auth/refresh should not return 401 (auth not required)")
        void refreshEndpointShouldNotRequireAuthentication() throws Exception {
            String refreshJson = """
                    {
                        "refreshToken": "invalid-token"
                    }
                    """;

            mockMvc.perform(post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(refreshJson))
                    .andExpect(status().is(org.hamcrest.Matchers.not(401)));
        }

        @Test
        @DisplayName("POST /api/v1/auth/forgot-password should not return 401 (auth not required)")
        void forgotPasswordEndpointShouldNotRequireAuthentication() throws Exception {
            String forgotJson = """
                    {
                        "email": "test@example.com"
                    }
                    """;

            mockMvc.perform(post("/api/v1/auth/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(forgotJson))
                    .andExpect(status().is(org.hamcrest.Matchers.not(401)));
        }
    }

    // ========================================================================
    // B) UNAUTHENTICATED API ACCESS
    // ========================================================================

    @Nested
    @DisplayName("B) Unauthenticated API Access")
    class UnauthenticatedApiAccess {

        @Test
        @DisplayName("GET /api/v1/customers without auth should return 401 Unauthorized")
        void customersEndpointShouldRejectUnauthenticatedRequests() throws Exception {
            mockMvc.perform(get("/api/v1/customers")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/v1/orders without auth should return 401 Unauthorized")
        void ordersEndpointShouldRejectUnauthenticatedRequests() throws Exception {
            mockMvc.perform(get("/api/v1/orders")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/v1/tenants without auth should return 401 Unauthorized")
        void tenantsEndpointShouldRejectUnauthenticatedRequests() throws Exception {
            mockMvc.perform(get("/api/v1/tenants")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/v1/users without auth should return 401 Unauthorized")
        void usersEndpointShouldRejectUnauthenticatedRequests() throws Exception {
            mockMvc.perform(get("/api/v1/users")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/v1/menus without auth should return 401 Unauthorized")
        void menusEndpointShouldRejectUnauthenticatedRequests() throws Exception {
            mockMvc.perform(get("/api/v1/menus")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ========================================================================
    // C) TENANT API - SUPER_ADMIN ONLY
    // ========================================================================

    @Nested
    @DisplayName("C) Tenant API - SUPER_ADMIN Only")
    class TenantApiSuperAdminOnly {

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        @DisplayName("GET /api/v1/tenants with ROLE_SUPER_ADMIN should not return 401 or 403")
        void superAdminShouldAccessTenants() throws Exception {
            mockMvc.perform(get("/api/v1/tenants")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is(org.hamcrest.Matchers.not(401)))
                    .andExpect(status().is(org.hamcrest.Matchers.not(403)));
        }

        @Test
        @WithMockUser(roles = "TENANT_ADMIN")
        @DisplayName("GET /api/v1/tenants with ROLE_TENANT_ADMIN should return 403 Forbidden")
        void tenantAdminShouldNotAccessTenants() throws Exception {
            mockMvc.perform(get("/api/v1/tenants")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("GET /api/v1/tenants with ROLE_MANAGER should return 403 Forbidden")
        void managerShouldNotAccessTenants() throws Exception {
            mockMvc.perform(get("/api/v1/tenants")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "STAFF")
        @DisplayName("GET /api/v1/tenants with ROLE_STAFF should return 403 Forbidden")
        void staffShouldNotAccessTenants() throws Exception {
            mockMvc.perform(get("/api/v1/tenants")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "VIEWER")
        @DisplayName("GET /api/v1/tenants with ROLE_VIEWER should return 403 Forbidden")
        void viewerShouldNotAccessTenants() throws Exception {
            mockMvc.perform(get("/api/v1/tenants")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

    // ========================================================================
    // D) USER API - ADMIN ROLES ONLY
    // ========================================================================

    @Nested
    @DisplayName("D) User API - Admin Roles Only")
    class UserApiAdminRoles {

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        @DisplayName("GET /api/v1/users with ROLE_SUPER_ADMIN should not return 401 or 403")
        void superAdminShouldAccessUsers() throws Exception {
            mockMvc.perform(get("/api/v1/users")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is(org.hamcrest.Matchers.not(401)))
                    .andExpect(status().is(org.hamcrest.Matchers.not(403)));
        }

        @Test
        @WithMockUser(roles = "TENANT_ADMIN")
        @DisplayName("GET /api/v1/users with ROLE_TENANT_ADMIN should not return 401 or 403")
        void tenantAdminShouldAccessUsers() throws Exception {
            mockMvc.perform(get("/api/v1/users")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is(org.hamcrest.Matchers.not(401)))
                    .andExpect(status().is(org.hamcrest.Matchers.not(403)));
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("GET /api/v1/users with ROLE_MANAGER should return 403 Forbidden")
        void managerShouldNotAccessUsers() throws Exception {
            mockMvc.perform(get("/api/v1/users")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "STAFF")
        @DisplayName("GET /api/v1/users with ROLE_STAFF should return 403 Forbidden")
        void staffShouldNotAccessUsers() throws Exception {
            mockMvc.perform(get("/api/v1/users")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "VIEWER")
        @DisplayName("GET /api/v1/users with ROLE_VIEWER should return 403 Forbidden")
        void viewerShouldNotAccessUsers() throws Exception {
            mockMvc.perform(get("/api/v1/users")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

    // ========================================================================
    // E) GENERAL AUTHENTICATED API ENDPOINTS
    // ========================================================================

    @Nested
    @DisplayName("E) General Authenticated API Endpoints")
    class GeneralAuthenticatedApi {

        @Test
        @WithMockUser(roles = "TENANT_ADMIN")
        @DisplayName("GET /api/v1/customers with ROLE_TENANT_ADMIN should not return 401")
        void tenantAdminShouldAccessCustomers() throws Exception {
            mockMvc.perform(get("/api/v1/customers")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is(org.hamcrest.Matchers.not(401)));
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("GET /api/v1/customers with ROLE_MANAGER should not return 401")
        void managerShouldAccessCustomers() throws Exception {
            mockMvc.perform(get("/api/v1/customers")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is(org.hamcrest.Matchers.not(401)));
        }

        @Test
        @WithMockUser(roles = "STAFF")
        @DisplayName("GET /api/v1/customers with ROLE_STAFF should not return 401")
        void staffShouldAccessCustomers() throws Exception {
            mockMvc.perform(get("/api/v1/customers")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is(org.hamcrest.Matchers.not(401)));
        }

        @Test
        @WithMockUser(roles = "VIEWER")
        @DisplayName("GET /api/v1/customers with ROLE_VIEWER should not return 401")
        void viewerShouldAccessCustomers() throws Exception {
            mockMvc.perform(get("/api/v1/customers")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is(org.hamcrest.Matchers.not(401)));
        }

        @Test
        @WithMockUser(roles = "TENANT_ADMIN")
        @DisplayName("GET /api/v1/orders with ROLE_TENANT_ADMIN should not return 401")
        void tenantAdminShouldAccessOrders() throws Exception {
            mockMvc.perform(get("/api/v1/orders")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is(org.hamcrest.Matchers.not(401)));
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("GET /api/v1/orders with ROLE_MANAGER should not return 401")
        void managerShouldAccessOrders() throws Exception {
            mockMvc.perform(get("/api/v1/orders")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is(org.hamcrest.Matchers.not(401)));
        }

        @Test
        @WithMockUser(roles = "STAFF")
        @DisplayName("GET /api/v1/orders with ROLE_STAFF should not return 401")
        void staffShouldAccessOrders() throws Exception {
            mockMvc.perform(get("/api/v1/orders")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is(org.hamcrest.Matchers.not(401)));
        }

        @Test
        @WithMockUser(roles = "VIEWER")
        @DisplayName("GET /api/v1/orders with ROLE_VIEWER should not return 401")
        void viewerShouldAccessOrders() throws Exception {
            mockMvc.perform(get("/api/v1/orders")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is(org.hamcrest.Matchers.not(401)));
        }

        @Test
        @WithMockUser(roles = "TENANT_ADMIN")
        @DisplayName("GET /api/v1/menus with ROLE_TENANT_ADMIN should not return 401")
        void tenantAdminShouldAccessMenus() throws Exception {
            mockMvc.perform(get("/api/v1/menus")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is(org.hamcrest.Matchers.not(401)));
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("GET /api/v1/menus with ROLE_MANAGER should not return 401")
        void managerShouldAccessMenus() throws Exception {
            mockMvc.perform(get("/api/v1/menus")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is(org.hamcrest.Matchers.not(401)));
        }

        @Test
        @WithMockUser(roles = "STAFF")
        @DisplayName("GET /api/v1/menus with ROLE_STAFF should not return 401")
        void staffShouldAccessMenus() throws Exception {
            mockMvc.perform(get("/api/v1/menus")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is(org.hamcrest.Matchers.not(401)));
        }

        @Test
        @WithMockUser(roles = "VIEWER")
        @DisplayName("GET /api/v1/menus with ROLE_VIEWER should not return 401")
        void viewerShouldAccessMenus() throws Exception {
            mockMvc.perform(get("/api/v1/menus")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is(org.hamcrest.Matchers.not(401)));
        }
    }

    // ========================================================================
    // F) API ERROR HANDLING
    // ========================================================================

    @Nested
    @DisplayName("F) API Error Handling")
    class ApiErrorHandling {

        @Test
        @WithMockUser(roles = "TENANT_ADMIN")
        @DisplayName("GET /api/v1/customers/99999 with auth should return 404 Not Found")
        void getNonExistentCustomerShouldReturn404() throws Exception {
            mockMvc.perform(get("/api/v1/customers/99999")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "TENANT_ADMIN")
        @DisplayName("POST /api/v1/customers with empty body and auth should return 400 Bad Request")
        void createCustomerWithEmptyBodyShouldReturn400() throws Exception {
            mockMvc.perform(post("/api/v1/customers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    // ========================================================================
    // G) CSRF IS DISABLED FOR API
    // ========================================================================

    @Nested
    @DisplayName("G) CSRF Disabled for API")
    class CsrfDisabledForApi {

        @Test
        @DisplayName("POST /api/v1/auth/login without CSRF token should not return 403")
        void loginWithoutCsrfTokenShouldNotBeForbidden() throws Exception {
            String loginJson = """
                    {
                        "username": "testuser",
                        "password": "testpassword"
                    }
                    """;

            // Perform POST without CSRF token - should NOT get 403 since CSRF is disabled for /api/**
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(loginJson))
                    .andExpect(status().is(org.hamcrest.Matchers.not(403)));
        }

        @Test
        @WithMockUser(roles = "TENANT_ADMIN")
        @DisplayName("POST /api/v1/customers without CSRF token should not return 403")
        void createCustomerWithoutCsrfTokenShouldNotBeForbidden() throws Exception {
            // Even an invalid body - the point is CSRF should not block the request
            mockMvc.perform(post("/api/v1/customers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().is(org.hamcrest.Matchers.not(403)));
        }

        @Test
        @WithMockUser(roles = "TENANT_ADMIN")
        @DisplayName("PUT /api/v1/customers/1 without CSRF token should not return 403")
        void updateCustomerWithoutCsrfTokenShouldNotBeForbidden() throws Exception {
            mockMvc.perform(put("/api/v1/customers/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().is(org.hamcrest.Matchers.not(403)));
        }

        @Test
        @WithMockUser(roles = "TENANT_ADMIN")
        @DisplayName("DELETE /api/v1/customers/1 without CSRF token should not return 403")
        void deleteCustomerWithoutCsrfTokenShouldNotBeForbidden() throws Exception {
            mockMvc.perform(delete("/api/v1/customers/1")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is(org.hamcrest.Matchers.not(403)));
        }
    }
}
