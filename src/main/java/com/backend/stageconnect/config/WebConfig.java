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
                "https://stageconnect.vercel.app"  // Example production URL (change to your actual domain)
            )
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true);
    }
} 