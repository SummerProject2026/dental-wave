package com.dentalwave.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Data Transfer Object for Availability.
 * Exposes employee availability information to the API layer using
 * a flat employeeId instead of the full Employee entity reference.
 */
public class AvailabilityDto {

    private Long id;

    /** ID of the employee this availability record belongs to */
    private Long employeeId;

    /** Day of the week this availability applies to */
    private DayOfWeek dayOfWeek;

    /** Start of the employee's available window */
    private LocalTime startTime;

    /** End of the employee's available window */
    private LocalTime endTime;

    /** True if the employee is available; false if blocked out */
    private Boolean available;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /** Default no-arg constructor (required for JSON deserialization) */
    public AvailabilityDto() {}

    /** Full constructor */
    public AvailabilityDto(Long id, Long employeeId, DayOfWeek dayOfWeek,
                           LocalTime startTime, LocalTime endTime, Boolean available) {
        this.id = id;
        this.employeeId = employeeId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.available = available;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }
}
