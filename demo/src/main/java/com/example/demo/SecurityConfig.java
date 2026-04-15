package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Allow frontend static page/resources without authentication
                        .requestMatchers(HttpMethod.GET,
                                "/", "/index.html",
                                "/favicon.ico",
                                "/**/*.html", "/**/*.css", "/**/*.js",
                                "/**/*.png", "/**/*.jpg", "/**/*.jpeg", "/**/*.svg", "/**/*.webp"
                        ).permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/login", "/hello", "/h2-console/**").permitAll() //public endpoints
                        .requestMatchers(HttpMethod.POST, "/users").permitAll() //public endpoints
                        .requestMatchers(HttpMethod.GET, "/users", "/access").hasRole("ADMIN") //protected endpoints with ADMIN role
                        .requestMatchers(HttpMethod.GET, "/profile").authenticated() //authenticated user profile endpoint
                        .anyRequest().authenticated()) //any other request need to be authenticated
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));

        return http.build();
    }
}
