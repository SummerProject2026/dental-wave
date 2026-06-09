package com.summerproject2026.DentalWave.controller;

import com.summerproject2026.DentalWave.dto.ScheduleDto;
import com.summerproject2026.DentalWave.service.ScheduleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for ScheduleController.
 *
 * ScheduleService is mocked so no database or Spring context overhead.
 * Date-path variables are tested using ISO format (yyyy-MM-dd).
 */
@WebMvcTest(ScheduleController.class)
@DisplayName("ScheduleController")
class ScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ScheduleService scheduleService;

    @Autowired
    private ObjectMapper objectMapper;

    // -------------------------------------------------------------------------
    // Shared fixtures
    // -------------------------------------------------------------------------

    private ScheduleDto scheduleDto;
    private static final LocalDate TEST_DATE = LocalDate.of(2025, 6, 15);

    @BeforeEach
    void setUp() {
        scheduleDto = new ScheduleDto();
        scheduleDto.setId(1L);
        scheduleDto.setDate(TEST_DATE);
        scheduleDto.setPublished(false);
    }

    // =========================================================================
    // POST /api/schedules
    // =========================================================================

    @Nested
    @DisplayName("POST /api/schedules")
    class CreateSchedule {

        @Test
        @DisplayName("returns 201 with the persisted ScheduleDto")
        void createSchedule_returns201() throws Exception {
            when(scheduleService.createSchedule(any(ScheduleDto.class))).thenReturn(scheduleDto);

            mockMvc.perform(post("/api/schedules")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(scheduleDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L));

            verify(scheduleService, times(1)).createSchedule(any(ScheduleDto.class));
        }
    }

    // =========================================================================
    // GET /api/schedules/{id}
    // =========================================================================

    @Nested
    @DisplayName("GET /api/schedules/{id}")
    class GetScheduleById {

        @Test
        @DisplayName("returns 200 and the ScheduleDto for a valid id")
        void getScheduleById_returns200() throws Exception {
            when(scheduleService.getScheduleById(1L)).thenReturn(scheduleDto);

            mockMvc.perform(get("/api/schedules/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L));
        }

        @Test
        @DisplayName("propagates ResourceNotFoundException (results in 404)")
        void getScheduleById_notFound() throws Exception {
            when(scheduleService.getScheduleById(99L))
                    .thenThrow(new com.dentalwave.exception.ResourceNotFoundException("Schedule not found"));

            mockMvc.perform(get("/api/schedules/99"))
                    .andExpect(status().isNotFound());
        }
    }

    // =========================================================================
    // GET /api/schedules
    // =========================================================================

    @Nested
    @DisplayName("GET /api/schedules")
    class GetAllSchedules {

        @Test
        @DisplayName("returns 200 and the full schedule list")
        void getAllSchedules_returns200() throws Exception {
            ScheduleDto second = new ScheduleDto();
            second.setId(2L);
            second.setDate(LocalDate.of(2025, 6, 16));

            when(scheduleService.getAllSchedules()).thenReturn(List.of(scheduleDto, second));

            mockMvc.perform(get("/api/schedules"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(1L))
                    .andExpect(jsonPath("$[1].id").value(2L));
        }

        @Test
        @DisplayName("returns 200 and empty list when no schedules exist")
        void getAllSchedules_emptyList() throws Exception {
            when(scheduleService.getAllSchedules()).thenReturn(List.of());

            mockMvc.perform(get("/api/schedules"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    // =========================================================================
    // PUT /api/schedules/{id}
    // =========================================================================

    @Nested
    @DisplayName("PUT /api/schedules/{id}")
    class UpdateSchedule {

        @Test
        @DisplayName("returns 200 with the updated ScheduleDto")
        void updateSchedule_returns200() throws Exception {
            scheduleDto.setDate(LocalDate.of(2025, 7, 1));
            when(scheduleService.updateSchedule(eq(1L), any(ScheduleDto.class))).thenReturn(scheduleDto);

            mockMvc.perform(put("/api/schedules/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(scheduleDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L));

            verify(scheduleService).updateSchedule(eq(1L), any(ScheduleDto.class));
        }
    }

    // =========================================================================
    // DELETE /api/schedules/{id}
    // =========================================================================

    @Nested
    @DisplayName("DELETE /api/schedules/{id}")
    class DeleteSchedule {

        @Test
        @DisplayName("returns 200 with a confirmation message")
        void deleteSchedule_returns200() throws Exception {
            doNothing().when(scheduleService).deleteSchedule(1L);

            mockMvc.perform(delete("/api/schedules/1"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Schedule with id 1 deleted successfully."));

            verify(scheduleService).deleteSchedule(1L);
        }
    }

    // =========================================================================
    // GET /api/schedules/date/{date}
    // =========================================================================

    @Nested
    @DisplayName("GET /api/schedules/date/{date}")
    class GetSchedulesByDate {

        @Test
        @DisplayName("returns 200 with schedules on the given date (ISO format)")
        void getSchedulesByDate_returns200() throws Exception {
            when(scheduleService.getSchedulesByDate(TEST_DATE)).thenReturn(List.of(scheduleDto));

            mockMvc.perform(get("/api/schedules/date/2025-06-15"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1L));
        }

        @Test
        @DisplayName("returns 200 and empty list when no schedules match date")
        void getSchedulesByDate_noMatch_emptyList() throws Exception {
            when(scheduleService.getSchedulesByDate(LocalDate.of(2000, 1, 1)))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/schedules/date/2000-01-01"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("returns 400 for a non-ISO date format")
        void getSchedulesByDate_invalidFormat_returns400() throws Exception {
            // Spring's @DateTimeFormat(ISO.DATE) will fail to parse "15-06-2025"
            mockMvc.perform(get("/api/schedules/date/15-06-2025"))
                    .andExpect(status().isBadRequest());
        }
    }

    // =========================================================================
    // GET /api/schedules/calendar/{calendarId}
    // =========================================================================

    @Nested
    @DisplayName("GET /api/schedules/calendar/{calendarId}")
    class GetSchedulesByCalendar {

        @Test
        @DisplayName("returns 200 with schedules belonging to the given calendar")
        void getSchedulesByCalendar_returns200() throws Exception {
            when(scheduleService.getSchedulesByCalendar(5L)).thenReturn(List.of(scheduleDto));

            mockMvc.perform(get("/api/schedules/calendar/5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        @DisplayName("returns 200 and empty list when calendar has no schedules")
        void getSchedulesByCalendar_noSchedules() throws Exception {
            when(scheduleService.getSchedulesByCalendar(99L)).thenReturn(List.of());

            mockMvc.perform(get("/api/schedules/calendar/99"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    // =========================================================================
    // POST /api/schedules/{scheduleId}/teams/{userId}/employees/{employeeId}
    // =========================================================================

    @Nested
    @DisplayName("POST /api/schedules/{scheduleId}/teams/{userId}/employees/{employeeId}")
    class AssignEmployeeToTeam {

        @Test
        @DisplayName("returns 200 with the updated ScheduleDto after assignment")
        void assignEmployeeToTeam_returns200() throws Exception {
            when(scheduleService.assignEmployeeToTeam(1L, 2L, 3L)).thenReturn(scheduleDto);

            mockMvc.perform(post("/api/schedules/1/teams/2/employees/3"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L));

            verify(scheduleService).assignEmployeeToTeam(1L, 2L, 3L);
        }

        @Test
        @DisplayName("propagates ResourceNotFoundException when any entity is missing (results in 404)")
        void assignEmployeeToTeam_entityNotFound() throws Exception {
            when(scheduleService.assignEmployeeToTeam(1L, 2L, 99L))
                    .thenThrow(new com.dentalwave.exception.ResourceNotFoundException("Employee not found"));

            mockMvc.perform(post("/api/schedules/1/teams/2/employees/99"))
                    .andExpect(status().isNotFound());
        }
    }

    // =========================================================================
    // DELETE /api/schedules/{scheduleId}/teams/{userId}/employees/{employeeId}
    // =========================================================================

    @Nested
    @DisplayName("DELETE /api/schedules/{scheduleId}/teams/{userId}/employees/{employeeId}")
    class RemoveEmployeeFromTeam {

        @Test
        @DisplayName("returns 200 with the updated ScheduleDto after removal")
        void removeEmployeeFromTeam_returns200() throws Exception {
            when(scheduleService.removeEmployeeFromTeam(1L, 2L, 3L)).thenReturn(scheduleDto);

            mockMvc.perform(delete("/api/schedules/1/teams/2/employees/3"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L));

            verify(scheduleService).removeEmployeeFromTeam(1L, 2L, 3L);
        }

        @Test
        @DisplayName("propagates ResourceNotFoundException when any entity is missing (results in 404)")
        void removeEmployeeFromTeam_entityNotFound() throws Exception {
            when(scheduleService.removeEmployeeFromTeam(1L, 2L, 99L))
                    .thenThrow(new com.dentalwave.exception.ResourceNotFoundException("Employee not found"));

            mockMvc.perform(delete("/api/schedules/1/teams/2/employees/99"))
                    .andExpect(status().isNotFound());
        }
    }

    // =========================================================================
    // PATCH /api/schedules/{id}/publish
    // =========================================================================

    @Nested
    @DisplayName("PATCH /api/schedules/{id}/publish")
    class PublishSchedule {

        @Test
        @DisplayName("returns 200 with published=true on the ScheduleDto")
        void publishSchedule_returns200() throws Exception {
            scheduleDto.setPublished(true);
            when(scheduleService.publishSchedule(1L)).thenReturn(scheduleDto);

            mockMvc.perform(patch("/api/schedules/1/publish"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.published").value(true));

            verify(scheduleService).publishSchedule(1L);
        }

        @Test
        @DisplayName("propagates ResourceNotFoundException (results in 404)")
        void publishSchedule_notFound() throws Exception {
            when(scheduleService.publishSchedule(99L))
                    .thenThrow(new com.dentalwave.exception.ResourceNotFoundException("Schedule not found"));

            mockMvc.perform(patch("/api/schedules/99/publish"))
                    .andExpect(status().isNotFound());
        }
    }
}