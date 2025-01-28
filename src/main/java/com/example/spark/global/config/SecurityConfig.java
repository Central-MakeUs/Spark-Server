package com.example.spark.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable() // CSRF 비활성화
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정 활성화
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/oauth/**", "/swagger-ui/**", "/v3/api-docs/**", "/youtube/**").permitAll() // 인증 없이 접근 가능 경로
                        .anyRequest().authenticated() // 그 외 요청은 인증 필요
                )
                .oauth2Login(); // OAuth2 로그인 활성화
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://localhost:8080"); // Swagger UI 도메인 명시
        config.addAllowedOrigin("http://localhost:3000"); // 필요시 다른 출처 추가
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")); // 허용할 HTTP 메서드
        config.setAllowedHeaders(List.of("*")); // 모든 요청 헤더 허용
        config.setExposedHeaders(List.of("Authorization")); // 노출할 응답 헤더
        config.setAllowCredentials(true); // 쿠키 허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
