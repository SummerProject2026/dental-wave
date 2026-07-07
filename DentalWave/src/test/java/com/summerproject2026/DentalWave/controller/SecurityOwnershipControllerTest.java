package com.summerproject2026.DentalWave.controller;

import com.summerproject2026.DentalWave.dto.EmployeeDto;
import com.summerproject2026.DentalWave.entity.Notification;
import com.summerproject2026.DentalWave.entity.User;
import com.summerproject2026.DentalWave.repository.EmployeeRepository;
import com.summerproject2026.DentalWave.repository.NotificationRepository;
import com.summerproject2026.DentalWave.repository.UserRepository;
import com.summerproject2026.DentalWave.security.JwtAuthenticationFilter;
import com.summerproject2026.DentalWave.security.JwtTokenProvider;
import com.summerproject2026.DentalWave.service.EmployeeService;
import com.summerproject2026.DentalWave.service.NotificationService;
import com.summerproject2026.DentalWave.service.ScheduleService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        NotificationController.class,
        EmployeeController.class,
        ScheduleController.class
})
@AutoConfigureMockMvc
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
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void allowMockJwtFilterToContinueChain() throws Exception {
        doAnswer(invocation -> {
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

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
        mockMvc.perform(get("/api/schedules/employee/name/Jane%20Doe"))
                .andExpect(status().isForbidden());
    }
}
