package com.summerproject2026.DentalWave.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data transfer object used for user authentication.
 *
 * Users may log in using either their username or email address
 * along with their password.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginDto {

    /** Username or email address of the user. */
    private String username;

    /** User password. */
    private String password;
}