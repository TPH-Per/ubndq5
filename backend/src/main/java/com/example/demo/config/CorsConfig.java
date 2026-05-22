package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


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

        private static final List<String> EXPLICIT_HEADERS = Arrays.asList(
                "Authorization", "Content-Type", "Accept", "Origin",
                "X-Requested-With", "Cache-Control", "ngrok-skip-browser-warning"
        );

        @Value("${app.cors.allowed-origins}")
        private String allowedOriginsRaw;

        @Value("${app.rate-limit.enabled:true}")
        private boolean rateLimitEnabled;

        /**
         * Rate limiter: 30 req/min per IP for citizen endpoints
         */
        @Override
        public void addInterceptors(InterceptorRegistry registry) {
                if (rateLimitEnabled) {
                        registry.addInterceptor(new RateLimitInterceptor())
                                        .addPathPatterns("/api/citizen/**");
                }
        }

        /**
         * Cấu hình CORS cho Spring MVC
         */
        @Override
        public void addCorsMappings(CorsRegistry registry) {
                String[] origins = parseAllowedOrigins();
                String[] lanOrigins = resolveLanOrigins(origins);

                String[] explicitHeaders = EXPLICIT_HEADERS.toArray(new String[0]);

                // Citizen APIs: cho phép LAN + public web origins (Vercel + Zalo WebView)
                registry.addMapping("/api/citizen/**")
                                .allowedOrigins(origins)
                                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                                .allowedHeaders(explicitHeaders)
                                .allowCredentials(true)
                                .maxAge(3600);

                // Staff/Admin APIs: chỉ cho LAN origins
                registry.addMapping("/api/staff/**")
                                .allowedOrigins(lanOrigins)
                                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                                .allowedHeaders(explicitHeaders)
                                .allowCredentials(true)
                                .maxAge(3600);

                registry.addMapping("/api/admin/**")
                                .allowedOrigins(lanOrigins)
                                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                                .allowedHeaders(explicitHeaders)
                                .allowCredentials(true)
                                .maxAge(3600);
        }

        /**
         * Cấu hình CORS cho Spring Security
         * 
         * Spring Security cần config CORS riêng, không dùng chung với MVC
         */
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                String[] origins = parseAllowedOrigins();
                String[] lanOrigins = resolveLanOrigins(origins);

                CorsConfiguration citizenConfig = buildCorsConfiguration(origins);
                CorsConfiguration staffConfig = buildCorsConfiguration(lanOrigins);
                CorsConfiguration adminConfig = buildCorsConfiguration(lanOrigins);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/api/citizen/**", citizenConfig);
                source.registerCorsConfiguration("/api/staff/**", staffConfig);
                source.registerCorsConfiguration("/api/admin/**", adminConfig);

                return source;
        }

        private CorsConfiguration buildCorsConfiguration(String[] allowedOrigins) {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
                // Must NOT use "*" when allowCredentials=true (CORS spec violation → TypeError in browsers/Zalo)
                configuration.setAllowedHeaders(Arrays.asList(
                        "Authorization", "Content-Type", "Accept", "Origin",
                        "X-Requested-With", "Cache-Control", "ngrok-skip-browser-warning"
                ));
                configuration.setAllowCredentials(true);
                configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
                configuration.setMaxAge(3600L);
                return configuration;
        }

        private String[] parseAllowedOrigins() {
                return Arrays.stream(allowedOriginsRaw.split(","))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .toArray(String[]::new);
        }

        private String[] resolveLanOrigins(String[] origins) {
                List<String> lanOrigins = Arrays.stream(origins)
                                .filter(this::isLanOrigin)
                                .collect(Collectors.toCollection(ArrayList::new));

                if (lanOrigins.isEmpty()) {
                        lanOrigins.add("http://localhost:5173");
                        lanOrigins.add("http://127.0.0.1:5173");
                        lanOrigins.add("http://localhost:5174");
                        lanOrigins.add("http://127.0.0.1:5174");
                }

                return lanOrigins.toArray(String[]::new);
        }

        private boolean isLanOrigin(String origin) {
                return origin.startsWith("http://10.")
                                || origin.startsWith("https://10.")
                                || origin.startsWith("http://192.168.")
                                || origin.startsWith("https://192.168.")
                                || origin.matches("^https?://172\\.(1[6-9]|2\\d|3[0-1])\\..*")
                                || origin.startsWith("http://localhost")
                                || origin.startsWith("https://localhost")
                                || origin.startsWith("http://127.0.0.1")
                                || origin.startsWith("https://127.0.0.1");
        }

}

