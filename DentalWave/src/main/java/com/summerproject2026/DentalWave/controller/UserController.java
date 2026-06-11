package com.summerproject2026.DentalWave.controller;

import com.summerproject2026.DentalWave.dto.UserDto;
import com.summerproject2026.DentalWave.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing users.
 * Admin only endpoints for user management.
 * Base path: /api/users
 *
 * @author Demaris
 */
@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController {

    /** Service that contains the user business logic */
    private final UserService userService;

    /**
     * POST /api/users - create a new user
     * Admin only endpoint
     *
     * @param userDto the user data to create
     * @return 201 Created with the new user
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createUser(userDto));
    }

    /**
     * GET /api/users/{id} - get user by id
     * Admin only endpoint
     *
     * @param id the user id
     * @return 200 OK with the user
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /**
     * GET /api/users - get all users
     * Admin only endpoint
     *
     * @return 200 OK with list of all users
     */
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * PUT /api/users/{id} - update user
     * Admin only endpoint
     *
     * @param id the user id to update
     * @param userDto the updated user data
     * @return 200 OK with the updated user
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable Long id,
            @RequestBody UserDto userDto) {
        return ResponseEntity.ok(userService.updateUser(id, userDto));
    }

    /**
     * DELETE /api/users/{id} - delete user
     * Admin only endpoint
     *
     * @param id the user id to delete
     * @return 200 OK with confirmation message
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User with id " + id + " deleted successfully.");
    }

    /**
     * GET /api/users/username/{username} - get user by username
     * Admin only endpoint
     *
     * @param username the username to search for
     * @return 200 OK with the matching user
     */
    @GetMapping("/username/{username}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserDto> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    /**
     * GET /api/users/role/{role} - get users by role
     * Admin only endpoint
     *
     * @param role the role name to filter by
     * @return 200 OK with list of matching users
     */
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<UserDto>> getUsersByRole(@PathVariable String role) {
        return ResponseEntity.ok(userService.getUsersByRole(role));
    }

    /**
     * GET /api/users/search?keyword= - search users by keyword
     * Admin only endpoint
     *
     * @param keyword the search term
     * @return 200 OK with list of matching users
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<UserDto>> searchUsers(@RequestParam String keyword) {
        return ResponseEntity.ok(userService.searchUsers(keyword));
    }

    /**
     * PATCH /api/users/{id}/enable - enable user account
     * Admin only endpoint
     *
     * @param id the user id to enable
     * @return 200 OK with the updated user
     */
    @PatchMapping("/{id}/enable")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserDto> enableUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.enableUser(id));
    }

    /**
     * PATCH /api/users/{id}/disable - disable user account
     * Admin only endpoint
     *
     * @param id the user id to disable
     * @return 200 OK with the updated user
     */
    @PatchMapping("/{id}/disable")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserDto> disableUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.disableUser(id));
    }
}