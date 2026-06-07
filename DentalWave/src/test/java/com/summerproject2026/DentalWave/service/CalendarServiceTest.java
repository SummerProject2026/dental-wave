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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CalendarServiceImpl.
 *
 * All collaborators are mocked — no Spring context or database involved.
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

    // ── Fixtures ──────────────────────────────────────────────────────────────

    private User creator;
    private Calendar calendar;
    private CalendarDto calendarDto;

    @BeforeEach
    void setUp() {
        creator = new User();
        creator.setId(1L);
        creator.setFirstName("Admin");

        calendar = new Calendar();
        calendar.setId(100L);
        calendar.setMonth("June 2025");
        calendar.setStartCalendarDate(LocalDate.of(2025, 6, 1));
        calendar.setEndCalendarDate(LocalDate.of(2025, 6, 30));
        calendar.setPublished(false);
        calendar.setCreatedBy(creator);

        calendarDto = new CalendarDto();
        calendarDto.setId(100L);
        calendarDto.setMonth("June 2025");
        calendarDto.setStartCalendarDate(LocalDate.of(2025, 6, 1));
        calendarDto.setEndCalendarDate(LocalDate.of(2025, 6, 30));
        calendarDto.setPublished(false);
        calendarDto.setCreatedById(1L);
    }

    // -------------------------------------------------------------------------
    // createCalendar
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("createCalendar — persists calendar and returns mapped DTO")
    void createCalendar_success() {
        when(calendarMapper.mapToCalendar(calendarDto)).thenReturn(calendar);
        when(userRepository.findById(1L)).thenReturn(Optional.of(creator));
        when(calendarRepository.save(calendar)).thenReturn(calendar);
        when(calendarMapper.mapToCalendarDto(calendar)).thenReturn(calendarDto);

        CalendarDto result = calendarService.createCalendar(calendarDto);

        assertThat(result).isEqualTo(calendarDto);
        verify(calendarRepository).save(calendar);
    }

    @Test
    @DisplayName("createCalendar — throws ResourceNotFoundException when creator user not found")
    void createCalendar_userNotFound_throws() {
        when(calendarMapper.mapToCalendar(calendarDto)).thenReturn(calendar);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> calendarService.createCalendar(calendarDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id: 1");

        verify(calendarRepository, never()).save(any());
    }

    @Test
    @DisplayName("createCalendar — skips user lookup when createdById is null")
    void createCalendar_nullCreatedById_skipsUserLookup() {
        calendarDto.setCreatedById(null);
        when(calendarMapper.mapToCalendar(calendarDto)).thenReturn(calendar);
        when(calendarRepository.save(calendar)).thenReturn(calendar);
        when(calendarMapper.mapToCalendarDto(calendar)).thenReturn(calendarDto);

        calendarService.createCalendar(calendarDto);

        verify(userRepository, never()).findById(any());
        verify(calendarRepository).save(calendar);
    }

    // -------------------------------------------------------------------------
    // getCalendarById
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getCalendarById — returns mapped DTO for existing calendar")
    void getCalendarById_success() {
        when(calendarRepository.findById(100L)).thenReturn(Optional.of(calendar));
        when(calendarMapper.mapToCalendarDto(calendar)).thenReturn(calendarDto);

        CalendarDto result = calendarService.getCalendarById(100L);

        assertThat(result).isEqualTo(calendarDto);
    }

    @Test
    @DisplayName("getCalendarById — throws ResourceNotFoundException when not found")
    void getCalendarById_notFound_throws() {
        when(calendarRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> calendarService.getCalendarById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Calendar not found with id: 99");
    }

    // -------------------------------------------------------------------------
    // getAllCalendars
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getAllCalendars — returns mapped list of all calendars")
    void getAllCalendars_success() {
        Calendar calendar2 = new Calendar();
        calendar2.setId(101L);
        CalendarDto dto2 = new CalendarDto();
        dto2.setId(101L);

        when(calendarRepository.findAll()).thenReturn(List.of(calendar, calendar2));
        when(calendarMapper.mapToCalendarDto(calendar)).thenReturn(calendarDto);
        when(calendarMapper.mapToCalendarDto(calendar2)).thenReturn(dto2);

        List<CalendarDto> result = calendarService.getAllCalendars();

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("getAllCalendars — returns empty list when no calendars exist")
    void getAllCalendars_empty() {
        when(calendarRepository.findAll()).thenReturn(List.of());

        List<CalendarDto> result = calendarService.getAllCalendars();

        assertThat(result).isEmpty();
    }

    // -------------------------------------------------------------------------
    // getCalendarsByMonth
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getCalendarsByMonth — returns matching calendars for given month")
    void getCalendarsByMonth_success() {
        when(calendarRepository.findByMonth("June 2025")).thenReturn(List.of(calendar));
        when(calendarMapper.mapToCalendarDto(calendar)).thenReturn(calendarDto);

        List<CalendarDto> result = calendarService.getCalendarsByMonth("June 2025");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMonth()).isEqualTo("June 2025");
    }

    @Test
    @DisplayName("getCalendarsByMonth — returns empty list when no calendars match")
    void getCalendarsByMonth_noMatch_returnsEmpty() {
        when(calendarRepository.findByMonth("December 2030")).thenReturn(List.of());

        List<CalendarDto> result = calendarService.getCalendarsByMonth("December 2030");

        assertThat(result).isEmpty();
    }

    // -------------------------------------------------------------------------
    // getPublishedCalendars
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getPublishedCalendars — returns only published calendars")
    void getPublishedCalendars_success() {
        calendar.setPublished(true);
        calendarDto.setPublished(true);

        when(calendarRepository.findByPublishedTrue()).thenReturn(List.of(calendar));
        when(calendarMapper.mapToCalendarDto(calendar)).thenReturn(calendarDto);

        List<CalendarDto> result = calendarService.getPublishedCalendars();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPublished()).isTrue();
    }

    // -------------------------------------------------------------------------
    // updateCalendar
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("updateCalendar — updates scalar fields and returns mapped DTO")
    void updateCalendar_success() {
        CalendarDto updateDto = new CalendarDto();
        updateDto.setMonth("July 2025");
        updateDto.setStartCalendarDate(LocalDate.of(2025, 7, 1));
        updateDto.setEndCalendarDate(LocalDate.of(2025, 7, 31));
        updateDto.setPublished(true);
        updateDto.setCreatedById(null);

        CalendarDto updatedResult = new CalendarDto();
        updatedResult.setMonth("July 2025");

        when(calendarRepository.findById(100L)).thenReturn(Optional.of(calendar));
        when(calendarRepository.save(calendar)).thenReturn(calendar);
        when(calendarMapper.mapToCalendarDto(calendar)).thenReturn(updatedResult);

        CalendarDto result = calendarService.updateCalendar(100L, updateDto);

        assertThat(result.getMonth()).isEqualTo("July 2025");
        verify(calendarRepository).save(calendar);
    }

    @Test
    @DisplayName("updateCalendar — throws ResourceNotFoundException when calendar not found")
    void updateCalendar_notFound_throws() {
        when(calendarRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> calendarService.updateCalendar(99L, calendarDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Calendar not found with id: 99");
    }

    @Test
    @DisplayName("updateCalendar — does not change published flag when not provided in DTO")
    void updateCalendar_nullPublished_doesNotChangeFlag() {
        CalendarDto updateDto = new CalendarDto();
        updateDto.setMonth("June 2025");
        updateDto.setStartCalendarDate(LocalDate.of(2025, 6, 1));
        updateDto.setEndCalendarDate(LocalDate.of(2025, 6, 30));
        updateDto.setPublished(null); // not provided

        when(calendarRepository.findById(100L)).thenReturn(Optional.of(calendar));
        when(calendarRepository.save(calendar)).thenReturn(calendar);
        when(calendarMapper.mapToCalendarDto(calendar)).thenReturn(calendarDto);

        calendarService.updateCalendar(100L, updateDto);

        // published remains false (original value), was not overwritten
        assertThat(calendar.getPublished()).isFalse();
    }

    // -------------------------------------------------------------------------
    // deleteCalendar
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("deleteCalendar — deletes calendar when it exists")
    void deleteCalendar_success() {
        when(calendarRepository.findById(100L)).thenReturn(Optional.of(calendar));

        calendarService.deleteCalendar(100L);

        verify(calendarRepository).delete(calendar);
    }

    @Test
    @DisplayName("deleteCalendar — throws ResourceNotFoundException when not found")
    void deleteCalendar_notFound_throws() {
        when(calendarRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> calendarService.deleteCalendar(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Calendar not found with id: 99");

        verify(calendarRepository, never()).delete(any());
    }

    // -------------------------------------------------------------------------
    // publishCalendar / unpublishCalendar
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("publishCalendar — sets published to true and saves")
    void publishCalendar_success() {
        CalendarDto publishedDto = new CalendarDto();
        publishedDto.setPublished(true);

        when(calendarRepository.findById(100L)).thenReturn(Optional.of(calendar));
        when(calendarRepository.save(calendar)).thenReturn(calendar);
        when(calendarMapper.mapToCalendarDto(calendar)).thenReturn(publishedDto);

        CalendarDto result = calendarService.publishCalendar(100L);

        assertThat(calendar.getPublished()).isTrue();
        assertThat(result.getPublished()).isTrue();
        verify(calendarRepository).save(calendar);
    }

    @Test
    @DisplayName("publishCalendar — throws ResourceNotFoundException when calendar not found")
    void publishCalendar_notFound_throws() {
        when(calendarRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> calendarService.publishCalendar(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Calendar not found with id: 99");
    }

    @Test
    @DisplayName("unpublishCalendar — sets published to false and saves")
    void unpublishCalendar_success() {
        calendar.setPublished(true);

        CalendarDto unpublishedDto = new CalendarDto();
        unpublishedDto.setPublished(false);

        when(calendarRepository.findById(100L)).thenReturn(Optional.of(calendar));
        when(calendarRepository.save(calendar)).thenReturn(calendar);
        when(calendarMapper.mapToCalendarDto(calendar)).thenReturn(unpublishedDto);

        CalendarDto result = calendarService.unpublishCalendar(100L);

        assertThat(calendar.getPublished()).isFalse();
        assertThat(result.getPublished()).isFalse();
        verify(calendarRepository).save(calendar);
    }

    @Test
    @DisplayName("unpublishCalendar — throws ResourceNotFoundException when calendar not found")
    void unpublishCalendar_notFound_throws() {
        when(calendarRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> calendarService.unpublishCalendar(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Calendar not found with id: 99");
    }

    // -------------------------------------------------------------------------
    // addSchedule
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("addSchedule — creates schedule, links to calendar, and returns DTO")
    void addSchedule_success() {
        Schedule schedule = new Schedule();
        schedule.setId(200L);

        ScheduleDto scheduleDto = new ScheduleDto();
        scheduleDto.setId(200L);

        when(calendarRepository.findById(100L)).thenReturn(Optional.of(calendar));
        when(scheduleMapper.mapToSchedule(scheduleDto)).thenReturn(schedule);
        when(calendarRepository.save(calendar)).thenReturn(calendar);
        when(scheduleMapper.mapToScheduleDto(schedule)).thenReturn(scheduleDto);

        ScheduleDto result = calendarService.addSchedule(100L, scheduleDto);

        assertThat(result.getId()).isEqualTo(200L);
        verify(calendarRepository).save(calendar);
        // Verify schedule was linked to the calendar via addSchedule()
        assertThat(calendar.getSchedules()).contains(schedule);
    }

    @Test
    @DisplayName("addSchedule — throws ResourceNotFoundException when calendar not found")
    void addSchedule_calendarNotFound_throws() {
        when(calendarRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> calendarService.addSchedule(99L, new ScheduleDto()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Calendar not found with id: 99");
    }

    // -------------------------------------------------------------------------
    // removeSchedule
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("removeSchedule — unlinks and deletes schedule from calendar")
    void removeSchedule_success() {
        Schedule schedule = new Schedule();
        schedule.setId(200L);
        schedule.setCalendar(calendar);
        calendar.addSchedule(schedule);

        when(calendarRepository.findById(100L)).thenReturn(Optional.of(calendar));
        when(scheduleRepository.findById(200L)).thenReturn(Optional.of(schedule));

        calendarService.removeSchedule(100L, 200L);

        verify(calendarRepository).save(calendar);
        assertThat(calendar.getSchedules()).doesNotContain(schedule);
    }

    @Test
    @DisplayName("removeSchedule — throws ResourceNotFoundException when calendar not found")
    void removeSchedule_calendarNotFound_throws() {
        when(calendarRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> calendarService.removeSchedule(99L, 200L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Calendar not found with id: 99");
    }

    @Test
    @DisplayName("removeSchedule — throws ResourceNotFoundException when schedule not found")
    void removeSchedule_scheduleNotFound_throws() {
        when(calendarRepository.findById(100L)).thenReturn(Optional.of(calendar));
        when(scheduleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> calendarService.removeSchedule(100L, 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Schedule not found with id: 99");
    }

    @Test
    @DisplayName("removeSchedule — throws IllegalArgumentException when schedule belongs to different calendar")
    void removeSchedule_scheduleBelongsToDifferentCalendar_throws() {
        Calendar otherCalendar = new Calendar();
        otherCalendar.setId(999L);

        Schedule schedule = new Schedule();
        schedule.setId(200L);
        schedule.setCalendar(otherCalendar); // belongs to a DIFFERENT calendar

        when(calendarRepository.findById(100L)).thenReturn(Optional.of(calendar));
        when(scheduleRepository.findById(200L)).thenReturn(Optional.of(schedule));

        assertThatThrownBy(() -> calendarService.removeSchedule(100L, 200L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Schedule 200 does not belong to calendar 100");
    }
}