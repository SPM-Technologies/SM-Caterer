package com.smtech.SM_Caterer.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Set;

/**
 * Controller for login page and root path routing.
 * Handles web-based authentication views and role-based redirects.
 */
@Controller
@Slf4j
public class LoginController {

    /**
     * Display login page.
     * Redirects to appropriate dashboard if already authenticated.
     */
    @GetMapping("/login")
    public String loginPage(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() &&
                !authentication.getPrincipal().equals("anonymousUser")) {
            log.debug("Already authenticated user accessing login, redirecting");
            return redirectByRole(authentication);
        }
        log.debug("Displaying login page");
        return "auth/login";
    }

    /**
     * Root path redirect - routes to appropriate dashboard based on role.
     */
    @GetMapping({"/", "/home"})
    public String rootRedirect(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }
        return redirectByRole(authentication);
    }

    private String redirectByRole(Authentication authentication) {
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        if (roles.contains("ROLE_SUPER_ADMIN")) {
            return "redirect:/admin";
        }
        return "redirect:/dashboard";
    }
}
