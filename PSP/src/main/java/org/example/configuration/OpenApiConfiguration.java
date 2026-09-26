package org.example.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    OpenAPI pspOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("PSP API")
                        .version("v1")
                        .description("Merchant-facing entry point for the payment network simulator."))
                .addServersItem(new Server().url("http://localhost:8084"));
    }
}
