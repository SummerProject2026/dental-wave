package com.summerproject2026.DentalWave.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Collection;
import java.util.HashSet;

/**
 * Represents an application login account.
 *
 * User stores authentication fields such as username, password, and roles.
 * Employee-specific details are stored separately on the Employee entity.
 */
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
    private Collection<Role> roles = new HashSet<>();

    /** Default constructor required by JPA. */
    public User() {}

    /**
     * Creates a user account with identity, contact, and role information.
     *
     * @param id user id, usually assigned by the database
     * @param firstName user's first name
     * @param lastName user's last name
     * @param username unique username used to log in
     * @param email unique email address
     * @param phoneNumber contact phone number
     * @param password hashed password
     * @param enabled whether the account can log in
     * @param roles security roles assigned to the account
     */
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
