package com.summerproject2026.DentalWave.service;

import com.summerproject2026.DentalWave.dto.ScheduleDto;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface defining all business operations for Schedule management.
 * Includes CRUD, filtering, team assignment, and publishing.
 */
public interface ScheduleService {

    /**
     * Creates a new standalone schedule (not linked to a calendar at creation time).
     * Use CalendarService#addSchedule to create a schedule inside a calendar.
     *
     * @param scheduleDto the data for the new schedule
     * @return the persisted ScheduleDto
     */
    ScheduleDto createSchedule(ScheduleDto scheduleDto);

    /**
     * Retrieves a schedule by its ID.
     *
     * @param id the schedule ID
     * @return the matching ScheduleDto
     * @throws com.dentalwave.exception.ResourceNotFoundException if not found
     */
    ScheduleDto getScheduleById(Long id);

    /**
     * Returns all schedules in the system.
     *
     * @return list of all ScheduleDtos (may be empty)
     */
    List<ScheduleDto> getAllSchedules();

    /**
     * Updates an existing schedule with new field values.
     *
     * @param id          the ID of the schedule to update
     * @param scheduleDto the updated data
     * @return the updated ScheduleDto
     * @throws com.dentalwave.exception.ResourceNotFoundException if not found
     */
    ScheduleDto updateSchedule(Long id, ScheduleDto scheduleDto);

    /**
     * Deletes a schedule by ID.
     *
     * @param id the ID of the schedule to delete
     * @throws com.dentalwave.exception.ResourceNotFoundException if not found
     */
    void deleteSchedule(Long id);

    /**
     * Returns all schedules that fall on a specific date.
     *
     * @param date the date to filter by
     * @return list of matching ScheduleDtos
     */
    List<ScheduleDto> getSchedulesByDate(LocalDate date);

    /**
     * Returns all schedules belonging to a given calendar.
     *
     * @param calendarId the parent calendar ID
     * @return list of matching ScheduleDtos
     */
    List<ScheduleDto> getSchedulesByCalendar(Long calendarId);

    /**
     * Assigns an employee to a specific team lead's team for this schedule.
     * If the user key doesn't exist in the teams map yet, it is created.
     *
     * @param scheduleId the schedule to update
     * @param userId     the ID of the team lead (User)
     * @param employeeId the ID of the Employee to add
     * @return the updated ScheduleDto
     * @throws com.dentalwave.exception.ResourceNotFoundException if any entity is not found
     */
    ScheduleDto assignEmployeeToTeam(Long scheduleId, Long userId, Long employeeId);

    /**
     * Removes an employee from a specific team lead's team for this schedule.
     *
     * @param scheduleId the schedule to update
     * @param userId     the ID of the team lead (User)
     * @param employeeId the ID of the Employee to remove
     * @return the updated ScheduleDto
     * @throws com.dentalwave.exception.ResourceNotFoundException if any entity is not found
     */
    ScheduleDto removeEmployeeFromTeam(Long scheduleId, Long userId, Long employeeId);

    /**
     * Marks a schedule as published.
     * (Requires a 'published' flag on Schedule entity — add it if not present.)
     *
     * @param id the schedule ID
     * @return the updated ScheduleDto
     */
    ScheduleDto publishSchedule(Long id);
}
