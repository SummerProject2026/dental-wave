package com.dentalwave.service.impl;

import com.dentalwave.dto.CalendarDto;
import com.dentalwave.dto.ScheduleDto;
import com.dentalwave.exception.ResourceNotFoundException;
import com.dentalwave.mapper.CalendarMapper;
import com.dentalwave.mapper.ScheduleMapper;
import com.dentalwave.model.Calendar;
import com.dentalwave.model.Schedule;
import com.dentalwave.model.User;
import com.dentalwave.repository.CalendarRepository;
import com.dentalwave.repository.ScheduleRepository;
import com.dentalwave.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CalendarServiceImpl.
 *
 * Strategy: every repository and mapper is mocked so tests are fast and deterministic.
 * Nested classes group tests by operation for readability.
 */
@ExtendWith(MockitoExtension.class)
class CalendarServiceImplTest {

    @Mock private CalendarRepository calendarRepository;
    @Mock private ScheduleRepository scheduleRepository;
    @Mock private UserRepository     userRepository;
    @Mock private CalendarMapper     calendarMapper;
    @Mock private ScheduleMapper     scheduleMapper;

    @InjectMocks
    private CalendarServiceImpl calendarService;

    // ── Shared fixtures ──────────────────────────────────────────────────────
    private User        creator;
    private Calendar    calendar;
    private CalendarDto calendarDto;

