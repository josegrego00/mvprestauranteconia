package com.mvprestaurante.mvp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MVP Restaurant API")
                        .description("API RESTful para el sistema de gestión de restaurante")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Soporte MVP")
                                .email("soporte@mibombay.com"))
                        .license(new License()
                                .name("Uso interno"))
                        .termsOfService("Términos de servicio"))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Servidor de desarrollo")));
    }
}