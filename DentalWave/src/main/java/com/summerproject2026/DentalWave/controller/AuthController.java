package com.summerproject2026.DentalWave.controller;

import com.summerproject2026.DentalWave.dto.JwtAuthResponse;
import com.summerproject2026.DentalWave.dto.LoginDto;
import com.summerproject2026.DentalWave.dto.RegisterDto;
import com.summerproject2026.DentalWave.dto.UserDto;
import com.summerproject2026.DentalWave.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for handling authentication requests.
 * Handles login, registration, and role-based registration.
 */
@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    // Service that contains the authentication business logic
    private AuthService authService;

    /**
     * POST /api/auth/login
     * Accepts username or email and password, returns a JWT token.
     * No authentication required - this is the public login endpoint.
     *
     * @param loginDto the login credentials
     * @return 200 OK with JWT token and user details
     */
    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> login(@RequestBody LoginDto loginDto) {

        // Delegate login logic to the auth service
        JwtAuthResponse jwtAuthResponse = authService.login(loginDto);

        // Return 200 OK with the JWT token in the response body
        return ResponseEntity.ok(jwtAuthResponse);
    }

    /**
     * POST /api/auth/register
     * Accepts user info and creates a new assistant account.
     * No authentication required - open registration for assistants.
     * New users are automatically assigned ROLE_ASSISTANT.
     *
     * @param registerDto the new user's registration information
     * @return 201 Created with the new user details
     */
    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@RequestBody RegisterDto registerDto) {

        // Delegate registration logic to the auth service
        UserDto savedUser = authService.register(registerDto);

        // Return 201 Created with the new user in the response body
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    /**
     * POST /api/auth/register/hr
     * Admin-only endpoint to register a new HR user.
     * The new user is automatically assigned ROLE_HR.
     * Requires ROLE_ADMIN to access.
     *
     * @param registerDto the new HR user's registration information
     * @return 201 Created with the new HR user details
     */
    @PostMapping("/register/hr")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserDto> registerHR(@RequestBody RegisterDto registerDto) {

        // Delegate HR registration to the auth service with ROLE_HR
        UserDto savedUser = authService.registerWithRole(registerDto, "ROLE_HR");

        // Return 201 Created with the new HR user in the response body
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }
}