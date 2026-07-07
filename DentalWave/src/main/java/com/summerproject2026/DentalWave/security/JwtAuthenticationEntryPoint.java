package com.summerproject2026.DentalWave.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

// Handles unauthorized access attempts by returning a 401 error
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    // Triggered when an unauthenticated user tries to access a protected resource
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"Please log in to continue.\"}");
    }
}
