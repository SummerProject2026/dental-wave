package com.summerproject2026.assistant_scheduler.dto;

import java.util.Set;

public class UserDto {

    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private String phoneNumber;

    private Set<String> roles;

    private Boolean enabled;

    public UserDto() {
    }

    public UserDto(Long id,
                   String firstName,
                   String lastName,
                   String email,
                   String phoneNumber,
                   Set<String> roles,
                   Boolean enabled) {

        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.roles = roles;
        this.enabled = enabled;
    }

}