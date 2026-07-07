package com.summerproject2026.DentalWave.controller;

import com.summerproject2026.DentalWave.config.SpringSecurityConfig;
import com.summerproject2026.DentalWave.dto.EmployeeDto;
import com.summerproject2026.DentalWave.entity.Notification;
import com.summerproject2026.DentalWave.entity.User;
import com.summerproject2026.DentalWave.repository.EmployeeRepository;
import com.summerproject2026.DentalWave.repository.NotificationRepository;
import com.summerproject2026.DentalWave.repository.UserRepository;
import com.summerproject2026.DentalWave.security.CustomUserDetailsService;
import com.summerproject2026.DentalWave.security.JwtAuthenticationEntryPoint;
import com.summerproject2026.DentalWave.security.JwtTokenProvider;
import com.summerproject2026.DentalWave.service.AuthService;
import com.summerproject2026.DentalWave.service.AvailabilityService;
import com.summerproject2026.DentalWave.service.EmployeeService;
import com.summerproject2026.DentalWave.service.NotificationService;
import com.summerproject2026.DentalWave.service.ScheduleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        AuthController.class,
        AvailabilityController.class,
        NotificationController.class,
        EmployeeController.class,
        ScheduleController.class
})
@AutoConfigureMockMvc
@Import({SpringSecurityConfig.class, JwtAuthenticationEntryPoint.class})
class SecurityOwnershipControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private NotificationRepository notificationRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private EmployeeService employeeService;

    @MockitoBean
    private EmployeeRepository employeeRepository;

    @MockitoBean
    private ScheduleService scheduleService;

    @MockitoBean
    private AvailabilityService availabilityService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void unauthenticatedNotificationRequestReturns401() throws Exception {
        mockMvc.perform(get("/api/notifications/user/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "assistant", authorities = "ROLE_ASSISTANT")
    void assistantCannotAccessAnotherUsersNotifications() throws Exception {
        User otherUser = new User();
        otherUser.setId(99L);
        otherUser.setUsername("other");
        otherUser.setEmail("other@example.com");

        when(userRepository.findById(99L)).thenReturn(Optional.of(otherUser));

        mockMvc.perform(get("/api/notifications/user/99"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "assistant", authorities = "ROLE_ASSISTANT")
    void assistantCannotMarkAnotherUsersNotificationRead() throws Exception {
        User otherUser = new User();
        otherUser.setId(99L);
        otherUser.setUsername("other");

        Notification notification = new Notification();
        notification.setId(5L);
        notification.setRecipient(otherUser);

        when(notificationRepository.findById(5L)).thenReturn(Optional.of(notification));

        mockMvc.perform(patch("/api/notifications/5/read"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "assistant", authorities = "ROLE_ASSISTANT")
    void assistantCannotAccessAnotherEmployeeRecord() throws Exception {
        EmployeeDto otherEmployee = new EmployeeDto();
        otherEmployee.setId(22L);
        otherEmployee.setUsername("other");
        otherEmployee.setEmail("other@example.com");

        when(employeeService.getEmployeeById(22L)).thenReturn(otherEmployee);

        mockMvc.perform(get("/api/employees/22"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "assistant", authorities = "ROLE_ASSISTANT")
    void assistantCannotFetchAnotherEmployeesSchedule() throws Exception {
        EmployeeDto otherEmployee = new EmployeeDto();
        otherEmployee.setId(22L);
        otherEmployee.setUsername("other");
        otherEmployee.setEmail("other@example.com");

        when(employeeService.getEmployeeById(22L)).thenReturn(otherEmployee);

        mockMvc.perform(get("/api/schedules/employee/22"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "assistant", authorities = "ROLE_ASSISTANT")
    void unauthorizedRoleCannotUseEmployeeNameScheduleLookup() throws Exception {
        mockMvc.perform(get("/api/schedules/employee/name/JaneDoe"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "assistant", authorities = "ROLE_ASSISTANT")
    void assistantCanAccessOwnAvailability() throws Exception {
        EmployeeDto ownEmployee = new EmployeeDto();
        ownEmployee.setId(11L);
        ownEmployee.setUsername("assistant");
        ownEmployee.setEmail("assistant@example.com");

        when(employeeService.getEmployeeById(11L)).thenReturn(ownEmployee);
        when(availabilityService.getAvailabilityByEmployee(11L)).thenReturn(List.of());

        mockMvc.perform(get("/api/availability/employee/11"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "assistant", authorities = "ROLE_ASSISTANT")
    void assistantCannotAccessAnotherEmployeesAvailability() throws Exception {
        EmployeeDto otherEmployee = new EmployeeDto();
        otherEmployee.setId(22L);
        otherEmployee.setUsername("other");
        otherEmployee.setEmail("other@example.com");

        when(employeeService.getEmployeeById(22L)).thenReturn(otherEmployee);

        mockMvc.perform(get("/api/availability/employee/22"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "hr", authorities = "ROLE_HR")
    void hrCanAccessEmployeeAvailability() throws Exception {
        when(availabilityService.getAvailabilityByEmployee(22L)).thenReturn(List.of());

        mockMvc.perform(get("/api/availability/employee/22"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "manager", authorities = "ROLE_MANAGER")
    void managerCanAccessEmployeeAvailability() throws Exception {
        when(availabilityService.getAvailabilityByEmployee(22L)).thenReturn(List.of());

        mockMvc.perform(get("/api/availability/employee/22"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ROLE_ADMIN")
    void adminCanAccessEmployeeAvailability() throws Exception {
        when(availabilityService.getAvailabilityByEmployee(22L)).thenReturn(List.of());

        mockMvc.perform(get("/api/availability/employee/22"))
                .andExpect(status().isOk());
    }

    @Test
    void publicRegistrationIsDisabled() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isForbidden());
    }
}
