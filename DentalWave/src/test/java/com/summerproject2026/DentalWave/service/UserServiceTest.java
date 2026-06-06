package com.summerproject2026.DentalWave.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.summerproject2026.DentalWave.dto.UserDto;
import com.summerproject2026.DentalWave.exception.DuplicateResourceException;
import com.summerproject2026.DentalWave.exception.ResourceNotFoundException;
import com.summerproject2026.DentalWave.repository.UserRepository;

/**
 * Tests the functionality of the UserService.
 */
@SpringBootTest
class UserServiceTest {

    /** Reference to the user service. */
    @Autowired
    private UserService userService;

    /** Reference to the user repository. */
    @Autowired
    private UserRepository userRepository;

    /**
     * Clears the database before each test.
     */
    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    /**
     * Creates a valid UserDto for testing.
     *
     * @return a valid UserDto
     */
    private UserDto createUserDto() {
        UserDto user = new UserDto();

        user.setFirstName("Abigail");
        user.setLastName("Close");
        user.setUsername("abigail");
        user.setEmail("abigail@test.com");
        user.setPhoneNumber("9195551234");
        user.setEnabled(true);

        return user;
    }

    /**
     * Tests creating a user.
     */
    @Test
    void testCreateUser() {

        UserDto savedUser = userService.createUser(createUserDto());

        assertNotNull(savedUser.getId());
        assertEquals("Abigail", savedUser.getFirstName());
        assertEquals("Close", savedUser.getLastName());
        assertEquals("abigail", savedUser.getUsername());
        assertEquals("abigail@test.com", savedUser.getEmail());
    }

    /**
     * Tests creating a duplicate user.
     */
    @Test
    void testCreateDuplicateUser() {

        userService.createUser(createUserDto());

        assertThrows(
                DuplicateResourceException.class,
                () -> userService.createUser(createUserDto()));
    }

    /**
     * Tests retrieving a user by ID.
     */
    @Test
    void testGetUserById() {

        UserDto savedUser = userService.createUser(createUserDto());

        UserDto foundUser = userService.getUserById(savedUser.getId());

        assertEquals(savedUser.getId(), foundUser.getId());
        assertEquals(savedUser.getUsername(), foundUser.getUsername());
    }

    /**
     * Tests retrieving a user that does not exist.
     */
    @Test
    void testGetUserByIdNotFound() {

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getUserById(999L));
    }

    /**
     * Tests retrieving all users.
     */
    @Test
    void testGetAllUsers() {

        userService.createUser(createUserDto());

        List<UserDto> users = userService.getAllUsers();

        assertEquals(1, users.size());
    }

    /**
     * Tests deleting a user.
     */
    @Test
    void testDeleteUser() {

        UserDto savedUser = userService.createUser(createUserDto());

        userService.deleteUser(savedUser.getId());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getUserById(savedUser.getId()));
    }

    /**
     * Tests enabling a user account.
     */
    @Test
    void testEnableUser() {

        UserDto savedUser = userService.createUser(createUserDto());

        UserDto updatedUser = userService.enableUser(savedUser.getId());

        assertTrue(updatedUser.getEnabled());
    }

    /**
     * Tests disabling a user account.
     */
    @Test
    void testDisableUser() {

        UserDto savedUser = userService.createUser(createUserDto());

        UserDto updatedUser = userService.disableUser(savedUser.getId());

        assertFalse(updatedUser.getEnabled());
    }
}