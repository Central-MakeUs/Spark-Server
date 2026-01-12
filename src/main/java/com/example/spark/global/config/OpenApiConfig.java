package com.example.spark.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // Bearer 인증 스키마 설정
        SecurityScheme bearerScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP) // HTTP 인증 타입
                .scheme("bearer") // Bearer 토큰 방식
                .bearerFormat("JWT") // 토큰 형식 (옵션)
                .description("Enter your Bearer token to access the API");

        return new OpenAPI()
                .info(new Info()
                        .title("Spark API")
                        .version("1.0")
                        .description("Spark API 문서"))
                .servers(List.of(
                        new Server().url("https://api.app-spark.store").description("Production Server"),
                        new Server().url("http://localhost:8080").description("Local Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth")) // 모든 요청에 인증 추가
                .components(new io.swagger.v3.oas.models.Components().addSecuritySchemes("BearerAuth", bearerScheme));
    }
}