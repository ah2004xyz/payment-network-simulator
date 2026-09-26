package org.example.bankb.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    OpenAPI bankBOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bank B API")
                        .version("v1")
                        .description("Issuing-bank API for cards with the 222222 prefix."))
                .addServersItem(new Server().url("http://localhost:8081/bank"));
    }
}
