package com.smtech.SM_Caterer.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for login page.
 * Handles web-based authentication views.
 */
@Controller
@Slf4j
public class LoginController {

    /**
     * Display login page.
     * Redirects to dashboard if already authenticated.
     */
    @GetMapping("/login")
    public String loginPage(Authentication authentication) {
        // Redirect to dashboard if already logged in
        if (authentication != null && authentication.isAuthenticated() &&
                !authentication.getPrincipal().equals("anonymousUser")) {
            log.debug("Already authenticated user accessing login, redirecting to dashboard");
            return "redirect:/dashboard";
        }
        log.debug("Displaying login page");
        return "auth/login";
    }
}
