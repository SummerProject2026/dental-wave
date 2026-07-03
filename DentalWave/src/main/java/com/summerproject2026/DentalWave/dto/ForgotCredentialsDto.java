package com.summerproject2026.DentalWave.dto;

/**
 * Request payload for the forgot-username and forgot-password flows.
 *
 * The requester must supply their first name, last name, and email.
 * All three must match an existing user record (case-insensitive)
 * before the system sends any recovery email.
 */
public class ForgotCredentialsDto {

    /** First name on the employee's account */
    private String firstName;

    /** Last name on the employee's account */
    private String lastName;

    /** Email address on the employee's account */
    private String email;

    public ForgotCredentialsDto() {}

    public ForgotCredentialsDto(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}