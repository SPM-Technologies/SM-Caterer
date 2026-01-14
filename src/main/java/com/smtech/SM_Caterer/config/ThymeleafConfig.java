package com.smtech.SM_Caterer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Thymeleaf configuration to expose request attributes.
 * This makes the request URI available in templates for navigation highlighting.
 */
@Configuration
public class ThymeleafConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RequestAttributeInterceptor());
    }

    /**
     * Interceptor that adds request URI to model for use in Thymeleaf templates.
     */
    public static class RequestAttributeInterceptor implements HandlerInterceptor {
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            // Expose the request URI as a request attribute
            request.setAttribute("currentUri", request.getRequestURI());
            return true;
        }
    }
}
