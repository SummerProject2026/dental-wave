package com.summerproject2026.DentalWave.dto;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for ScheduleTeam.
 * Represents a named team within a schedule.
 */
public class ScheduleTeamDto {

    /** Unique identifier for the team */
    private Long id;

    /** Team name e.g. "Team A", "Team B" */
    private String name;

    /** ID of the schedule this team belongs to */
    private Long scheduleId;

    /** Employees assigned to this team */
    private List<EmployeeDto> employees = new ArrayList<>();

    public ScheduleTeamDto() {}

    public ScheduleTeamDto(Long id, String name, Long scheduleId,
                           List<EmployeeDto> employees) {
        this.id = id;
        this.name = name;
        this.scheduleId = scheduleId;
        this.employees = employees != null ? employees : new ArrayList<>();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }

    public List<EmployeeDto> getEmployees() { return employees; }
    public void setEmployees(List<EmployeeDto> employees) { this.employees = employees; }
}