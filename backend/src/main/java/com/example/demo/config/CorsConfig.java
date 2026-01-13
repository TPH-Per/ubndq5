package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * CORS Configuration
 * 
 * CORS (Cross-Origin Resource Sharing) cho phép Vue.js frontend
 * gọi API từ Spring Boot backend.
 * 
 * Mặc định browser CHẶN request cross-origin vì lý do bảo mật.
 * Config này cho phép frontend (localhost:5174) gọi backend (localhost:8080).
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    
    /**
     * Cấu hình CORS cho Spring MVC
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")               // Áp dụng cho tất cả /api/*
                .allowedOrigins(
                    "http://localhost:5174",         // Vue.js dev server
                    "http://localhost:5173",         // Vue.js default port
                    "http://localhost:3000"          // Backup port
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")                 // Cho phép tất cả headers
                .allowCredentials(true)              // Cho phép gửi cookies/auth headers
                .maxAge(3600);                       // Cache preflight request 1 giờ
    }
    
    /**
     * Cấu hình CORS cho Spring Security
     * 
     * Spring Security cần config CORS riêng, không dùng chung với MVC
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Origins được phép
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:5174",
            "http://localhost:5173",
            "http://localhost:3000"
        ));
        
        // HTTP methods được phép
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        
        // Headers được phép
        configuration.setAllowedHeaders(List.of("*"));
        
        // Cho phép gửi credentials (Authorization header, cookies)
        configuration.setAllowCredentials(true);
        
        // Headers mà frontend được đọc từ response
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type"
        ));
        
        // Cache preflight request
        configuration.setMaxAge(3600L);
        
        // Áp dụng cho tất cả paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
