package com.dentalwave.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for Calendar.
 * Used to expose calendar data to the API layer without leaking
 * JPA-managed entities. Replaces the User reference with flat
 * createdById / createdByName fields for simplicity.
 */
public class CalendarDto {

    private Long id;

    /** Human-readable month label, e.g. "June 2025" */
    private String month;

    /** First date of the calendar period */
    private LocalDate startCalendarDate;

    /** Last date of the calendar period */
    private LocalDate endCalendarDate;

    /** Whether the calendar has been published for staff to view */
    private Boolean published;

    /** ID of the user who created the calendar */
    private Long createdById;

    /** Display name of the user who created the calendar */
    private String createdByName;

    /** Nested schedule DTOs belonging to this calendar */
    private List<ScheduleDto> schedules = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /** Default no-arg constructor (required for JSON deserialization) */
    public CalendarDto() {}

    /** Full constructor */
    public CalendarDto(Long id, String month, LocalDate startCalendarDate,
                       LocalDate endCalendarDate, Boolean published,
                       Long createdById, String createdByName,
                       List<ScheduleDto> schedules) {
        this.id = id;
        this.month = month;
        this.startCalendarDate = startCalendarDate;
        this.endCalendarDate = endCalendarDate;
        this.published = published;
        this.createdById = createdById;
        this.createdByName = createdByName;
        this.schedules = schedules != null ? schedules : new ArrayList<>();
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public LocalDate getStartCalendarDate() { return startCalendarDate; }
    public void setStartCalendarDate(LocalDate startCalendarDate) { this.startCalendarDate = startCalendarDate; }

    public LocalDate getEndCalendarDate() { return endCalendarDate; }
    public void setEndCalendarDate(LocalDate endCalendarDate) { this.endCalendarDate = endCalendarDate; }

    public Boolean getPublished() { return published; }
    public void setPublished(Boolean published) { this.published = published; }

    public Long getCreatedById() { return createdById; }
    public void setCreatedById(Long createdById) { this.createdById = createdById; }

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

    public List<ScheduleDto> getSchedules() { return schedules; }
    public void setSchedules(List<ScheduleDto> schedules) { this.schedules = schedules; }
}
