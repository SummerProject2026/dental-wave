package com.summerproject2026.DentalWave.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.summerproject2026.DentalWave.dto.JwtAuthResponse;
import com.summerproject2026.DentalWave.dto.LoginDto;
import com.summerproject2026.DentalWave.dto.RegisterDto;
import com.summerproject2026.DentalWave.dto.UserDto;
import com.summerproject2026.DentalWave.exception.DuplicateResourceException;
import com.summerproject2026.DentalWave.repository.UserRepository;
import com.summerproject2026.DentalWave.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Integration tests for AuthController/AuthService behavior.
 *
 * These tests use the real Spring context and database.
 */
@SpringBootTest
class AuthControllerTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Valid login should return a JWT token.
     */
    @Test
    void loginWithValidCredentials() {
        String uniqueId = String.valueOf(System.nanoTime());

        String username = "admin_test_login_" + uniqueId;
        String email = "admin_login_" + uniqueId + "@dentalwave.com";
        String password = "Password123";

        RegisterDto registerDto = new RegisterDto(
                "Admin", "User", username,
                email, "1234567890", password);

        authService.register(registerDto);

        LoginDto loginDto = new LoginDto(username, password);

        JwtAuthResponse response = authService.login(loginDto);

        assertNotNull(response.getAccessToken());
    }

    /**
     * Invalid login should throw an exception.
     */
    @Test
    void loginWithInvalidCredentials() {
        LoginDto loginDto = new LoginDto("wronguser", "wrongpassword");

        assertThrows(Exception.class, () -> authService.login(loginDto));
    }

    /**
     * Valid registration should return the new user.
     */
    @Test
    void registerNewUser() {
        String uniqueId = String.valueOf(System.nanoTime());

        String username = "johndoe_" + uniqueId;
        String email = "john_" + uniqueId + "@dentalwave.com";

        RegisterDto registerDto = new RegisterDto(
                "John", "Doe", username,
                email, "1234567890", "password123");

        UserDto savedUser = authService.register(registerDto);

        assertNotNull(savedUser);
        assertEquals(username, savedUser.getUsername());
        assertEquals(email, savedUser.getEmail());
    }

    /**
     * Duplicate email registration should throw DuplicateResourceException.
     */
    @Test
    void registerWithDuplicateEmail() {
        String uniqueId = String.valueOf(System.nanoTime());

        String email = "john_duplicate_" + uniqueId + "@dentalwave.com";

        RegisterDto firstRegister = new RegisterDto(
                "John", "Doe", "johndoe_" + uniqueId,
                email, "1234567890", "password123");

        authService.register(firstRegister);

        RegisterDto duplicateRegister = new RegisterDto(
                "Jane", "Doe", "janedoe_" + uniqueId,
                email, "1234567890", "password123");

        assertThrows(DuplicateResourceException.class,
                () -> authService.register(duplicateRegister));
    }
}