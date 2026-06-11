package com.summerproject2026.DentalWave.service.impl;

import com.summerproject2026.DentalWave.dto.UserDto;
import com.summerproject2026.DentalWave.entity.User;
import com.summerproject2026.DentalWave.exception.DuplicateResourceException;
import com.summerproject2026.DentalWave.exception.ResourceNotFoundException;
import com.summerproject2026.DentalWave.mapper.UserMapper;
import com.summerproject2026.DentalWave.repository.RoleRepository;
import com.summerproject2026.DentalWave.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserServiceImpl.
 *
 * NOTE:
 * This is a Mockito unit test, not a Spring Boot integration test.
 * Spring Boot is not started, no database is used, and repositories
 * are replaced with mocks.
 *
 * UserMapper is not mocked because its methods are static.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    private UserServiceImpl userService;

    private User user;
    private UserDto userDto;

    /**
     * Creates shared test fixtures before each test.
     */
    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(
                userRepository,
                roleRepository,
                new UserMapper()
        );

        user = new User();
        user.setId(1L);
        user.setFirstName("Abigail");
        user.setLastName("Close");
        user.setUsername("abigail");
        user.setEmail("abigail@test.com");
        user.setPhoneNumber("9195551234");
        user.setEnabled(true);
        user.setRoles(new HashSet<>());

        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setFirstName("Abigail");
        userDto.setLastName("Close");
        userDto.setUsername("abigail");
        userDto.setEmail("abigail@test.com");
        userDto.setPhoneNumber("9195551234");
        userDto.setEnabled(true);
        userDto.setRoles(new HashSet<>());
    }

    /**
     * Verifies that createUser saves a new user when email and username are unique.
     */
    @Test
    @DisplayName("createUser — saves user when email and username are unique")
    void createUser_success() {
        when(userRepository.existsByEmail("abigail@test.com")).thenReturn(false);
        when(userRepository.existsByUsername("abigail")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.createUser(userDto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFirstName()).isEqualTo("Abigail");
        assertThat(result.getEmail()).isEqualTo("abigail@test.com");

        verify(userRepository).save(any(User.class));
    }

    /**
     * Verifies that createUser throws DuplicateResourceException
     * when the email already exists.
     */
    @Test
    @DisplayName("createUser — throws when email already exists")
    void createUser_duplicateEmail_throws() {
        when(userRepository.existsByEmail("abigail@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(userDto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("abigail@test.com");

        verify(userRepository, never()).save(any());
    }

    /**
     * Verifies that createUser throws DuplicateResourceException
     * when the username already exists.
     */
    @Test
    @DisplayName("createUser — throws when username already exists")
    void createUser_duplicateUsername_throws() {
        when(userRepository.existsByEmail("abigail@test.com")).thenReturn(false);
        when(userRepository.existsByUsername("abigail")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(userDto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("abigail");

        verify(userRepository, never()).save(any());
    }

    /**
     * Verifies that getUserById returns the mapped DTO when the user exists.
     */
    @Test
    @DisplayName("getUserById — returns DTO when user exists")
    void getUserById_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDto result = userService.getUserById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("abigail");
    }

    /**
     * Verifies that getUserById throws ResourceNotFoundException
     * when the user does not exist.
     */
    @Test
    @DisplayName("getUserById — throws when user not found")
    void getUserById_notFound_throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    /**
     * Verifies that getUserByEmail returns the mapped DTO when the email exists.
     */
    @Test
    @DisplayName("getUserByEmail — returns DTO when email exists")
    void getUserByEmail_success() {
        when(userRepository.findByEmail("abigail@test.com")).thenReturn(Optional.of(user));

        UserDto result = userService.getUserByEmail("abigail@test.com");

        assertThat(result.getEmail()).isEqualTo("abigail@test.com");
        assertThat(result.getUsername()).isEqualTo("abigail");
    }

    /**
     * Verifies that getUserByEmail throws ResourceNotFoundException
     * when no user exists with that email.
     */
    @Test
    @DisplayName("getUserByEmail — throws when email not found")
    void getUserByEmail_notFound_throws() {
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByEmail("missing@test.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("missing@test.com");
    }

    /**
     * Verifies that getAllUsers returns all users mapped to DTOs.
     */
    @Test
    @DisplayName("getAllUsers — returns mapped list")
    void getAllUsers_success() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserDto> result = userService.getAllUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFirstName()).isEqualTo("Abigail");
    }

    /**
     * Verifies that getUsersByRole returns users matching the given role.
     */
    @Test
    @DisplayName("getUsersByRole — returns mapped users")
    void getUsersByRole_success() {
        when(userRepository.findByRoles_NameIgnoreCase("ADMIN")).thenReturn(List.of(user));

        List<UserDto> result = userService.getUsersByRole("ADMIN");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("abigail");
    }

    /**
     * Verifies that searchUsers returns users matching the keyword.
     */
    @Test
    @DisplayName("searchUsers — returns mapped users")
    void searchUsers_success() {
        when(userRepository.searchByKeyword("Abi")).thenReturn(List.of(user));

        List<UserDto> result = userService.searchUsers("Abi");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFirstName()).isEqualTo("Abigail");
    }

    /**
     * Verifies that updateUser updates an existing user when the email is unchanged.
     */
    @Test
    @DisplayName("updateUser — updates user when email unchanged")
    void updateUser_success_emailUnchanged() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserDto result = userService.updateUser(1L, userDto);

        assertThat(result.getEmail()).isEqualTo("abigail@test.com");
        assertThat(result.getUsername()).isEqualTo("abigail");

        verify(userRepository).save(user);
    }

    /**
     * Verifies that updateUser checks duplicates when the email changes.
     */
    @Test
    @DisplayName("updateUser — throws when changed email already exists")
    void updateUser_duplicateChangedEmail_throws() {
        UserDto updateDto = new UserDto();
        updateDto.setEmail("new@test.com");
        updateDto.setUsername("abigail");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("new@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(1L, updateDto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("new@test.com");

        verify(userRepository, never()).save(any());
    }

    /**
     * Verifies that updateUser throws ResourceNotFoundException
     * when the user does not exist.
     */
    @Test
    @DisplayName("updateUser — throws when user not found")
    void updateUser_notFound_throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(99L, userDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    /**
     * Verifies that deleteUser deletes an existing user.
     */
    @Test
    @DisplayName("deleteUser — deletes existing user")
    void deleteUser_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        verify(userRepository).delete(user);
    }

    /**
     * Verifies that deleteUser throws ResourceNotFoundException
     * when the user does not exist.
     */
    @Test
    @DisplayName("deleteUser — throws when user not found")
    void deleteUser_notFound_throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(userRepository, never()).delete(any());
    }

    /**
     * Verifies that enableUser sets enabled to true and saves the user.
     */
    @Test
    @DisplayName("enableUser — enables user account")
    void enableUser_success() {
        user.setEnabled(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserDto result = userService.enableUser(1L);

        assertThat(user.getEnabled()).isTrue();
        assertThat(result.getEnabled()).isTrue();

        verify(userRepository).save(user);
    }

    /**
     * Verifies that disableUser sets enabled to false and saves the user.
     */
    @Test
    @DisplayName("disableUser — disables user account")
    void disableUser_success() {
        user.setEnabled(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserDto result = userService.disableUser(1L);

        assertThat(user.getEnabled()).isFalse();
        assertThat(result.getEnabled()).isFalse();

        verify(userRepository).save(user);
    }
}