package com.backend.stageconnect.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins(
                "http://localhost:3000",           // Next.js dev server
                "http://127.0.0.1:3000",           // Alternative localhost
                "http://localhost:5173",           // Vite dev server
                "http://127.0.0.1:5173",           // Alternative Vite localhost
                "https://stageconnect.vercel.app"  // Production URL
            )
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("Origin", "X-Requested-With", "Content-Type", "Accept", "Authorization", "X-CORS-Debug")
            .exposedHeaders("Authorization", "X-CORS-Debug")
            .allowCredentials(true)
            .maxAge(3600);
    }
} 