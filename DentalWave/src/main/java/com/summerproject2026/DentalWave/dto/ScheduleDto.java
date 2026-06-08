package com.summerproject2026.DentalWave.dto;

import java.time.LocalDate;

/**
 * Data Transfer Object (DTO) for Schedule.
 * Used to transfer schedule data between the backend and frontend.
 */
public class ScheduleDto {

    /**
     * Unique identifier for the schedule.
     */
    private Long id;

    /**
     * First day covered by the schedule.
     */
    private LocalDate startScheduleDate;

    /**
     * Last day covered by the schedule.
     */
    private LocalDate endScheduleDate;

    /**
     * Indicates whether the schedule has been published.
     */
    private Boolean published;

    /**
     * ID of the user who created the schedule.
     */
    private Long createdById;

    /**
     * Default constructor.
     */
    public ScheduleDto() {
    }

    /**
     * Full constructor.
     *
     * @param id unique schedule identifier
     * @param startScheduleDate first day of the schedule
     * @param endScheduleDate last day of the schedule
     * @param published publication status
     * @param createdById ID of the user who created the schedule
     */
    public ScheduleDto(Long id,
                       LocalDate startScheduleDate,
                       LocalDate endScheduleDate,
                       Boolean published,
                       Long createdById) {
        this.id = id;
        this.startScheduleDate = startScheduleDate;
        this.endScheduleDate = endScheduleDate;
        this.published = published;
        this.createdById = createdById;
    }

    /**
     * Returns the schedule ID.
     *
     * @return schedule ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the schedule ID.
     *
     * @param id schedule ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns the schedule start date.
     *
     * @return start date
     */
    public LocalDate getStartScheduleDate() {
        return startScheduleDate;
    }

    /**
     * Sets the schedule start date.
     *
     * @param startScheduleDate start date
     */
    public void setStartScheduleDate(LocalDate startScheduleDate) {
        this.startScheduleDate = startScheduleDate;
    }

    /**
     * Returns the schedule end date.
     *
     * @return end date
     */
    public LocalDate getEndScheduleDate() {
        return endScheduleDate;
    }

    /**
     * Sets the schedule end date.
     *
     * @param endScheduleDate end date
     */
    public void setEndScheduleDate(LocalDate endScheduleDate) {
        this.endScheduleDate = endScheduleDate;
    }

    /**
     * Returns whether the schedule has been published.
     *
     * @return publication status
     */
    public Boolean getPublished() {
        return published;
    }

    /**
     * Sets the publication status.
     *
     * @param published publication status
     */
    public void setPublished(Boolean published) {
        this.published = published;
    }

    /**
     * Returns the ID of the user who created the schedule.
     *
     * @return creator user ID
     */
    public Long getCreatedById() {
        return createdById;
    }

    /**
     * Sets the ID of the user who created the schedule.
     *
     * @param createdById creator user ID
     */
    public void setCreatedById(Long createdById) {
        this.createdById = createdById;
    }
}