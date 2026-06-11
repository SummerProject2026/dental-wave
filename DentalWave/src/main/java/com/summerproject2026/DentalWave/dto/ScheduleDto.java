package com.summerproject2026.DentalWave.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object for Schedule.
 */
public class ScheduleDto {

    /** Unique identifier for the schedule. */
    private Long id;

    /** The specific date this schedule covers */
    private LocalDate date;

    /** Start time of the shift */
    private LocalTime startTime;

    /** End time of the shift */
    private LocalTime endTime;

    /** Optional notes for this schedule */
    private String notes;

    /** First day covered by the schedule */
    private LocalDate startScheduleDate;

    /** Last day covered by the schedule */
    private LocalDate endScheduleDate;

    /** Indicates whether the schedule has been published */
    private Boolean published;

    /** ID of the user who created the schedule */
    private Long createdById;

    /** ID of the calendar this schedule belongs to */
    private Long calendarId;

    /** Team assignments: team lead user ID → list of employees */
    private Map<Long, List<EmployeeDto>> teams;

    public ScheduleDto() {}

    public ScheduleDto(Long id, LocalDate startScheduleDate,
                       LocalDate endScheduleDate, Boolean published,
                       Long createdById) {
        this.id = id;
        this.startScheduleDate = startScheduleDate;
        this.endScheduleDate = endScheduleDate;
        this.published = published;
        this.createdById = createdById;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDate getStartScheduleDate() { return startScheduleDate; }
    public void setStartScheduleDate(LocalDate startScheduleDate) { this.startScheduleDate = startScheduleDate; }

    public LocalDate getEndScheduleDate() { return endScheduleDate; }
    public void setEndScheduleDate(LocalDate endScheduleDate) { this.endScheduleDate = endScheduleDate; }

    public Boolean getPublished() { return published; }
    public void setPublished(Boolean published) { this.published = published; }

    public Long getCreatedById() { return createdById; }
    public void setCreatedById(Long createdById) { this.createdById = createdById; }

    public Long getCalendarId() { return calendarId; }
    public void setCalendarId(Long calendarId) { this.calendarId = calendarId; }

    public Map<Long, List<EmployeeDto>> getTeams() { return teams; }
    public void setTeams(Map<Long, List<EmployeeDto>> teams) { this.teams = teams; }
}