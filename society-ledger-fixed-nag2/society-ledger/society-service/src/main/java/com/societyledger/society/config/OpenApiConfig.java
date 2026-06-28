package com.societyledger.society.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI societyLedgerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Society Ledger — Society Service")
                        .description("REST API for Society Service")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Society Ledger")
                                .email("support@societyledger.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8082").description("Local"),
                        new Server().url("https://api.societyledger.com").description("Production")));
    }
}
