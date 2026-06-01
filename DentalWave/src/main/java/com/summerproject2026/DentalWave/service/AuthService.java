package com.summerproject2026.DentalWave.service;

import com.summerproject2026.DentalWave.dto.JwtAuthResponse;


/**
 * NOTE:
 * Authentication requires different data than standard user management.
 *
 * UserDto is used throughout the application when displaying or managing
 * users and should not contain sensitive information such as passwords.
 *
 * To support authentication, we will need:
 *
 * LoginDto
 * - email
 * - password
 *
 * Used when a user logs into the system. The credentials are validated
 * and a JWT token is returned upon successful authentication.
 *
 * RegisterDto
 * - firstName
 * - lastName
 * - email
 * - phoneNumber
 * - password
 *
 * Used when creating a new user account. The password is needed during
 * registration but should never be exposed through UserDto responses.
 *
 * Separating these DTOs helps improve security, keeps responsibilities
 * clear, and follows common Spring Security and JWT authentication
 * design practices.
 */

/**
 * Authorization service
 */
public interface AuthService {

    /**
     * Authenticates a user and generates a JWT token.
     *
     * @param loginDto the user's login credentials
     * @return a JWT authentication response containing the access token
     */
    JwtAuthResponse login(LoginDto loginDto);

    /**
     * Registers a new user account.
     *
     * @param userDto the information for the new user
     * @return the newly registered user
     */
    UserDto register(RegisterDto registerDto);
}