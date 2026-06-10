package com.summerproject2026.DentalWave.controller;

import com.summerproject2026.DentalWave.dto.CalendarDto;
import com.summerproject2026.DentalWave.dto.ScheduleDto;
import com.summerproject2026.DentalWave.service.CalendarService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
/**
 * Unit tests for CalendarController.
 *
 * Uses @WebMvcTest to load only the web layer (no DB / security context).
 * CalendarService is mocked via @MockBean so tests remain fast and isolated.
 *
 * If your project has a SecurityConfig, add:
 *   @WebMvcTest(value = CalendarController.class, excludeAutoConfiguration =
 *       {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
 * or provide a test-only security config that permits all requests.
 */
@WebMvcTest(CalendarController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("CalendarController")
class CalendarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CalendarService calendarService;

    @MockitoBean
    private com.summerproject2026.DentalWave.security.JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private com.summerproject2026.DentalWave.security.JwtAuthenticationFilter jwtAuthenticationFilter;


    @Autowired
    private ObjectMapper objectMapper;

    // -------------------------------------------------------------------------
    // Shared fixtures
    // -------------------------------------------------------------------------

    private CalendarDto calendarDto;
    private ScheduleDto scheduleDto;

    @BeforeEach
    void setUp() {
        calendarDto = new CalendarDto();
        calendarDto.setId(1L);
        calendarDto.setMonth("June 2025");
        calendarDto.setPublished(false);

        scheduleDto = new ScheduleDto();
        scheduleDto.setId(10L);
    }

    // =========================================================================
    // POST /api/calendars
    // =========================================================================

    @Nested
    @DisplayName("POST /api/calendars")
    class CreateCalendar {

        @Test
        @DisplayName("returns 201 and the created CalendarDto")
        void createCalendar_returns201() throws Exception {
            when(calendarService.createCalendar(any(CalendarDto.class))).thenReturn(calendarDto);

            mockMvc.perform(post("/api/calendars")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(calendarDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.month").value("June 2025"));

            verify(calendarService).createCalendar(any(CalendarDto.class));
        }

        @Test
        @DisplayName("delegates to service exactly once")
        void createCalendar_delegatesToService() throws Exception {
            when(calendarService.createCalendar(any(CalendarDto.class))).thenReturn(calendarDto);

            mockMvc.perform(post("/api/calendars")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(calendarDto)))
                    .andExpect(status().isCreated());

            verify(calendarService, times(1)).createCalendar(any(CalendarDto.class));
        }
    }

    // =========================================================================
    // GET /api/calendars/{id}
    // =========================================================================

    @Nested
    @DisplayName("GET /api/calendars/{id}")
    class GetCalendarById {

        @Test
        @DisplayName("returns 200 and the CalendarDto for a valid id")
        void getCalendarById_returns200() throws Exception {
            when(calendarService.getCalendarById(1L)).thenReturn(calendarDto);

            mockMvc.perform(get("/api/calendars/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.month").value("June 2025"));
        }

        @Test
        @DisplayName("propagates ResourceNotFoundException from service (results in 404)")
        void getCalendarById_notFound_propagatesException() throws Exception {
            when(calendarService.getCalendarById(99L))
                    .thenThrow(new com.summerproject2026.DentalWave.exception.ResourceNotFoundException("Calendar not found"));

            mockMvc.perform(get("/api/calendars/99"))
                    .andExpect(status().isNotFound());
        }
    }

    // =========================================================================
    // GET /api/calendars
    // =========================================================================

    @Nested
    @DisplayName("GET /api/calendars")
    class GetAllCalendars {

        @Test
        @DisplayName("returns 200 and a list of all calendars")
        void getAllCalendars_returns200WithList() throws Exception {
            CalendarDto second = new CalendarDto();
            second.setId(2L);
            second.setMonth("July 2025");

            when(calendarService.getAllCalendars()).thenReturn(List.of(calendarDto, second));

            mockMvc.perform(get("/api/calendars"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(1L))
                    .andExpect(jsonPath("$[1].id").value(2L));
        }

        @Test
        @DisplayName("returns 200 and an empty list when no calendars exist")
        void getAllCalendars_emptyList() throws Exception {
            when(calendarService.getAllCalendars()).thenReturn(List.of());

            mockMvc.perform(get("/api/calendars"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    // =========================================================================
    // PUT /api/calendars/{id}
    // =========================================================================

    @Nested
    @DisplayName("PUT /api/calendars/{id}")
    class UpdateCalendar {

        @Test
        @DisplayName("returns 200 with the updated CalendarDto")
        void updateCalendar_returns200() throws Exception {
            calendarDto.setMonth("August 2025");
            when(calendarService.updateCalendar(eq(1L), any(CalendarDto.class))).thenReturn(calendarDto);

            mockMvc.perform(put("/api/calendars/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(calendarDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.month").value("August 2025"));

            verify(calendarService).updateCalendar(eq(1L), any(CalendarDto.class));
        }
    }

    // =========================================================================
    // DELETE /api/calendars/{id}
    // =========================================================================

    @Nested
    @DisplayName("DELETE /api/calendars/{id}")
    class DeleteCalendar {

        @Test
        @DisplayName("returns 200 with a confirmation message")
        void deleteCalendar_returns200() throws Exception {
            doNothing().when(calendarService).deleteCalendar(1L);

            mockMvc.perform(delete("/api/calendars/1"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Calendar with id 1 deleted successfully."));

            verify(calendarService).deleteCalendar(1L);
        }
    }

    // =========================================================================
    // PATCH /api/calendars/{id}/publish
    // =========================================================================

    @Nested
    @DisplayName("PATCH /api/calendars/{id}/publish")
    class PublishCalendar {

        @Test
        @DisplayName("returns 200 with published=true")
        void publishCalendar_returns200() throws Exception {
            calendarDto.setPublished(true);
            when(calendarService.publishCalendar(1L)).thenReturn(calendarDto);

            mockMvc.perform(patch("/api/calendars/1/publish"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.published").value(true));

            verify(calendarService).publishCalendar(1L);
        }
    }

    // =========================================================================
    // PATCH /api/calendars/{id}/unpublish
    // =========================================================================

    @Nested
    @DisplayName("PATCH /api/calendars/{id}/unpublish")
    class UnpublishCalendar {

        @Test
        @DisplayName("returns 200 with published=false")
        void unpublishCalendar_returns200() throws Exception {
            calendarDto.setPublished(false);
            when(calendarService.unpublishCalendar(1L)).thenReturn(calendarDto);

            mockMvc.perform(patch("/api/calendars/1/unpublish"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.published").value(false));

            verify(calendarService).unpublishCalendar(1L);
        }
    }

    // =========================================================================
    // GET /api/calendars/month/{month}
    // =========================================================================

    @Nested
    @DisplayName("GET /api/calendars/month/{month}")
    class GetCalendarsByMonth {

        @Test
        @DisplayName("returns 200 and matching calendars for a given month")
        void getCalendarsByMonth_returns200() throws Exception {
            when(calendarService.getCalendarsByMonth("June 2025")).thenReturn(List.of(calendarDto));

            mockMvc.perform(get("/api/calendars/month/June 2025"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].month").value("June 2025"));
        }

        @Test
        @DisplayName("returns 200 and an empty list for a month with no calendars")
        void getCalendarsByMonth_noMatch_emptyList() throws Exception {
            when(calendarService.getCalendarsByMonth("January 2000")).thenReturn(List.of());

            mockMvc.perform(get("/api/calendars/month/January 2000"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    // =========================================================================
    // GET /api/calendars/published
    // =========================================================================

    @Nested
    @DisplayName("GET /api/calendars/published")
    class GetPublishedCalendars {

        @Test
        @DisplayName("returns 200 with only published calendars")
        void getPublishedCalendars_returns200() throws Exception {
            calendarDto.setPublished(true);
            when(calendarService.getPublishedCalendars()).thenReturn(List.of(calendarDto));

            mockMvc.perform(get("/api/calendars/published"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].published").value(true));
        }
    }

    // =========================================================================
    // POST /api/calendars/{calendarId}/schedules
    // =========================================================================

    @Nested
    @DisplayName("POST /api/calendars/{calendarId}/schedules")
    class AddSchedule {

        @Test
        @DisplayName("returns 201 with the new ScheduleDto")
        void addSchedule_returns201() throws Exception {
            when(calendarService.addSchedule(eq(1L), any(ScheduleDto.class))).thenReturn(scheduleDto);

            mockMvc.perform(post("/api/calendars/1/schedules")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(scheduleDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(10L));

            verify(calendarService).addSchedule(eq(1L), any(ScheduleDto.class));
        }
    }

    // =========================================================================
    // DELETE /api/calendars/{calendarId}/schedules/{scheduleId}
    // =========================================================================

    @Nested
    @DisplayName("DELETE /api/calendars/{calendarId}/schedules/{scheduleId}")
    class RemoveSchedule {

        @Test
        @DisplayName("returns 200 with a confirmation message")
        void removeSchedule_returns200() throws Exception {
            doNothing().when(calendarService).removeSchedule(1L, 10L);

            mockMvc.perform(delete("/api/calendars/1/schedules/10"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Schedule 10 removed from calendar 1."));

            verify(calendarService).removeSchedule(1L, 10L);
        }
    }
}