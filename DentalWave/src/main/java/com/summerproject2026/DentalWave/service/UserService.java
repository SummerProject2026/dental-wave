package com.summerproject2026.DentalWave.service;

import java.util.List;
import com.summerproject2026.DentalWave.dto.UserDto;

/**
 * Service interface for managing users within the DentalWave system.
 *
 * Responsibilities:
 * <ul>
 *   <li>Create, retrieve, update, and delete users.</li>
 *   <li>Search for users by username, role, or keyword.</li>
 *   <li>Enable and disable user accounts.</li>
 * </ul>
 */
public interface UserService {

    /**
     * Creates a new user.
     *
     * @param userDto the user information to create
     * @return the newly created user
     */
    UserDto createUser(UserDto userDto);

    /**
     * Retrieves a user by their unique identifier.
     *
     * @param id the user id
     * @return the matching user
     */
    UserDto getUserById(Long id);

    /**
     * Retrieves all users in the system.
     *
     * @return a list of all users
     */
    List<UserDto> getAllUsers();

    /**
     * Updates an existing user's information.
     *
     * @param id the id of the user to update
     * @param userDto the updated user information
     * @return the updated user
     */
    UserDto updateUser(Long id, UserDto userDto);

    /**
     * Deletes a user from the system.
     *
     * @param id the id of the user to delete
     */
    void deleteUser(Long id);

    /**
     * Retrieves a user by their username.
     *
     * @param username the username to search for
     * @return the matching user
     */
    UserDto getUserByUsername(String username);

    /**
     * Retrieves a user by their email address.
     *
     * @param email the email address to search for
     * @return the matching user
     */
    UserDto getUserByEmail(String email);

    /**
     * Retrieves all users assigned to a specific role.
     *
     * @param role the role name
     * @return a list of users with the specified role
     */
    List<UserDto> getUsersByRole(String role);

    /**
     * Searches for users matching the provided keyword.
     *
     * @param keyword the search term
     * @return a list of matching users
     */
    List<UserDto> searchUsers(String keyword);

    /**
     * Enables a user account.
     *
     * @param id the id of the user to enable
     * @return the updated user with enabled status set to true
     */
    UserDto enableUser(Long id);

    /**
     * Disables a user account.
     *
     * @param id the id of the user to disable
     * @return the updated user with enabled status set to false
     */
    UserDto disableUser(Long id);
}