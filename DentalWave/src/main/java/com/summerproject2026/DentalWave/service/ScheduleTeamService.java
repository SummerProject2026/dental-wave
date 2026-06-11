package com.summerproject2026.DentalWave.service;

import com.summerproject2026.DentalWave.dto.ScheduleTeamDto;

import java.util.List;

/**
 * Service interface for managing schedule teams.
 */
public interface ScheduleTeamService {

    /**
     * Creates a new team within a schedule.
     *
     * @param scheduleTeamDto the team data to create
     * @return the created team
     */
    ScheduleTeamDto createTeam(ScheduleTeamDto scheduleTeamDto);

    /**
     * Retrieves a team by its ID.
     *
     * @param id the team ID
     * @return the matching team
     */
    ScheduleTeamDto getTeamById(Long id);

    /**
     * Retrieves all teams belonging to a schedule.
     *
     * @param scheduleId the schedule ID
     * @return list of teams for that schedule
     */
    List<ScheduleTeamDto> getTeamsBySchedule(Long scheduleId);

    /**
     * Adds an employee to a team.
     *
     * @param teamId     the team ID
     * @param employeeId the employee to add
     * @return the updated team
     */
    ScheduleTeamDto addEmployeeToTeam(Long teamId, Long employeeId);

    /**
     * Removes an employee from a team.
     *
     * @param teamId     the team ID
     * @param employeeId the employee to remove
     * @return the updated team
     */
    ScheduleTeamDto removeEmployeeFromTeam(Long teamId, Long employeeId);

    /**
     * Updates the name of a team.
     *
     * @param id   the team ID
     * @param name the new team name
     * @return the updated team
     */
    ScheduleTeamDto updateTeamName(Long id, String name);

    /**
     * Deletes a team by its ID.
     *
     * @param id the team ID
     */
    void deleteTeam(Long id);
}