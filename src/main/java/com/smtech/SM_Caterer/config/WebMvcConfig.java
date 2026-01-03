package com.smtech.SM_Caterer.config;

import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.util.Locale;

/**
 * Web MVC Configuration for Thymeleaf views.
 * Configures:
 * - Layout dialect for template inheritance
 * - i18n locale resolver and interceptor
 * - Static resource handlers
 * - Simple view controllers
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * Thymeleaf Layout Dialect for template inheritance.
     */
    @Bean
    public LayoutDialect layoutDialect() {
        return new LayoutDialect();
    }

    /**
     * Cookie-based locale resolver for i18n.
     * Stores user's language preference in a cookie.
     */
    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver("SM_CATERER_LANG");
        resolver.setDefaultLocale(Locale.ENGLISH);
        resolver.setCookieMaxAge(365 * 24 * 60 * 60); // 1 year
        resolver.setCookiePath("/");
        return resolver;
    }

    /**
     * Locale change interceptor.
     * Allows switching language via ?lang=xx parameter.
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Static resources
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");

        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");

        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");

        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");

        registry.addResourceHandler("/fonts/**")
                .addResourceLocations("classpath:/static/fonts/");

        // Webjars (if using)
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Simple redirects
        registry.addRedirectViewController("/", "/dashboard");
        registry.addRedirectViewController("/home", "/dashboard");
    }
}
