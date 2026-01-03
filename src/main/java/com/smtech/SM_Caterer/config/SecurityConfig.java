package com.smtech.SM_Caterer.config;

import com.smtech.SM_Caterer.API.filter.JwtAuthenticationFilter;
import com.smtech.SM_Caterer.security.jwt.JwtAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.beans.factory.annotation.Value;

/**
 * Spring Security Configuration with dual filter chains.
 *
 * Features:
 * - API Filter Chain (Order 1): JWT-based stateless authentication for /api/**
 * - Web Filter Chain (Order 2): Session-based form login for Thymeleaf views
 * - Role-based access control (RBAC)
 * - CORS configuration for API
 * - CSRF protection for web views
 * - Method-level security enabled
 *
 * @author CloudCaters Team
 * @version 2.0
 * @since Phase 3
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final CorsProperties corsProperties;

    @Value("${security.remember-me.key:smcaterer-default-key}")
    private String rememberMeKey;

    @Value("${security.remember-me.validity-seconds:604800}")
    private int rememberMeValiditySeconds;

    /**
     * API Security Filter Chain - JWT Stateless Authentication.
     * Handles all /api/** requests.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/**")

            // Disable CSRF for stateless API
            .csrf(AbstractHttpConfigurer::disable)

            // Enable CORS for API
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Exception handling
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            )

            // Session management - stateless for API
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Authorization rules for API
            .authorizeHttpRequests(auth -> auth
                // Public API endpoints
                .requestMatchers(
                    "/api/v1/auth/login",
                    "/api/v1/auth/refresh",
                    "/api/v1/auth/forgot-password",
                    "/api/v1/health"
                ).permitAll()

                // OPTIONS requests for CORS preflight
                .requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()

                // Tenant management - SUPER_ADMIN only
                .requestMatchers("/api/v1/tenants/**").hasRole("SUPER_ADMIN")

                // User management - TENANT_ADMIN or higher
                .requestMatchers("/api/v1/users/**").hasAnyRole("SUPER_ADMIN", "TENANT_ADMIN")

                // All other API requests require authentication
                .anyRequest().authenticated()
            )

            // Add JWT filter before UsernamePasswordAuthenticationFilter
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Web Security Filter Chain - Session-based Authentication.
     * Handles all web (Thymeleaf) requests.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/**")

            // Security headers
            .headers(headers -> headers
                .frameOptions(frame -> frame.deny())
                .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                .contentTypeOptions(content -> {})
                .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                .permissionsPolicy(permissions -> permissions.policy("geolocation=(), microphone=(), camera=()"))
            )

            // CSRF protection for web forms (cookie-based)
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers("/api/**")
            )

            // Session management - session-based for web UI
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
                .expiredUrl("/login?expired=true")
            )

            // Authorization rules for web
            .authorizeHttpRequests(auth -> auth
                // Public resources
                .requestMatchers("/login", "/logout", "/error/**").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                .requestMatchers("/static/**", "/favicon.ico").permitAll()
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**", "/v3/api-docs/**").permitAll()

                // Master data management - requires TENANT_ADMIN or MANAGER
                .requestMatchers("/masters/**").hasAnyRole("SUPER_ADMIN", "TENANT_ADMIN", "MANAGER")

                // Dashboard - any authenticated user
                .requestMatchers("/dashboard/**").authenticated()
                .requestMatchers("/", "/home").authenticated()

                // Profile - any authenticated user
                .requestMatchers("/profile/**").authenticated()

                // All other requests require authentication
                .anyRequest().authenticated()
            )

            // Form login configuration
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )

            // Logout configuration
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("SMCATERER_SESSION", "SM_CATERER_LANG", "JSESSIONID")
                .permitAll()
            )

            // Remember me configuration
            .rememberMe(remember -> remember
                .key(rememberMeKey)
                .tokenValiditySeconds(rememberMeValiditySeconds)
                .userDetailsService(userDetailsService)
            )

            // Exception handling for web
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/error/403")
            )

            // Authentication provider
            .authenticationProvider(authenticationProvider());

        return http.build();
    }

    /**
     * Configures CORS settings for API.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
        configuration.setAllowedMethods(corsProperties.getAllowedMethods());
        configuration.setAllowedHeaders(corsProperties.getAllowedHeaders());
        configuration.setAllowCredentials(corsProperties.isAllowCredentials());
        configuration.setMaxAge(corsProperties.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    /**
     * Configures authentication provider with UserDetailsService and PasswordEncoder.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    /**
     * Exposes AuthenticationManager bean for manual authentication.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
