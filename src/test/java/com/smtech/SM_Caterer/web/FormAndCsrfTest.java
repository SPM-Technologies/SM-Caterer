package com.smtech.SM_Caterer.web;

import com.smtech.SM_Caterer.base.BaseControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for form submissions and CSRF protection.
 *
 * Validates that:
 * - POST requests without a CSRF token are rejected with 403 (Forbidden)
 * - POST requests with a valid CSRF token are processed
 * - Login form respects CSRF protection
 * - Logout redirects correctly
 *
 * The web filter chain uses CookieCsrfTokenRepository.withHttpOnlyFalse()
 * for CSRF protection on all non-API endpoints.
 */
@DisplayName("Form Submission and CSRF Protection Tests")
class FormAndCsrfTest extends BaseControllerTest {

    // ========================================================================
    // A) CSRF PROTECTION - requests without CSRF token must be rejected
    // ========================================================================

    @Nested
    @DisplayName("A) CSRF Protection - Missing Token")
    class CsrfProtection {

        @Test
        @DisplayName("POST /admin/tenants without CSRF token returns 403 Forbidden")
        @WithMockUser(username = "super_admin", roles = "SUPER_ADMIN")
        void postAdminTenants_withoutCsrf_returns403() throws Exception {
            mockMvc.perform(post("/admin/tenants")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("businessName", "Test Caterer")
                            .param("contactPerson", "Test Contact")
                            .param("email", "test@test.com")
                            .param("phone", "9876543210"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("POST /admin/users without CSRF token returns 403 Forbidden")
        @WithMockUser(username = "super_admin", roles = "SUPER_ADMIN")
        void postAdminUsers_withoutCsrf_returns403() throws Exception {
            mockMvc.perform(post("/admin/users")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("username", "newuser")
                            .param("email", "newuser@test.com")
                            .param("firstName", "New")
                            .param("lastName", "User"))
                    .andExpect(status().isForbidden());
        }
    }

    // ========================================================================
    // B) LOGIN FORM - CSRF enforcement on login endpoint
    // ========================================================================

    @Nested
    @DisplayName("B) Login Form CSRF")
    class LoginFormCsrf {

        @Test
        @DisplayName("POST /login with CSRF token and valid-shaped params processes (not 403)")
        void postLogin_withCsrf_processes() throws Exception {
            // With CSRF token the request should be processed by Spring Security.
            // Bad credentials will yield a redirect to /login?error=true, not a 403.
            mockMvc.perform(post("/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("username", "nonexistent_user")
                            .param("password", "wrong_password"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login?error=true"));
        }

        @Test
        @DisplayName("POST /login without CSRF token returns 403 Forbidden")
        void postLogin_withoutCsrf_returns403() throws Exception {
            mockMvc.perform(post("/login")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("username", "someuser")
                            .param("password", "somepassword"))
                    .andExpect(status().isForbidden());
        }
    }

    // ========================================================================
    // C) WEB FORM SUBMISSIONS WITH CSRF - requests with token are processed
    // ========================================================================

    @Nested
    @DisplayName("C) Web Form Submissions with CSRF")
    class WebFormSubmissionsWithCsrf {

        @Test
        @DisplayName("POST /masters/units with CSRF and TENANT_ADMIN role processes (not 403)")
        @WithMockUser(username = "tenant_admin", roles = "TENANT_ADMIN")
        void postMastersUnits_withCsrf_processes() throws Exception {
            // The POST should be accepted by security (not 403).
            // It may result in a 200 (validation error re-render), 3xx (redirect),
            // or even 5xx if the service layer has issues -- but NOT 403.
            mockMvc.perform(post("/masters/units")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("unitCode", "KG")
                            .param("unitName", "Kilogram")
                            .param("status", "ACTIVE"))
                    .andExpect(status().is(not(403)));
        }

        @Test
        @DisplayName("POST /masters/materials with CSRF and TENANT_ADMIN role processes (not 403)")
        @WithMockUser(username = "tenant_admin", roles = "TENANT_ADMIN")
        void postMastersMaterials_withCsrf_processes() throws Exception {
            mockMvc.perform(post("/masters/materials")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("materialCode", "MAT_TEST")
                            .param("materialName", "Test Material")
                            .param("status", "ACTIVE"))
                    .andExpect(status().is(not(403)));
        }

        @Test
        @DisplayName("POST /customers with CSRF and TENANT_ADMIN role processes (not 403)")
        @WithMockUser(username = "tenant_admin", roles = "TENANT_ADMIN")
        void postCustomers_withCsrf_processes() throws Exception {
            mockMvc.perform(post("/customers")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("name", "Test Customer")
                            .param("phone", "9876543210")
                            .param("email", "customer@test.com")
                            .param("status", "ACTIVE"))
                    .andExpect(status().is(not(403)));
        }

        @Test
        @DisplayName("POST /orders/wizard/cancel with CSRF and STAFF role processes (not 403)")
        @WithMockUser(username = "staff_user", roles = "STAFF")
        void postOrdersWizardCancel_withCsrf_processes() throws Exception {
            // Cancelling the wizard clears session and redirects to /orders
            mockMvc.perform(post("/orders/wizard/cancel")
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/orders"));
        }
    }

    // ========================================================================
    // D) LOGOUT
    // ========================================================================

    @Nested
    @DisplayName("D) Logout")
    class Logout {

        @Test
        @DisplayName("GET /logout when authenticated redirects to /login?logout=true")
        @WithMockUser(username = "tenant_admin", roles = "TENANT_ADMIN")
        void getLogout_whenAuthenticated_redirectsToLoginWithLogoutParam() throws Exception {
            // SecurityConfig uses GET-based logout via AntPathRequestMatcher("/logout", "GET")
            mockMvc.perform(get("/logout"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login?logout=true"));
        }
    }
}
