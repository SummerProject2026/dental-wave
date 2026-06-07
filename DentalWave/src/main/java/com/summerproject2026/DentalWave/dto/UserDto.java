package com.summerproject2026.DentalWave.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Set;

/**
 * UserDto used for Data transfer object used to send user information to the frontend
 * @author demaris
 *
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    // Unique identifier for the user
    private Long id;

    // User's first name
    private String firstName;

    // User's last name
    private String lastName;

    // Username used to log in
    private String username;

    // User's email address
    private String email;

    // User's phone number
    private String phoneNumber;

    // Set of roles assigned to the user e.g. ROLE_HR, ROLE_ASSISTANT, ROLE_MANAGER
    // Set is used to prevent duplicate roles
    private Set<String> roles;

    // Indicates whether the user account is active or disabled
    private Boolean enabled;
}