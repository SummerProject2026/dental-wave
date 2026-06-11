package com.summerproject2026.DentalWave.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.summerproject2026.DentalWave.dto.UserDto;
import com.summerproject2026.DentalWave.exception.ResourceNotFoundException;
import com.summerproject2026.DentalWave.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web-layer unit tests for {@link UserController}.
 *
 * <p>Uses {@code @WebMvcTest} to load only the controller layer.
 * {@link UserService} is replaced with a Mockito mock via {@code @MockitoBean}
 * so tests are completely isolated from the business and persistence layers.</p>
 *
 * <p>Spring Security filters are disabled via
 * {@code @AutoConfigureMockMvc(addFilters = false)} to avoid 401/403
 * responses that would break these unit tests.</p>
 *
 *
 */
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    /**
     * MockMvc instance used to perform HTTP requests in tests
     * without starting a real server.
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * Jackson ObjectMapper used to serialize Java objects
     * to JSON for request bodies.
     */
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Mocked UserService — replaces the real service bean
     * so no database calls are made during tests.
     */
    @MockitoBean
    private UserService userService;

    /**
     * Mocked JwtTokenProvider — required by Spring Security
     * context loaded by @WebMvcTest.
     */
    @MockitoBean
    private com.summerproject2026.DentalWave.security.JwtTokenProvider jwtTokenProvider;

    /**
     * Mocked JwtAuthenticationFilter — required by Spring Security
     * context loaded by @WebMvcTest.
     */
    @MockitoBean
    private com.summerproject2026.DentalWave.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    /** First shared test user fixture. */
    private UserDto userDto;

    /** Second shared test user fixture. */
    private UserDto userDto2;

    /**
     * Sets up shared test data before each test.
     * Creates two UserDto objects representing different users.
     */
    @BeforeEach
    void setUp() {
        // Set up first user — Alice with ROLE_ASSISTANT
        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setFirstName("Alice");
        userDto.setLastName("Smith");
        userDto.setUsername("alice");
        userDto.setEmail("alice@dentalwave.com");
        userDto.setPhoneNumber("704-555-0101");
        userDto.setRoles(Set.of("ROLE_ASSISTANT"));
        userDto.setEnabled(true);

        // Set up second user — Bob with ROLE_HR
        userDto2 = new UserDto();
        userDto2.setId(2L);
        userDto2.setFirstName("Bob");
        userDto2.setLastName("Jones");
        userDto2.setUsername("bob");
        userDto2.setEmail("bob@dentalwave.com");
        userDto2.setPhoneNumber("704-555-0202");
        userDto2.setRoles(Set.of("ROLE_HR"));
        userDto2.setEnabled(true);
    }

    /**
     * Verifies that POST /api/users returns 201 Created
     * with the new user in the response body.
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("POST /api/users → 201 Created with new user")
    void createUser_returns201() throws Exception {
        // Mock service to return created user
        when(userService.createUser(any(UserDto.class))).thenReturn(userDto);

        // Perform POST request and verify response
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("alice")))
                .andExpect(jsonPath("$.email", is("alice@dentalwave.com")));

        // Verify service was called exactly once
        verify(userService, times(1)).createUser(any(UserDto.class));
    }

    /**
     * Verifies that POST /api/users returns 400 Bad Request
     * when the request body is missing.
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("POST /api/users → 400 Bad Request when body is missing")
    void createUser_returns400_whenBodyMissing() throws Exception {
        // Perform POST request without body
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        // Verify service was never called
        verify(userService, never()).createUser(any());
    }

    /**
     * Verifies that GET /api/users/{id} returns 200 OK
     * with the correct user data when the user exists.
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("GET /api/users/{id} → 200 OK with matching user")
    void getUserById_returns200_whenExists() throws Exception {
        // Mock service to return user with id 1
        when(userService.getUserById(1L)).thenReturn(userDto);

        // Perform GET request and verify response
        mockMvc.perform(get("/api/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("alice")))
                .andExpect(jsonPath("$.email", is("alice@dentalwave.com")));

        // Verify service was called with correct id
        verify(userService, times(1)).getUserById(1L);
    }

    /**
     * Verifies that GET /api/users/{id} returns 404 Not Found
     * when the user does not exist.
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("GET /api/users/{id} → 404 Not Found when user does not exist")
    void getUserById_returns404_whenNotFound() throws Exception {
        // Mock service to throw ResourceNotFoundException
        when(userService.getUserById(99L))
                .thenThrow(new ResourceNotFoundException("User not found with id: 99"));

        // Perform GET request and verify 404 response
        mockMvc.perform(get("/api/users/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    /**
     * Verifies that GET /api/users returns 200 OK
     * with a list of all users.
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("GET /api/users → 200 OK with list of all users")
    void getAllUsers_returns200WithList() throws Exception {
        // Mock service to return two users
        when(userService.getAllUsers()).thenReturn(List.of(userDto, userDto2));

        // Perform GET request and verify both users returned
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username", is("alice")))
                .andExpect(jsonPath("$[1].username", is("bob")));

        // Verify service was called exactly once
        verify(userService, times(1)).getAllUsers();
    }

    /**
     * Verifies that GET /api/users returns 200 OK
     * with an empty list when no users exist.
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("GET /api/users → 200 OK with empty list when no users exist")
    void getAllUsers_returns200WithEmptyList() throws Exception {
        // Mock service to return empty list
        when(userService.getAllUsers()).thenReturn(List.of());

        // Perform GET request and verify empty list returned
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    /**
     * Verifies that PUT /api/users/{id} returns 200 OK
     * with the updated user data when the user exists.
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("PUT /api/users/{id} → 200 OK with updated user")
    void updateUser_returns200_whenExists() throws Exception {
        // Create updated user dto with new first name
        UserDto updatedDto = new UserDto();
        updatedDto.setId(1L);
        updatedDto.setFirstName("Alice Updated");
        updatedDto.setLastName("Smith");
        updatedDto.setUsername("alice");
        updatedDto.setEmail("alice@dentalwave.com");
        updatedDto.setEnabled(true);

        // Mock service to return updated user
        when(userService.updateUser(eq(1L), any(UserDto.class))).thenReturn(updatedDto);

        // Perform PUT request and verify updated name in response
        mockMvc.perform(put("/api/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("Alice Updated")));

        // Verify service was called with correct id
        verify(userService, times(1)).updateUser(eq(1L), any(UserDto.class));
    }

    /**
     * Verifies that PUT /api/users/{id} returns 404 Not Found
     * when the user does not exist.
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("PUT /api/users/{id} → 404 Not Found when user does not exist")
    void updateUser_returns404_whenNotFound() throws Exception {
        // Mock service to throw ResourceNotFoundException
        when(userService.updateUser(eq(99L), any(UserDto.class)))
                .thenThrow(new ResourceNotFoundException("User not found with id: 99"));

        // Perform PUT request and verify 404 response
        mockMvc.perform(put("/api/users/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isNotFound());
    }

    /**
     * Verifies that PUT /api/users/{id} returns 400 Bad Request
     * when the request body is missing.
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("PUT /api/users/{id} → 400 Bad Request when body is missing")
    void updateUser_returns400_whenBodyMissing() throws Exception {
        // Perform PUT request without body
        mockMvc.perform(put("/api/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        // Verify service was never called
        verify(userService, never()).updateUser(any(), any());
    }

    /**
     * Verifies that DELETE /api/users/{id} returns 200 OK
     * with a confirmation message when the user exists.
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("DELETE /api/users/{id} → 200 OK with confirmation message")
    void deleteUser_returns200_whenExists() throws Exception {
        // Mock service to do nothing on delete
        doNothing().when(userService).deleteUser(1L);

        // Perform DELETE request and verify confirmation message
        mockMvc.perform(delete("/api/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string("User with id 1 deleted successfully."));

        // Verify service was called exactly once
        verify(userService, times(1)).deleteUser(1L);
    }

    /**
     * Verifies that DELETE /api/users/{id} returns 404 Not Found
     * when the user does not exist.
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("DELETE /api/users/{id} → 404 Not Found when user does not exist")
    void deleteUser_returns404_whenNotFound() throws Exception {
        // Mock service to throw ResourceNotFoundException
        doThrow(new ResourceNotFoundException("User not found with id: 99"))
                .when(userService).deleteUser(99L);

        // Perform DELETE request and verify 404 response
        mockMvc.perform(delete("/api/users/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    /**
     * Verifies that GET /api/users/username/{username} returns 200 OK
     * with the correct user when the username exists.
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("GET /api/users/username/{username} → 200 OK with matching user")
    void getUserByUsername_returns200_whenExists() throws Exception {
        // Mock service to return user with username alice
        when(userService.getUserByUsername("alice")).thenReturn(userDto);

        // Perform GET request and verify response
        mockMvc.perform(get("/api/users/username/{username}", "alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("alice")))
                .andExpect(jsonPath("$.email", is("alice@dentalwave.com")));

        // Verify service was called with correct username
        verify(userService, times(1)).getUserByUsername("alice");
    }

    /**
     * Verifies that GET /api/users/username/{username} returns 404 Not Found
     * when the username does not exist.
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("GET /api/users/username/{username} → 404 Not Found when username does not exist")
    void getUserByUsername_returns404_whenNotFound() throws Exception {
        // Mock service to throw ResourceNotFoundException
        when(userService.getUserByUsername("unknown"))
                .thenThrow(new ResourceNotFoundException("User not found with username: unknown"));

        // Perform GET request and verify 404 response
        mockMvc.perform(get("/api/users/username/{username}", "unknown"))
                .andExpect(status().isNotFound());
    }

    /**
     * Verifies that GET /api/users/role/{role} returns 200 OK
     * with users matching the given role.
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("GET /api/users/role/{role} → 200 OK with matching users")
    void getUsersByRole_returns200() throws Exception {
        // Mock service to return one user with ROLE_ASSISTANT
        when(userService.getUsersByRole("ROLE_ASSISTANT")).thenReturn(List.of(userDto));

        // Perform GET request and verify response
        mockMvc.perform(get("/api/users/role/{role}", "ROLE_ASSISTANT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username", is("alice")));

        // Verify service was called with correct role
        verify(userService, times(1)).getUsersByRole("ROLE_ASSISTANT");
    }

    /**
     * Verifies that GET /api/users/role/{role} returns 200 OK
     * with an empty list when no users have the given role.
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("GET /api/users/role/{role} → 200 OK with empty list when no users have role")
    void getUsersByRole_returns200WithEmptyList() throws Exception {
        // Mock service to return empty list for unknown role
        when(userService.getUsersByRole("ROLE_UNKNOWN")).thenReturn(List.of());

        // Perform GET request and verify empty list returned
        mockMvc.perform(get("/api/users/role/{role}", "ROLE_UNKNOWN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    /**
     * Verifies that GET /api/users/search returns 200 OK
     * with users matching the search keyword.
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("GET /api/users/search?keyword= → 200 OK with matching users")
    void searchUsers_returns200WithMatchingUsers() throws Exception {
        // Mock service to return one user matching keyword alice
        when(userService.searchUsers("alice")).thenReturn(List.of(userDto));

        // Perform GET request with keyword parameter
        mockMvc.perform(get("/api/users/search").param("keyword", "alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username", is("alice")));

        // Verify service was called with correct keyword
        verify(userService, times(1)).searchUsers("alice");
    }

    /**
     * Verifies that GET /api/users/search returns 200 OK
     * with an empty list when no users match the keyword.
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("GET /api/users/search?keyword= → 200 OK with empty list when no match")
    void searchUsers_returns200WithEmptyList_whenNoMatch() throws Exception {
        // Mock service to return empty list for unknown keyword
        when(userService.searchUsers("xyzzy")).thenReturn(List.of());

        // Perform GET request and verify empty list returned
        mockMvc.perform(get("/api/users/search").param("keyword", "xyzzy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    /**
     * Verifies that PATCH /api/users/{id}/enable returns 200 OK
     * with enabled set to true when the user exists.
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("PATCH /api/users/{id}/enable → 200 OK with enabled=true")
    void enableUser_returns200_withEnabledTrue() throws Exception {
        // Set user as enabled
        userDto.setEnabled(true);

        // Mock service to return enabled user
        when(userService.enableUser(1L)).thenReturn(userDto);

        // Perform PATCH request and verify enabled is true
        mockMvc.perform(patch("/api/users/{id}/enable", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled", is(true)));

        // Verify service was called with correct id
        verify(userService, times(1)).enableUser(1L);
    }

    /**
     * Verifies that PATCH /api/users/{id}/enable returns 404 Not Found
     * when the user does not exist.
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("PATCH /api/users/{id}/enable → 404 Not Found when user does not exist")
    void enableUser_returns404_whenNotFound() throws Exception {
        // Mock service to throw ResourceNotFoundException
        when(userService.enableUser(99L))
                .thenThrow(new ResourceNotFoundException("User not found with id: 99"));

        // Perform PATCH request and verify 404 response
        mockMvc.perform(patch("/api/users/{id}/enable", 99L))
                .andExpect(status().isNotFound());
    }

    /**
     * Verifies that PATCH /api/users/{id}/disable returns 200 OK
     * with enabled set to false when the user exists.
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("PATCH /api/users/{id}/disable → 200 OK with enabled=false")
    void disableUser_returns200_withEnabledFalse() throws Exception {
        // Set user as disabled
        userDto.setEnabled(false);

        // Mock service to return disabled user
        when(userService.disableUser(1L)).thenReturn(userDto);

        // Perform PATCH request and verify enabled is false
        mockMvc.perform(patch("/api/users/{id}/disable", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled", is(false)));

        // Verify service was called with correct id
        verify(userService, times(1)).disableUser(1L);
    }

    /**
     * Verifies that PATCH /api/users/{id}/disable returns 404 Not Found
     * when the user does not exist.
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("PATCH /api/users/{id}/disable → 404 Not Found when user does not exist")
    void disableUser_returns404_whenNotFound() throws Exception {
        // Mock service to throw ResourceNotFoundException
        when(userService.disableUser(99L))
                .thenThrow(new ResourceNotFoundException("User not found with id: 99"));

        // Perform PATCH request and verify 404 response
        mockMvc.perform(patch("/api/users/{id}/disable", 99L))
                .andExpect(status().isNotFound());
    }
}