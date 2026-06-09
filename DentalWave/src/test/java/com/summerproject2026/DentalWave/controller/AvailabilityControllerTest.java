package com.summerproject2026.DentalWave.controller;
import com.summerproject2026.DentalWave.dto.AvailabilityDto;
import com.summerproject2026.DentalWave.service.AvailabilityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AvailabilityController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AvailabilityController")
class AvailabilityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AvailabilityService availabilityService;

    @MockBean
    private com.summerproject2026.DentalWave.security.JwtTokenProvider jwtTokenProvider;

    @MockBean
    private com.summerproject2026.DentalWave.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    private AvailabilityDto availabilityDto;

    @BeforeEach
    void setUp() {
        availabilityDto = new AvailabilityDto();
        availabilityDto.setId(1L);
        availabilityDto.setEmployeeId(5L);
        availabilityDto.setDayOfWeek(DayOfWeek.MONDAY);
        availabilityDto.setStartTime(LocalTime.of(8, 0));
        availabilityDto.setEndTime(LocalTime.of(17, 0));
    }

    @Nested
    @DisplayName("POST /api/availability")
    class CreateAvailability {

        @Test
        @DisplayName("returns 201 with the persisted AvailabilityDto")
        void createAvailability_returns201() throws Exception {
            when(availabilityService.createAvailability(any(AvailabilityDto.class)))
                    .thenReturn(availabilityDto);

            mockMvc.perform(post("/api/availability")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(availabilityDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.employeeId").value(5L))
                    .andExpect(jsonPath("$.dayOfWeek").value("MONDAY"));

            verify(availabilityService, times(1)).createAvailability(any(AvailabilityDto.class));
        }

        @Test
        @DisplayName("delegates to service exactly once")
        void createAvailability_delegatesToServiceOnce() throws Exception {
            when(availabilityService.createAvailability(any(AvailabilityDto.class)))
                    .thenReturn(availabilityDto);

            mockMvc.perform(post("/api/availability")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(availabilityDto)))
                    .andExpect(status().isCreated());

            verify(availabilityService, times(1)).createAvailability(any(AvailabilityDto.class));
        }
    }

    @Nested
    @DisplayName("GET /api/availability/{id}")
    class GetAvailabilityById {

        @Test
        @DisplayName("returns 501 NOT_IMPLEMENTED")
        void getAvailabilityById_returns501() throws Exception {
            mockMvc.perform(get("/api/availability/1"))
                    .andExpect(status().isNotImplemented());
        }
    }

    @Nested
    @DisplayName("GET /api/availability/employee/{employeeId}")
    class GetAvailabilityByEmployee {

        @Test
        @DisplayName("returns 200 with the employee's availability records")
        void getAvailabilityByEmployee_returns200() throws Exception {
            AvailabilityDto second = new AvailabilityDto();
            second.setId(2L);
            second.setEmployeeId(5L);
            second.setDayOfWeek(DayOfWeek.WEDNESDAY);

            when(availabilityService.getAvailabilityByEmployee(5L))
                    .thenReturn(List.of(availabilityDto, second));

            mockMvc.perform(get("/api/availability/employee/5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].dayOfWeek").value("MONDAY"))
                    .andExpect(jsonPath("$[1].dayOfWeek").value("WEDNESDAY"));
        }

        @Test
        @DisplayName("returns 200 and empty list when employee has no availability records")
        void getAvailabilityByEmployee_emptyList() throws Exception {
            when(availabilityService.getAvailabilityByEmployee(99L)).thenReturn(List.of());

            mockMvc.perform(get("/api/availability/employee/99"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("propagates ResourceNotFoundException when employee doesn't exist")
        void getAvailabilityByEmployee_employeeNotFound() throws Exception {
            when(availabilityService.getAvailabilityByEmployee(999L))
                    .thenThrow(new com.summerproject2026.DentalWave.exception.ResourceNotFoundException("Employee not found"));

            mockMvc.perform(get("/api/availability/employee/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/availability/{id}")
    class UpdateAvailability {

        @Test
        @DisplayName("returns 200 with the updated AvailabilityDto")
        void updateAvailability_returns200() throws Exception {
            availabilityDto.setEndTime(LocalTime.of(18, 0));
            when(availabilityService.updateAvailability(eq(1L), any(AvailabilityDto.class)))
                    .thenReturn(availabilityDto);

            mockMvc.perform(put("/api/availability/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(availabilityDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.endTime").value("18:00:00"));
            verify(availabilityService).updateAvailability(eq(1L), any(AvailabilityDto.class));
        }

        @Test
        @DisplayName("propagates ResourceNotFoundException when record doesn't exist")
        void updateAvailability_notFound() throws Exception {
            when(availabilityService.updateAvailability(eq(99L), any(AvailabilityDto.class)))
                    .thenThrow(new com.summerproject2026.DentalWave.exception.ResourceNotFoundException("Availability not found"));

            mockMvc.perform(put("/api/availability/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(availabilityDto)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/availability/{id}")
    class DeleteAvailability {

        @Test
        @DisplayName("returns 200 with a confirmation message")
        void deleteAvailability_returns200() throws Exception {
            doNothing().when(availabilityService).deleteAvailability(1L);

            mockMvc.perform(delete("/api/availability/1"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Availability record with id 1 deleted successfully."));

            verify(availabilityService).deleteAvailability(1L);
        }

        @Test
        @DisplayName("propagates ResourceNotFoundException when record doesn't exist")
        void deleteAvailability_notFound() throws Exception {
            doThrow(new com.summerproject2026.DentalWave.exception.ResourceNotFoundException("Availability not found"))
                    .when(availabilityService).deleteAvailability(99L);

            mockMvc.perform(delete("/api/availability/99"))
                    .andExpect(status().isNotFound());
        }
    }
}