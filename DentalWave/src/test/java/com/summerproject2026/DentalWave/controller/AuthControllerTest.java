package com.summerproject2026.DentalWave.controller;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.summerproject2026.DentalWave.dto.JwtAuthResponse;
import com.summerproject2026.DentalWave.dto.LoginDto;
import com.summerproject2026.DentalWave.dto.RegisterDto;
import com.summerproject2026.DentalWave.dto.UserDto;
import com.summerproject2026.DentalWave.exception.DuplicateResourceException;
import com.summerproject2026.DentalWave.repository.UserRepository;
import com.summerproject2026.DentalWave.service.AuthService;

/**
 * Integration tests for AuthController.
 * Tests login and register through the service layer
 * using the real database.
 *
 * @author Demaris
 */
@SpringBootTest
class AuthControllerTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanUp() {
        // Remove test users before each test to avoid duplicates
        userRepository.findByEmail("john@dentalwave.com")
                .ifPresent(userRepository::delete);
    }

    // ==================== LOGIN TESTS ====================

    /**
     * Valid login should return a JWT token.
     */
    @Test
    void loginWithValidCredentials() {
        LoginDto loginDto = new LoginDto("admin", "Password123");

        JwtAuthResponse response = authService.login(loginDto);

        assertNotNull(response.getAccessToken());
        assertEquals("ROLE_ADMIN", response.getRole());
    }

    /**
     * Invalid login should throw an exception.
     */
    @Test
    void loginWithInvalidCredentials() {
        LoginDto loginDto = new LoginDto("wronguser", "wrongpassword");

        assertThrows(Exception.class, () -> authService.login(loginDto));
    }

    // ==================== REGISTER TESTS ====================

    /**
     * Valid registration should return the new user.
     */
    @Test
    void registerNewUser() {
        RegisterDto registerDto = new RegisterDto(
                "John", "Doe", "johndoe",
                "john@dentalwave.com", "1234567890", "password123");

        UserDto savedUser = authService.register(registerDto);

        assertNotNull(savedUser);
        assertEquals("johndoe", savedUser.getUsername());
        assertEquals("john@dentalwave.com", savedUser.getEmail());
    }

    /**
     * Duplicate email registration should throw DuplicateResourceException.
     */
    @Test
    void registerWithDuplicateEmail() {
        // First register the user
        RegisterDto firstRegister = new RegisterDto(
                "John", "Doe", "johndoe",
                "john@dentalwave.com", "1234567890", "password123");
        authService.register(firstRegister);

        // Try to register again with same email
        RegisterDto duplicateRegister = new RegisterDto(
                "Jane", "Doe", "janedoe",
                "john@dentalwave.com", "1234567890", "password123");

        assertThrows(DuplicateResourceException.class,
                () -> authService.register(duplicateRegister));
    }
}