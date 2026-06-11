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
 * @author Demaris
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

    // =========================================================================
    // GET /api/users/{id}
    // =========================================================================

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
     * when the user does not exist in the system.
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("GET /api/users/{id} → 404 Not Found when user does not exist")
    void getUserById_returns404_whenNotFound() throws Exception {
        // Mock service to throw ResourceNotFoundException for unknown user
        when(userService.getUserById(99L))
                .thenThrow(new ResourceNotFoundException("User not found with id: 99"));

        // Perform GET request and verify 404 response
        mockMvc.perform(get("/api/users/{id}", 99L))
                .andExpect(status().isNotFound());
    }


    // GET /api/users


    /**
     * Verifies that GET /api/users returns 200 OK
     * with a list of all users in the system.
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("GET /api/users → 200 OK with list of all users")
    void getAllUsers_returns200WithList() throws Exception {
        // Mock service to return two users
        when(userService.getAllUsers()).thenReturn(List.of(userDto, userDto2));

        // Perform GET request and verify both users are returned
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
     * with an empty list when no users exist in the system.
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
     * Verifies that GET /api/users returns the correct
     * number of users in the response.
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("GET /api/users → returns correct count of users")
    void getAllUsers_returnsCorrectCount() throws Exception {
        // Mock service to return two users
        when(userService.getAllUsers()).thenReturn(List.of(userDto, userDto2));

        // Perform GET request and verify count is 2
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)));
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
     * when the user does not exist in the system.
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("PUT /api/users/{id} → 404 Not Found when user does not exist")
    void updateUser_returns404_whenNotFound() throws Exception {
        // Mock service to throw ResourceNotFoundException for unknown user
        when(userService.updateUser(eq(99L), any(UserDto.class)))
                .thenThrow(new ResourceNotFoundException("User not found with id: 99"));

        // Perform PUT request for non-existent user and verify 404 response
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

        // Verify service was never called when body is missing
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
                .andExpect(content().string("User deleted successfully"));

        // Verify service was called exactly once
        verify(userService, times(1)).deleteUser(1L);
    }

    /**
     * Verifies that DELETE /api/users/{id} returns 404 Not Found
     * when the user does not exist in the system.
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("DELETE /api/users/{id} → 404 Not Found when user does not exist")
    void deleteUser_returns404_whenNotFound() throws Exception {
        // Mock service to throw ResourceNotFoundException for unknown user
        doThrow(new ResourceNotFoundException("User not found with id: 99"))
                .when(userService).deleteUser(99L);

        // Perform DELETE request for non-existent user and verify 404 response
        mockMvc.perform(delete("/api/users/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    /**
     * Verifies that DELETE /api/users/{id} calls the service
     * exactly once with the correct user id.
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("DELETE /api/users/{id} → service is called exactly once")
    void deleteUser_callsServiceExactlyOnce() throws Exception {
        // Mock service to do nothing on delete
        doNothing().when(userService).deleteUser(1L);

        // Perform DELETE request
        mockMvc.perform(delete("/api/users/{id}", 1L))
                .andExpect(status().isOk());

        // Verify service was called exactly once with correct id
        verify(userService, times(1)).deleteUser(1L);
    }
}