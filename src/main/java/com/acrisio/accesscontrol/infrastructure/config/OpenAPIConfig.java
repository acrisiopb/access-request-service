package com.acrisio.accesscontrol.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI accessControlAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Controle de Acessos Corporativo - API")
                        .description(
                                "Serviço de controle de acessos corporativos com autenticação JWT, solicitações de acesso, renovação e histórico.\n\n" +
                                "API para gerenciamento de usuários, módulos, acessos e solicitações de acesso.\n\n" +
                                        "Login: test@admin.com\n\n" +
                                        "Senha: test123"
                        )
                        .version("1.0.0")
                        .license(new License().name("Github | Access Request Service ").url("https://github.com/acrisiopb/access-request-service"))
                        .contact(new Contact().name("Acrísio Cruz").url("https://www.linkedin.com/in/acrisio-cruz-910261243/")))

                .servers(List.of(
                        new Server().url("/").description("Default Server URL")
                ))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("BearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        ));
    }
}