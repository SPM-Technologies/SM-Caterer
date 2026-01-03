package com.smtech.SM_Caterer.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI / Swagger Configuration.
 * Provides API documentation with JWT authentication support.
 *
 * Access Swagger UI at: /swagger-ui.html
 * Access API docs at: /api-docs
 *
 * @author CloudCaters Team
 * @version 1.0
 * @since Phase 2
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local Development Server"),
                        new Server()
                                .url("https://api.cloudcaters.com")
                                .description("Production Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token obtained from /api/v1/auth/login")
                        )
                );
    }

    private Info apiInfo() {
        return new Info()
                .title("CloudCaters API")
                .version("2.0.0")
                .description("""
                        CloudCaters Multi-Tenant Catering Management System API.

                        ## Authentication
                        1. Call POST /api/v1/auth/login with username and password
                        2. Copy the accessToken from response
                        3. Click 'Authorize' button above
                        4. Enter: Bearer <your-token>

                        ## Features
                        - Multi-tenant isolation
                        - Role-based access control (RBAC)
                        - JWT-based stateless authentication
                        - Rate limiting

                        ## Roles
                        - SUPER_ADMIN: Full system access
                        - TENANT_ADMIN: Full tenant access
                        - MANAGER: Order and inventory management
                        - STAFF: Order creation and viewing
                        - VIEWER: Read-only access
                        """)
                .contact(new Contact()
                        .name("CloudCaters Support")
                        .email("support@cloudcaters.com")
                        .url("https://cloudcaters.com"))
                .license(new License()
                        .name("Proprietary")
                        .url("https://cloudcaters.com/license"));
    }
}
