package com.acrisio.accesscontrol.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI accessControlAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Access Control API")
                        .description("API para gerenciamento de usuários, módulos, acessos e solicitações de acesso.")
                        .version("1.0.0"))
                .servers(List.of(
                        new Server().url("/").description("Default Server URL")
                ));
    }
}