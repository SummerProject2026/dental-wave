package com.dentalwave.service;

import com.dentalwave.dto.CalendarDto;
import com.dentalwave.dto.ScheduleDto;

import java.util.List;

/**
 * Service interface defining all business operations for Calendar management.
 * Handles CRUD operations, publishing lifecycle, and nested schedule management.
 */
public interface CalendarService {

    /**
     * Creates a new calendar from the provided DTO.
     *
     * @param calendarDto the data for the new calendar
     * @return the persisted calendar as a DTO (with generated ID)
     */
    CalendarDto createCalendar(CalendarDto calendarDto);

    /**
     * Retrieves a calendar by its ID.
     *
     * @param id the calendar ID
     * @return the matching CalendarDto
     * @throws com.dentalwave.exception.ResourceNotFoundException if not found
     */
    CalendarDto getCalendarById(Long id);

    /**
     * Returns all calendars in the system.
     *
     * @return list of all CalendarDtos (may be empty)
     */
    List<CalendarDto> getAllCalendars();

    /**
     * Updates an existing calendar with new field values.
     *
     * @param id          the ID of the calendar to update
     * @param calendarDto the updated data
     * @return the updated CalendarDto
     * @throws com.dentalwave.exception.ResourceNotFoundException if not found
     */
    CalendarDto updateCalendar(Long id, CalendarDto calendarDto);

    /**
     * Deletes a calendar (and its schedules via cascade).
     *
     * @param id the ID of the calendar to delete
     * @throws com.dentalwave.exception.ResourceNotFoundException if not found
     */
    void deleteCalendar(Long id);

    /**
     * Marks a calendar as published, making it visible to staff.
     *
     * @param id the calendar ID
     * @return the updated CalendarDto with published = true
     */
    CalendarDto publishCalendar(Long id);

    /**
     * Reverts a calendar to draft (unpublished) state.
     *
     * @param id the calendar ID
     * @return the updated CalendarDto with published = false
     */
    CalendarDto unpublishCalendar(Long id);

    /**
     * Retrieves all calendars for a given month label.
     *
     * @param month month label to filter by (e.g. "June 2025")
     * @return list of matching CalendarDtos
     */
    List<CalendarDto> getCalendarsByMonth(String month);

    /**
     * Retrieves all calendars that have been published.
     *
     * @return list of published CalendarDtos
     */
    List<CalendarDto> getPublishedCalendars();

    /**
     * Creates a new schedule and adds it to the specified calendar.
     *
     * @param calendarId  the ID of the parent calendar
     * @param scheduleDto the data for the new schedule
     * @return the persisted ScheduleDto (with generated ID)
     * @throws com.dentalwave.exception.ResourceNotFoundException if calendar not found
     */
    ScheduleDto addSchedule(Long calendarId, ScheduleDto scheduleDto);

    /**
     * Removes a schedule from a calendar and deletes it.
     *
     * @param calendarId the ID of the parent calendar
     * @param scheduleId the ID of the schedule to remove
     * @throws com.dentalwave.exception.ResourceNotFoundException if either is not found
     */
    void removeSchedule(Long calendarId, Long scheduleId);
}
