package com.summerproject2026.DentalWave.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Collection;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;

    private String lastName;

    // Username must be unique and cannot be null
    @Column(nullable = false, unique = true)
    private String username;

    // Email must be unique and cannot be null
    @Column(nullable = false, unique = true)
    private String email;

    private String phoneNumber;

    // Stores the hashed password
    private String password;

    // Indicates if the account is active or disabled
    private Boolean enabled;

    // Loads all roles immediately when user is fetched
    @ManyToMany(fetch = FetchType.EAGER)
    // Join table linking users and roles
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Collection<Role> roles;

    public User() {
    }

    public User(Long id,
                String firstName,
                String lastName,
                String username,
                String email,
                String phoneNumber,
                String password,
                Boolean enabled,
                Collection<Role> roles) {

        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.enabled = enabled;
        this.roles = roles;
    }
}