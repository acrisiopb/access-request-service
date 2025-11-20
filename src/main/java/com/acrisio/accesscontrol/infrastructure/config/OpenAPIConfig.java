package com.acrisio.accesscontrol.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI accessControlAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Access Control API")
                        .description("API para gerenciamento de usuários, módulos, acessos e solicitações de acesso.")
                        .version("1.0.0"));
    }
}
