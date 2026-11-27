package com.team04.playlist.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SecurityCorsConfig implements WebMvcConfigurer {
    @Value("${playlist.cors.origin}")
    private String allowOrigin;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowOrigin)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD")
                .allowedHeaders("Content-Type", "Authorization")
                // 프론트가 다른 Origin일 때 리프레시 쿠키를 주고받으려면 필요하다.
                .allowCredentials(true);
    }
}