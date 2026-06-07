package com.summerproject2026.DentalWave.controller;

import com.summerproject2026.DentalWave.dto.JwtAuthResponse;
import com.summerproject2026.DentalWave.dto.LoginDto;
import com.summerproject2026.DentalWave.dto.RegisterDto;
import com.summerproject2026.DentalWave.dto.UserDto;
import com.summerproject2026.DentalWave.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Handles all authentication related HTTP requests (login and register)
@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    // Service that contains the authentication business logic
    private AuthService authService;

    // POST /api/auth/login - accepts credentials and returns a JWT token
    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> login(@RequestBody LoginDto loginDto) {

        // Delegate login logic to the auth service
        JwtAuthResponse jwtAuthResponse = authService.login(loginDto);

        // Return 200 OK with the JWT token in the response body
        return ResponseEntity.ok(jwtAuthResponse);
    }

    // POST /api/auth/register - accepts user info and creates a new account
    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@RequestBody RegisterDto registerDto) {

        // Delegate registration logic to the auth service
        UserDto savedUser = authService.register(registerDto);

        // Return 201 Created with the new user in the response body
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }
}