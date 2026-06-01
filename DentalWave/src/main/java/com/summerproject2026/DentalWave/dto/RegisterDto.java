package com.summerproject2026.DentalWave.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Information needed to register a new user.
 *
 * This DTO is used to capture input when creating a new account.
 * It includes a password because registration requires one, but
 * the password should not be included in UserDto responses.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDto {

    /** User's first name. */
    private String firstName;

    /** User's last name. */
    private String lastName;

    /** User's username. */
    private String username;

    /** User's email address. */
    private String email;

    /** User's phone number. */
    private String phoneNumber;

    /** User's password. */
    private String password;
}