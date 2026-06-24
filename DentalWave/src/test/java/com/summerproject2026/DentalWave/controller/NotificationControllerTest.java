package com.summerproject2026.DentalWave.controller;

import com.summerproject2026.DentalWave.dto.NotificationDto;
import com.summerproject2026.DentalWave.enums.NotificationType;
import com.summerproject2026.DentalWave.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web-layer unit tests for {@link NotificationController}.
 *
 * <p>Uses {@code @WebMvcTest} to load only the controller layer.
 * {@link NotificationService} is replaced with a Mockito mock via
 * {@code @MockitoBean} so tests are completely isolated from the
 * business and persistence layers.</p>
 *
 * @author Demaris
 */
@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

    /**
     * MockMvc instance used to perform HTTP requests in tests
     * without starting a real server.
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * Mocked NotificationService — replaces the real service bean
     * so no database calls are made during tests.
     */
    @MockitoBean
    private NotificationService notificationService;

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

    /** First shared notification fixture for REQUESTS tab */
    private NotificationDto requestNotification;

    /** Second shared notification fixture for EMPLOYEES tab */
    private NotificationDto employeeNotification;

    /**
     * Sets up shared test data before each test.
     * Creates two NotificationDto objects for different tabs.
     */
    @BeforeEach
    void setUp() {
        // Set up time-off request notification for REQUESTS tab
        requestNotification = new NotificationDto();
        requestNotification.setId(1L);
        requestNotification.setRecipientId(10L);
        requestNotification.setRecipientUsername("hr");
        requestNotification.setMessage("Jane Smith submitted a time-off request.");
        requestNotification.setRead(false);
        requestNotification.setCreatedAt(LocalDateTime.now());
        requestNotification.setType(NotificationType.TIME_OFF_REQUEST);
        requestNotification.setTargetTab("REQUESTS");
        requestNotification.setReferenceId(5L);

        // Set up employee hired notification for EMPLOYEES tab
        employeeNotification = new NotificationDto();
        employeeNotification.setId(2L);
        employeeNotification.setRecipientId(10L);
        employeeNotification.setRecipientUsername("hr");
        employeeNotification.setMessage("New employee Alice Smith has been hired.");
        employeeNotification.setRead(false);
        employeeNotification.setCreatedAt(LocalDateTime.now());
        employeeNotification.setType(NotificationType.EMPLOYEE_HIRED);
        employeeNotification.setTargetTab("EMPLOYEES");
        employeeNotification.setReferenceId(3L);
    }

    /**
     * Verifies that GET /api/notifications/user/{userId} returns 200 OK
     * with all notifications for the user.
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("GET /api/notifications/user/{userId} → 200 OK with all notifications")
    void getNotificationsForUser_returns200() throws Exception {
        // Mock service to return two notifications
        when(notificationService.getNotificationsForUser(10L))
                .thenReturn(List.of(requestNotification, employeeNotification));

        // Perform GET request and verify response
        mockMvc.perform(get("/api/notifications/user/{userId}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].message", is("Jane Smith submitted a time-off request.")))
                .andExpect(jsonPath("$[1].message", is("New employee Alice Smith has been hired.")));

        // Verify service was called with correct user id
        verify(notificationService, times(1)).getNotificationsForUser(10L);
    }

    /**
     * Verifies that GET /api/notifications/user/{userId} returns 200 OK
     * with empty list when user has no notifications.
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("GET /api/notifications/user/{userId} → 200 OK with empty list")
    void getNotificationsForUser_returns200WithEmptyList() throws Exception {
        // Mock service to return empty list
        when(notificationService.getNotificationsForUser(99L)).thenReturn(List.of());

        // Perform GET request and verify empty list
        mockMvc.perform(get("/api/notifications/user/{userId}", 99L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    /**
     * Verifies that GET /api/notifications/user/{userId}/unread returns 200 OK
     * with only unread notifications for the user.
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("GET /api/notifications/user/{userId}/unread → 200 OK with unread notifications")
    void getUnreadNotifications_returns200() throws Exception {
        // Mock service to return one unread notification
        when(notificationService.getUnreadNotifications(10L))
                .thenReturn(List.of(requestNotification));

        // Perform GET request and verify unread notification returned
        mockMvc.perform(get("/api/notifications/user/{userId}/unread", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].read", is(false)))
                .andExpect(jsonPath("$[0].targetTab", is("REQUESTS")));

        // Verify service was called with correct user id
        verify(notificationService, times(1)).getUnreadNotifications(10L);
    }

    /**
     * Verifies that GET /api/notifications/user/{userId}/unread/count returns 200 OK
     * with the correct count of unread notifications.
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("GET /api/notifications/user/{userId}/unread/count → 200 OK with count")
    void getUnreadCount_returns200() throws Exception {
        // Mock service to return count of 2
        when(notificationService.getUnreadCount(10L)).thenReturn(2L);

        // Perform GET request and verify count
        mockMvc.perform(get("/api/notifications/user/{userId}/unread/count", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(2)));

        // Verify service was called with correct user id
        verify(notificationService, times(1)).getUnreadCount(10L);
    }

    /**
     * Verifies that GET /api/notifications/user/{userId}/unread/count returns 0
     * when user has no unread notifications.
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("GET /api/notifications/user/{userId}/unread/count → 200 OK with count 0")
    void getUnreadCount_returns0_whenNoUnread() throws Exception {
        // Mock service to return count of 0
        when(notificationService.getUnreadCount(10L)).thenReturn(0L);

        // Perform GET request and verify count is 0
        mockMvc.perform(get("/api/notifications/user/{userId}/unread/count", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(0)));
    }

    /**
     * Verifies that GET /api/notifications/user/{userId}/tab/{targetTab} returns 200 OK
     * with notifications for the REQUESTS tab.
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("GET /api/notifications/user/{userId}/tab/REQUESTS → 200 OK with REQUESTS notifications")
    void getNotificationsByTab_returnsRequestsTab() throws Exception {
        // Mock service to return one REQUESTS tab notification
        when(notificationService.getUnreadNotificationsByTab(10L, "REQUESTS"))
                .thenReturn(List.of(requestNotification));

        // Perform GET request for REQUESTS tab
        mockMvc.perform(get("/api/notifications/user/{userId}/tab/{targetTab}",
                        10L, "REQUESTS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].targetTab", is("REQUESTS")))
                .andExpect(jsonPath("$[0].type", is("TIME_OFF_REQUEST")));

        // Verify service was called with correct params
        verify(notificationService, times(1))
                .getUnreadNotificationsByTab(10L, "REQUESTS");
    }

    /**
     * Verifies that GET /api/notifications/user/{userId}/tab/{targetTab} returns 200 OK
     * with notifications for the EMPLOYEES tab.
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("GET /api/notifications/user/{userId}/tab/EMPLOYEES → 200 OK with EMPLOYEES notifications")
    void getNotificationsByTab_returnsEmployeesTab() throws Exception {
        // Mock service to return one EMPLOYEES tab notification
        when(notificationService.getUnreadNotificationsByTab(10L, "EMPLOYEES"))
                .thenReturn(List.of(employeeNotification));

        // Perform GET request for EMPLOYEES tab
        mockMvc.perform(get("/api/notifications/user/{userId}/tab/{targetTab}",
                        10L, "EMPLOYEES"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].targetTab", is("EMPLOYEES")))
                .andExpect(jsonPath("$[0].type", is("EMPLOYEE_HIRED")));

        // Verify service was called with correct params
        verify(notificationService, times(1))
                .getUnreadNotificationsByTab(10L, "EMPLOYEES");
    }

    /**
     * Verifies that PATCH /api/notifications/{id}/read returns 200 OK
     * with a confirmation message when notification is marked as read.
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("PATCH /api/notifications/{id}/read → 200 OK with confirmation message")
    void markAsRead_returns200() throws Exception {
        // Mock service to do nothing on mark as read
        doNothing().when(notificationService).markAsRead(1L);

        // Perform PATCH request and verify confirmation message
        mockMvc.perform(patch("/api/notifications/{id}/read", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string("Notification 1 marked as read."));

        // Verify service was called with correct notification id
        verify(notificationService, times(1)).markAsRead(1L);
    }

    /**
     * Verifies that PATCH /api/notifications/user/{userId}/read-all returns 200 OK
     * with a confirmation message when all notifications are marked as read.
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("PATCH /api/notifications/user/{userId}/read-all → 200 OK with confirmation message")
    void markAllAsRead_returns200() throws Exception {
        // Mock service to do nothing on mark all as read
        doNothing().when(notificationService).markAllAsRead(10L);

        // Perform PATCH request and verify confirmation message
        mockMvc.perform(patch("/api/notifications/user/{userId}/read-all", 10L))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        "All notifications marked as read for user 10."));

        // Verify service was called with correct user id
        verify(notificationService, times(1)).markAllAsRead(10L);
    }
}