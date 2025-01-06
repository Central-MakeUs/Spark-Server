package com.example.spark.global.config;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.ArrayList;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "Spark API", version = "v1", description = """
        # Spark API 문서
        ---
        
        ### 개발자
        
        서버 개발 담당 박상돈: sky980221@gmail.com
       
        ---
        
        ### 개발용 토큰
        
        ``
        
        ---
        
        """),
        servers = {
                @Server(url = "http://15.165.112.52:8080/api", description = "Public Server URL"), // Public IP + 포트
                @Server(url = "http://localhost:8080/api", description = "Local Server URL")     // 로컬 환경
        },
        security = {
                @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class OpenApiConfig {

    public OpenApiConfig(MappingJackson2HttpMessageConverter converter) {
        var supportedMediaTypes = new ArrayList<>(converter.getSupportedMediaTypes());
        supportedMediaTypes.add(new MediaType("application", "octet-stream"));
        converter.setSupportedMediaTypes(supportedMediaTypes);
    }
}