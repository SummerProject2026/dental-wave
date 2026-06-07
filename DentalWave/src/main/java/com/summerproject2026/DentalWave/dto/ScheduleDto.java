package com.dentalwave.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object for Schedule.
 * Replaces entity references with IDs and nested DTOs so the API
 * layer never exposes raw JPA entities.
 *
 * The teams map uses Long (userId) as the key and a list of EmployeeDto
 * as the value, mirroring the entity's Map<User, List<Employee>>.
 */
public class ScheduleDto {

    private Long id;

    /** Calendar date this schedule covers */
    private LocalDate date;

    /** Schedule start time */
    private LocalTime startTime;

    /** Schedule end time */
    private LocalTime endTime;

    /** ID of the parent calendar */
    private Long calendarId;

    /**
     * Team assignments: key = userId of the team lead / doctor,
     * value = list of employees assigned under that lead for this shift.
     */
    private Map<Long, List<EmployeeDto>> teams = new HashMap<>();

    /** Optional free-text notes for this shift */
    private String notes;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /** Default no-arg constructor (required for JSON deserialization) */
    public ScheduleDto() {}

    /** Full constructor */
    public ScheduleDto(Long id, LocalDate date, LocalTime startTime, LocalTime endTime,
                       Long calendarId, Map<Long, List<EmployeeDto>> teams, String notes) {
        this.id = id;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.calendarId = calendarId;
        this.teams = teams != null ? teams : new HashMap<>();
        this.notes = notes;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public Long getCalendarId() { return calendarId; }
    public void setCalendarId(Long calendarId) { this.calendarId = calendarId; }

    public Map<Long, List<EmployeeDto>> getTeams() { return teams; }
    public void setTeams(Map<Long, List<EmployeeDto>> teams) { this.teams = teams; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
