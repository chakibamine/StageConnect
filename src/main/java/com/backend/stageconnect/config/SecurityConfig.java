package com.backend.stageconnect.config;

import com.backend.stageconnect.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(authorize -> authorize
                // Allow authentication endpoints (login, register, validate token)
                .requestMatchers("/api/auth/**").permitAll()
                // Individual registration endpoints for backward compatibility
                .requestMatchers("/api/candidates/register").permitAll()
                .requestMatchers("/api/responsibles/register").permitAll()
                // Allow company endpoints (for now, can be restricted later)
                .requestMatchers("/api/companies/**").permitAll()
                // For development, allow all other requests. Change to authenticated for production
                .anyRequest().permitAll()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow specific origins instead of wildcard for better security
        // Add your Next.js frontend URL - update this with your actual frontend URL
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",           // Next.js dev server
            "http://127.0.0.1:3000",           // Alternative localhost
            "http://localhost:5173",           // Vite dev server
            "http://127.0.0.1:5173"            // Alternative Vite localhost
        ));
        
        // Allow common HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));
        
        // Allow all common headers
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", 
            "Content-Type", 
            "Accept", 
            "X-Requested-With",
            "Cache-Control",
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Headers",
            "X-CORS-Debug"
        ));
        
        // Allow browsers to send credentials (cookies, auth headers)
        configuration.setAllowCredentials(true);
        
        // How long the browser should cache the CORS response (in seconds)
        configuration.setMaxAge(3600L);
        
        // Expose custom headers that frontend might need
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization", 
            "Content-Disposition"
        ));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
} 