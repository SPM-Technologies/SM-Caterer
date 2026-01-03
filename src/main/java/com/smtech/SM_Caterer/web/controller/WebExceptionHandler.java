package com.smtech.SM_Caterer.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Exception handler for web controllers (Thymeleaf views).
 * Provides user-friendly error pages and logging.
 * Note: API exception handling is done by GlobalExceptionHandler in exception package.
 */
@ControllerAdvice(basePackages = "com.smtech.SM_Caterer.web.controller")
@Slf4j
public class WebExceptionHandler {

    /**
     * Handle access denied exceptions.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied for user {} accessing {}: {}",
                request.getRemoteUser(),
                request.getRequestURI(),
                ex.getMessage());
        return "error/403";
    }

    /**
     * Handle resource not found.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public String handleNotFound(NoResourceFoundException ex, HttpServletRequest request) {
        log.info("Resource not found: {}", request.getRequestURI());
        return "error/404";
    }

    /**
     * Handle illegal argument exceptions (validation errors).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException ex,
                                         HttpServletRequest request,
                                         RedirectAttributes redirectAttributes) {
        log.warn("Invalid argument in request {}: {}", request.getRequestURI(), ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());

        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isEmpty()) {
            return "redirect:" + referer;
        }
        return "redirect:/dashboard";
    }

    /**
     * Handle runtime exceptions.
     */
    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException(RuntimeException ex,
                                          HttpServletRequest request,
                                          RedirectAttributes redirectAttributes) {
        log.error("Runtime exception in request {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        // Don't expose internal error details to users
        redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred. Please try again.");

        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isEmpty()) {
            return "redirect:" + referer;
        }
        return "redirect:/dashboard";
    }

    /**
     * Handle all other exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception in request {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        ModelAndView mav = new ModelAndView("error/500");
        mav.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        // Don't expose exception details to users
        return mav;
    }
}
