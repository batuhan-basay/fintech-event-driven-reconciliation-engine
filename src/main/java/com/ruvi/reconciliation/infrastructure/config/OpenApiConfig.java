package com.ruvi.reconciliation.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI reconciliationOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Reconciliation Engine API")
                        .description("Fault-tolerant, event-driven payment & financial reconciliation engine. "
                                + "Transactional Outbox + Idempotency + nightly reconciliation.")
                        .version("v1")
                        .contact(new Contact().name("Ruveyda"))
                        .license(new License().name("MIT")));
    }
}
