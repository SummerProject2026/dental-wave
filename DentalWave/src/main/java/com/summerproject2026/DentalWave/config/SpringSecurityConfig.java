package com.summerproject2026.DentalWave.config;

import com.summerproject2026.DentalWave.security.JwtAuthenticationEntryPoint;
import com.summerproject2026.DentalWave.security.JwtAuthenticationFilter;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configures Spring Security authentication, authorization,
 * JWT validation, and CORS settings for the application.
 */
@Configuration
@EnableMethodSecurity
@AllArgsConstructor
public class SpringSecurityConfig {

    // Handles unauthorized access attempts
    private JwtAuthenticationEntryPoint authenticationEntryPoint;

    // Intercepts requests and validates JWT tokens
    private JwtAuthenticationFilter authenticationFilter;

    /**
     * Creates a BCrypt password encoder used to hash passwords
     * before storing them in the database.
     */
    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures endpoint security rules and JWT authentication.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // Enables Cross-Origin Resource Sharing (CORS)
                // so the React frontend running on localhost:5173
                // can communicate with the backend running on localhost:8080
                .cors(Customizer.withDefaults())

                // Disable CSRF because JWT is being used instead of session authentication
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(authorize -> {

                    // Public authentication endpoints
                    authorize.requestMatchers("/api/auth/login").permitAll();
                    authorize.requestMatchers("/api/auth/register").permitAll();

                    // Allow browser preflight OPTIONS requests
                    authorize.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();

                    // All other endpoints require authentication
                    authorize.anyRequest().authenticated();
                });

        // Use custom handler for unauthorized requests
        http.exceptionHandling(exception ->
                exception.authenticationEntryPoint(authenticationEntryPoint));

        // Validate JWT tokens before Spring Security attempts authentication
        http.addFilterBefore(
                authenticationFilter,
                UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }

    /**
     * Configures which frontend applications are allowed
     * to access the backend API.
     *
     * This is required because the frontend and backend
     * run on different ports during development.
     *
     * Frontend:
     * http://localhost:5173
     *
     * Backend:
     * http://localhost:8080
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        // Allow requests from the React/Vite frontend
        configuration.setAllowedOrigins(
                List.of("http://localhost:5173")
        );

        // Allow common HTTP methods used by the frontend
        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")
        );

        // Allow all request headers
        configuration.setAllowedHeaders(List.of("*"));

        // Allow credentials such as Authorization headers
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        // Apply this configuration to all endpoints
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * Provides Spring Security's AuthenticationManager,
     * which is used during login authentication.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }
}