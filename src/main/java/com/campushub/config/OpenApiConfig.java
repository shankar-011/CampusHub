package com.campushub.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CampusHub API")
                        .version("1.0")
                        .description("""
                                REST API for campus event management.
                                """))
                .tags(List.of(
                        new Tag().name("Auth").description("Register, login and refresh tokens"),
                        new Tag().name("Events").description("Browse and manage campus events"),
                        new Tag().name("Bookings").description("Book tickets and view your reservations"),
                        new Tag().name("Admin").description("User, event and booking management — ADMIN only")
                ))
                .addSecurityItem(new SecurityRequirement().addList("Bearer"))
                .components(new Components()
                        .addSecuritySchemes("Bearer", new SecurityScheme()
                                .name("Bearer")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
