package com.smtech.SM_Caterer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.smtech.SM_Caterer.domain.entity.Tenant;
import com.smtech.SM_Caterer.domain.repository.TenantRepository;
import com.smtech.SM_Caterer.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Web MVC configuration for Thymeleaf and static resources.
 * - Exposes request URI for navigation highlighting
 * - Serves uploaded files (logos) from external directory
 * - Exposes tenant branding information to all pages
 */
@Configuration
@RequiredArgsConstructor
public class ThymeleafConfig implements WebMvcConfigurer {

    @Value("${app.upload.logo-dir:uploads/logos}")
    private String logoUploadDir;

    private final TenantRepository tenantRepository;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TenantBrandingInterceptor(tenantRepository));
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded logos from external directory
        registry.addResourceHandler("/uploads/logos/**")
                .addResourceLocations("file:" + logoUploadDir + "/");
    }

    /**
     * Interceptor that adds request URI and tenant branding to model for use in Thymeleaf templates.
     */
    @RequiredArgsConstructor
    public static class TenantBrandingInterceptor implements HandlerInterceptor {

        private final TenantRepository tenantRepository;

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            // Expose the request URI as a request attribute
            request.setAttribute("currentUri", request.getRequestURI());

            // Add tenant branding information if user is authenticated
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && authentication.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                Long tenantId = userDetails.getTenantId();

                if (tenantId != null) {
                    tenantRepository.findById(tenantId).ifPresent(tenant -> {
                        request.setAttribute("tenantDisplayName", tenant.getEffectiveDisplayName());
                        request.setAttribute("tenantTagline", tenant.getTagline());
                        request.setAttribute("tenantLogoPath", tenant.getLogoPath());
                        request.setAttribute("tenantHasLogo", tenant.hasLogo());
                        request.setAttribute("tenantPrimaryColor", tenant.getEffectivePrimaryColor());
                    });
                }
            }

            return true;
        }
    }
}
