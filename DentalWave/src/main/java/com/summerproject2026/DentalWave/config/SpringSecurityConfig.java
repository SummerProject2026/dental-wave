package com.summerproject2026.DentalWave.config;

import com.summerproject2026.DentalWave.security.JwtAuthenticationEntryPoint;
import com.summerproject2026.DentalWave.security.JwtAuthenticationFilter;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

    // Configures Spring Security roles and permissions for the application
    @Configuration
    @EnableMethodSecurity
    @AllArgsConstructor
    public class SpringSecurityConfig {

        // Handles unauthorized access attempts
        private JwtAuthenticationEntryPoint authenticationEntryPoint;

        // Intercepts requests and validates JWT tokens
        private JwtAuthenticationFilter authenticationFilter;

        // Encodes passwords using BCrypt hashing
        @Bean
        public static PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }

        // Defines which endpoints are public and which require authentication
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(authorize -> {
                        // Public endpoints - no login required
                        authorize.requestMatchers("/api/auth/login").permitAll();
                        authorize.requestMatchers("/api/auth/register").permitAll();
                        // Allow preflight requests from the frontend
                        authorize.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                        // All other endpoints require authentication
                        authorize.anyRequest().authenticated();
                    });

            // Handle unauthorized access with custom entry point
            http.exceptionHandling(exception -> exception
                    .authenticationEntryPoint(authenticationEntryPoint));

            // Add JWT filter before the default username/password filter
            http.addFilterBefore(authenticationFilter,
                    UsernamePasswordAuthenticationFilter.class);

            return http.build();
        }

        // Returns the AuthenticationManager for login processing
        @Bean
        public AuthenticationManager authenticationManager(
                AuthenticationConfiguration configuration) throws Exception {
            return configuration.getAuthenticationManager();
        }
    }
