package com.summerproject2026.DentalWave.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a scheduling calendar for a given month.
 * A calendar contains multiple schedules and is associated with a creator (User).
 * Calendars can be published to make them visible to staff.
 */
@Entity
@Table(name = "calendars")
public class Calendar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Display label for the calendar month (e.g. "June 2025") */
    @Column(nullable = false)
    private String month;

    /** The first date covered by this calendar */
    @Column(nullable = false)
    private LocalDate startCalendarDate;

    /** The last date covered by this calendar */
    @Column(nullable = false)
    private LocalDate endCalendarDate;

    /**
     * Whether this calendar has been published and is visible to employees.
     * Defaults to false (draft state).
     */
    @Column(nullable = false)
    private Boolean published = false;

    /** The user (typically a manager/admin) who created this calendar */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    /**
     * The list of daily schedules that belong to this calendar.
     * Cascade all operations so schedules are persisted/removed with the calendar.
     */
    @OneToMany(mappedBy = "calendar", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Schedule> schedules = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /** Default no-arg constructor required by JPA */
    public Calendar() {}

    /** Full constructor for programmatic creation */
    public Calendar(Long id, String month, LocalDate startCalendarDate,
                    LocalDate endCalendarDate, Boolean published,
                    User createdBy, List<Schedule> schedules) {
        this.id = id;
        this.month = month;
        this.startCalendarDate = startCalendarDate;
        this.endCalendarDate = endCalendarDate;
        this.published = published;
        this.createdBy = createdBy;
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

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public List<Schedule> getSchedules() { return schedules; }
    public void setSchedules(List<Schedule> schedules) { this.schedules = schedules; }

    // -------------------------------------------------------------------------
    // Convenience helpers
    // -------------------------------------------------------------------------

    /** Adds a schedule and sets its back-reference to this calendar */
    public void addSchedule(Schedule schedule) {
        schedules.add(schedule);
        schedule.setCalendar(this);
    }

    /** Removes a schedule and clears its back-reference */
    public void removeSchedule(Schedule schedule) {
        schedules.remove(schedule);
        schedule.setCalendar(null);
    }
}
