package com.example.spark.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

@Configuration
@EnableWebSecurity(debug = true) // 디버그 로그 활성화
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // ✅ 인증이 필요 없는 요청 제외 (filtering 자체에서 제외)
                .securityMatcher("/api/relay-youtube-analytics", "/public/**")

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/relay-youtube-analytics").permitAll() // 🚀 인증 없이 접근 가능
                        .requestMatchers("/public/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("/api/welcome", true)
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 🔥 세션 사용 안 함
                );

        return http.build();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://localhost:8080"); // Swagger UI 도메인 명시
        config.addAllowedOrigin("http://localhost:3000"); // 필요시 다른 출처 추가
        config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedOrigin("https://app-spark.shop"); // 프로덕션 환경 추가
        config.addAllowedOrigin("https://api.app-spark.shop"); // API 접속 URL
        config.addAllowedOrigin("https://www.app-spark.shop");
        config.addAllowedOrigin("http://221.147.110.182:5000");
        config.addAllowedOrigin("https://spark-front-omega.vercel.app");
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")); // 허용할 HTTP 메서드
        config.setAllowedHeaders(List.of("*")); // 모든 요청 헤더 허용
        config.setExposedHeaders(List.of("Authorization")); // 노출할 응답 헤더
        config.setAllowCredentials(true); // 쿠키 허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
