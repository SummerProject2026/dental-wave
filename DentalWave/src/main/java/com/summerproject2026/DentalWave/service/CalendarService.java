package com.summerproject2026.DentalWave.service;

import com.summerproject2026.DentalWave.dto.CalendarDto;
import com.summerproject2026.DentalWave.dto.ScheduleDto;

import java.time.LocalDate;
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
     */
    CalendarDto updateCalendar(Long id, CalendarDto calendarDto);

    /**
     * Deletes a calendar (and its schedules via cascade).
     *
     * @param id the ID of the calendar to delete
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
     */
    ScheduleDto addSchedule(Long calendarId, ScheduleDto scheduleDto);

    /**
     * Removes a schedule from a calendar and deletes it.
     *
     * @param calendarId the ID of the parent calendar
     * @param scheduleId the ID of the schedule to remove
     */
    void removeSchedule(Long calendarId, Long scheduleId);

    /**
     * Auto-generates a draft calendar for the given office and month.
     * Creates one schedule per weekday (Monday-Friday) within the date range,
     * and automatically assigns employees to teams based on role:
     * each team gets 1 Doctor + 1 TC, with Assistants distributed
     * as evenly as possible across the teams created.
     *
     * @param calendarDto the office, month, date range, and creator info
     * @return the fully populated CalendarDto, unpublished (draft state)
     */
    CalendarDto generateCalendar(CalendarDto calendarDto);

    /**
     * Schedules an employee across every day in the given calendar.
     * For each day's schedule, the employee is added to whichever
     * team currently has the fewest members, balancing team sizes.
     *
     * @param calendarId the calendar to schedule the employee into
     * @param employeeId the employee to schedule
     * @return the updated CalendarDto with the employee added to each day's smallest team
     */
    CalendarDto scheduleEmployeeAcrossCalendar(Long calendarId, Long employeeId);

    /**
     * Removes an employee from every team across every day in the
     * given calendar.
     *
     * @param calendarId the calendar to remove the employee from
     * @param employeeId the employee to remove
     * @return the updated CalendarDto with the employee removed from every team
     */
    CalendarDto removeEmployeeFromCalendar(Long calendarId, Long employeeId);

    /**
     * Removes an employee from every team on schedules that fall within
     * the given date range, across all calendars for the given office.
     * Used by Manager "Approved Request" page — "Remove from Schedule" action.
     *
     * @param officeId   the office whose calendars to search
     * @param employeeId the employee to remove
     * @param startDate  the first date of the time-off range (inclusive)
     * @param endDate    the last date of the time-off range (inclusive)
     */
    void removeEmployeeFromScheduleOnDates(Long officeId, Long employeeId,
                                           LocalDate startDate, LocalDate endDate);
}