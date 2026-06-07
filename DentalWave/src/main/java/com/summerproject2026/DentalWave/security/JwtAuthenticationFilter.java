package com.summerproject2026.DentalWave.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Intercepts every HTTP request and validates the JWT token if present
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // Used to validate and extract data from the JWT token
    private final JwtTokenProvider jwtTokenProvider;

    // Used to load user details from the database
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Extract the JWT token from the request header
        String token = getTokenFromRequest(request);

        // If token exists and is valid, authenticate the user
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {

            // Get the username from the token
            String username = jwtTokenProvider.getUsername(token);

            // Load user details from the database
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

            // Create authentication object with user details and authorities
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            // Attach request details to the authentication object
            authenticationToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));

            // Set the authentication in the security context
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        // Continue the filter chain
        filterChain.doFilter(request, response);
    }

    // Extracts the Bearer token from the Authorization header
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}