    @BeforeEach
    void setUp() {
        creator = new User();
        creator.setId(1L);
        creator.setFirstName("Admin");
        creator.setLastName("User");

        calendar = new Calendar();
        calendar.setId(1L);
        calendar.setMonth("June 2025");
        calendar.setStartCalendarDate(LocalDate.of(2025, 6, 1));
        calendar.setEndCalendarDate(LocalDate.of(2025, 6, 30));
        calendar.setPublished(false);
        calendar.setCreatedBy(creator);

        calendarDto = new CalendarDto();
        calendarDto.setId(1L);
        calendarDto.setMonth("June 2025");
        calendarDto.setStartCalendarDate(LocalDate.of(2025, 6, 1));
        calendarDto.setEndCalendarDate(LocalDate.of(2025, 6, 30));
        calendarDto.setPublished(false);
        calendarDto.setCreatedById(1L);
        calendarDto.setCreatedByName("Admin User");
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CREATE
    // ══════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("createCalendar")
    class CreateCalendar {

        @Test
        @DisplayName("Valid DTO with existing creator – saves and returns DTO")
        void createCalendar_validDto_persistsAndReturnsDto() {
            when(calendarMapper.mapToCalendar(calendarDto)).thenReturn(calendar);
            when(userRepository.findById(1L)).thenReturn(Optional.of(creator));
            when(calendarRepository.save(calendar)).thenReturn(calendar);
            when(calendarMapper.mapToCalendarDto(calendar)).thenReturn(calendarDto);

            CalendarDto result = calendarService.createCalendar(calendarDto);

            assertThat(result).isNotNull();
            assertThat(result.getMonth()).isEqualTo("June 2025");
            // The managed User must be set on the entity before saving
            verify(userRepository).findById(1L);
            verify(calendarRepository).save(calendar);
        }

        @Test
        @DisplayName("Creator user not found – throws ResourceNotFoundException, nothing saved")
        void createCalendar_creatorNotFound_throwsResourceNotFoundException() {
            when(calendarMapper.mapToCalendar(calendarDto)).thenReturn(calendar);
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> calendarService.createCalendar(calendarDto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found with id: 1");

            verify(calendarRepository, never()).save(any());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // READ
    // ══════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("getCalendarById")
    class GetCalendarById {

        @Test
        @DisplayName("Existing ID – returns mapped DTO")
        void getCalendarById_exists_returnsDto() {
            when(calendarRepository.findById(1L)).thenReturn(Optional.of(calendar));
            when(calendarMapper.mapToCalendarDto(calendar)).thenReturn(calendarDto);

            CalendarDto result = calendarService.getCalendarById(1L);

            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Non-existent ID – throws ResourceNotFoundException")
        void getCalendarById_notFound_throwsException() {
            when(calendarRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> calendarService.getCalendarById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Calendar not found with id: 99");
        }
    }

    @Nested
    @DisplayName("getAllCalendars")
    class GetAllCalendars {

        @Test
        @DisplayName("Two calendars in DB – returns list of size 2")
        void getAllCalendars_twoExist_returnsListSizeTwo() {
            when(calendarRepository.findAll()).thenReturn(List.of(calendar, calendar));
            when(calendarMapper.mapToCalendarDto(calendar)).thenReturn(calendarDto);

            assertThat(calendarService.getAllCalendars()).hasSize(2);
        }

        @Test
        @DisplayName("No calendars in DB – returns empty list")
        void getAllCalendars_none_returnsEmptyList() {
            when(calendarRepository.findAll()).thenReturn(Collections.emptyList());

            assertThat(calendarService.getAllCalendars()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getCalendarsByMonth")
    class GetCalendarsByMonth {

        @Test
        @DisplayName("Month with matching calendars – returns filtered list")
        void getCalendarsByMonth_matchFound_returnsList() {
            when(calendarRepository.findByMonth("June 2025")).thenReturn(List.of(calendar));
            when(calendarMapper.mapToCalendarDto(calendar)).thenReturn(calendarDto);

            List<CalendarDto> result = calendarService.getCalendarsByMonth("June 2025");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getMonth()).isEqualTo("June 2025");
        }

        @Test
        @DisplayName("Month with no calendars – returns empty list")
        void getCalendarsByMonth_noMatch_returnsEmptyList() {
            when(calendarRepository.findByMonth("December 2099")).thenReturn(Collections.emptyList());

            assertThat(calendarService.getCalendarsByMonth("December 2099")).isEmpty();
        }
    }

    @Nested
    @DisplayName("getPublishedCalendars")
    class GetPublishedCalendars {

        @Test
        @DisplayName("One published calendar exists – returns it")
        void getPublishedCalendars_onePublished_returnsIt() {
            calendar.setPublished(true);
            calendarDto.setPublished(true);

            when(calendarRepository.findByPublishedTrue()).thenReturn(List.of(calendar));
            when(calendarMapper.mapToCalendarDto(calendar)).thenReturn(calendarDto);

            List<CalendarDto> result = calendarService.getPublishedCalendars();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getPublished()).isTrue();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // UPDATE
    // ══════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("updateCalendar")
    class UpdateCalendar {

        @Test
        @DisplayName("Valid update – month and dates are changed and saved")
        void updateCalendar_validData_savesUpdatedCalendar() {
            CalendarDto updateDto = new CalendarDto();
            updateDto.setMonth("July 2025");
            updateDto.setStartCalendarDate(LocalDate.of(2025, 7, 1));
            updateDto.setEndCalendarDate(LocalDate.of(2025, 7, 31));
            updateDto.setPublished(true);
            updateDto.setCreatedById(1L);

            when(calendarRepository.findById(1L)).thenReturn(Optional.of(calendar));
            when(userRepository.findById(1L)).thenReturn(Optional.of(creator));
            when(calendarRepository.save(calendar)).thenReturn(calendar);
            when(calendarMapper.mapToCalendarDto(calendar)).thenReturn(calendarDto);

            calendarService.updateCalendar(1L, updateDto);

            // Verify that scalar fields were updated before save
            assertThat(calendar.getMonth()).isEqualTo("July 2025");
            assertThat(calendar.getPublished()).isTrue();
            verify(calendarRepository).save(calendar);
        }

        @Test
        @DisplayName("Calendar not found – throws ResourceNotFoundException")
        void updateCalendar_notFound_throwsException() {
            when(calendarRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> calendarService.updateCalendar(99L, calendarDto))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // DELETE
    // ══════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("deleteCalendar")
    class DeleteCalendar {

        @Test
        @DisplayName("Existing calendar – deleted successfully")
        void deleteCalendar_exists_callsRepositoryDelete() {
            when(calendarRepository.findById(1L)).thenReturn(Optional.of(calendar));

            calendarService.deleteCalendar(1L);

            verify(calendarRepository).delete(calendar);
        }

        @Test
        @DisplayName("Non-existent calendar – throws ResourceNotFoundException")
        void deleteCalendar_notFound_throwsException() {
            when(calendarRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> calendarService.deleteCalendar(99L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(calendarRepository, never()).delete(any());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // PUBLISH / UNPUBLISH
    // ══════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("publishCalendar / unpublishCalendar")
    class PublishLifecycle {

        @Test
        @DisplayName("publishCalendar – sets published=true and saves")
        void publishCalendar_draftCalendar_setsPublishedTrue() {
            calendar.setPublished(false);
            when(calendarRepository.findById(1L)).thenReturn(Optional.of(calendar));
            when(calendarRepository.save(calendar)).thenReturn(calendar);
            when(calendarMapper.mapToCalendarDto(calendar)).thenReturn(calendarDto);

            calendarService.publishCalendar(1L);

            assertThat(calendar.getPublished()).isTrue();
            verify(calendarRepository).save(calendar);
        }

        @Test
        @DisplayName("unpublishCalendar – sets published=false and saves")
        void unpublishCalendar_publishedCalendar_setsPublishedFalse() {
            calendar.setPublished(true);
            when(calendarRepository.findById(1L)).thenReturn(Optional.of(calendar));
            when(calendarRepository.save(calendar)).thenReturn(calendar);
            when(calendarMapper.mapToCalendarDto(calendar)).thenReturn(calendarDto);

            calendarService.unpublishCalendar(1L);

            assertThat(calendar.getPublished()).isFalse();
        }

        @Test
        @DisplayName("publishCalendar – calendar not found throws ResourceNotFoundException")
        void publishCalendar_notFound_throwsException() {
            when(calendarRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> calendarService.publishCalendar(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // NESTED SCHEDULE MANAGEMENT
    // ══════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("addSchedule")
    class AddSchedule {

        @Test
        @DisplayName("Valid calendar – schedule is added via addSchedule() and calendar is saved")
        void addSchedule_validCalendar_addsScheduleAndSaves() {
            Schedule schedule = new Schedule();
            schedule.setId(10L);
            ScheduleDto scheduleDto = new ScheduleDto();
            scheduleDto.setCalendarId(1L);

            when(calendarRepository.findById(1L)).thenReturn(Optional.of(calendar));
            when(scheduleMapper.mapToSchedule(scheduleDto)).thenReturn(schedule);
            when(calendarRepository.save(calendar)).thenReturn(calendar);
            when(scheduleMapper.mapToScheduleDto(schedule)).thenReturn(scheduleDto);

            ScheduleDto result = calendarService.addSchedule(1L, scheduleDto);

            assertThat(result).isNotNull();
            // The schedule should now be in the calendar's collection
            assertThat(calendar.getSchedules()).contains(schedule);
        }

        @Test
        @DisplayName("Calendar not found – throws ResourceNotFoundException")
        void addSchedule_calendarNotFound_throwsException() {
            when(calendarRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> calendarService.addSchedule(99L, new ScheduleDto()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("removeSchedule")
    class RemoveSchedule {

        @Test
        @DisplayName("Schedule belongs to calendar – removed and calendar saved")
        void removeSchedule_belongsToCalendar_removesSuccessfully() {
            Schedule schedule = new Schedule();
            schedule.setId(10L);
            schedule.setCalendar(calendar); // ownership is correct
            calendar.addSchedule(schedule);

            when(calendarRepository.findById(1L)).thenReturn(Optional.of(calendar));
            when(scheduleRepository.findById(10L)).thenReturn(Optional.of(schedule));
            when(calendarRepository.save(calendar)).thenReturn(calendar);

            calendarService.removeSchedule(1L, 10L);

            assertThat(calendar.getSchedules()).doesNotContain(schedule);
        }

        @Test
        @DisplayName("Schedule belongs to different calendar – throws IllegalArgumentException")
        void removeSchedule_wrongCalendar_throwsIllegalArgumentException() {
            Calendar otherCalendar = new Calendar();
            otherCalendar.setId(99L);

            Schedule schedule = new Schedule();
            schedule.setId(10L);
            schedule.setCalendar(otherCalendar); // owned by calendar 99, not calendar 1

            when(calendarRepository.findById(1L)).thenReturn(Optional.of(calendar));
            when(scheduleRepository.findById(10L)).thenReturn(Optional.of(schedule));

            assertThatThrownBy(() -> calendarService.removeSchedule(1L, 10L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("does not belong to calendar 1");
        }

        @Test
        @DisplayName("Schedule not found – throws ResourceNotFoundException")
        void removeSchedule_scheduleNotFound_throwsException() {
            when(calendarRepository.findById(1L)).thenReturn(Optional.of(calendar));
            when(scheduleRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> calendarService.removeSchedule(1L, 99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}