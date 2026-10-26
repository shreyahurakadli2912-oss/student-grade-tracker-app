package com.studentgradetracker.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI studentGradeTrackerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Student Grade Management System API")
                        .description("REST API documentation for Student Grade Management System - CodeAlpha Java Internship Project. " +
                                "Supports student records management, bulk CSV/Excel upload, grade calculations, analytics, and exports.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Student Grade Management System")
                                .email("developer@studentgradetracker.com"))
                        .license(new License().name("Apache 2.0")));
    }
}
