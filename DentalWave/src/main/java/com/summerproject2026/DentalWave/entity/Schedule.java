package com.summerproject2026.DentalWave.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Represents a published or draft employee schedule.
 */
@Entity
@Table(name = "schedules")
public class Schedule {

    /**
     * Unique identifier for the schedule.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * First day covered by the schedule.
     */
    @Column(name = "start_schedule_date", nullable = false)
    private LocalDate startScheduleDate;

    /**
     * Last day covered by the schedule.
     */
    @Column(name = "end_schedule_date", nullable = false)
    private LocalDate endScheduleDate;

    /**
     * Indicates whether the schedule has been published
     * and is visible to employees.
     */
    @Column(nullable = false)
    private Boolean published = false;

    /**
     * User who created the schedule.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    /**
     * Associated schedules.
     * (Replace this relationship if your UML changes.)
     */
    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Schedule> schedules;

    /**
     * Default constructor required by JPA.
     */
    public Schedule() {
    }

    /**
     * Full constructor.
     *
     * @param id unique identifier
     * @param startScheduleDate first day of the schedule
     * @param endScheduleDate last day of the schedule
     * @param published publication status
     * @param createdBy user who created the schedule
     * @param schedules associated schedules
     */
    public Schedule(Long id,
                    LocalDate startScheduleDate,
                    LocalDate endScheduleDate,
                    Boolean published,
                    User createdBy,
                    List<Schedule> schedules) {
        this.id = id;
        this.startScheduleDate = startScheduleDate;
        this.endScheduleDate = endScheduleDate;
        this.published = published;
        this.createdBy = createdBy;
        this.schedules = schedules;
    }

    /**
     * @return schedule id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id schedule id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return schedule start date
     */
    public LocalDate getStartScheduleDate() {
        return startScheduleDate;
    }

    /**
     * @param startScheduleDate schedule start date
     */
    public void setStartScheduleDate(LocalDate startScheduleDate) {
        this.startScheduleDate = startScheduleDate;
    }

    /**
     * @return schedule end date
     */
    public LocalDate getEndScheduleDate() {
        return endScheduleDate;
    }

    /**
     * @param endScheduleDate schedule end date
     */
    public void setEndScheduleDate(LocalDate endScheduleDate) {
        this.endScheduleDate = endScheduleDate;
    }

    /**
     * @return publication status
     */
    public Boolean getPublished() {
        return published;
    }

    /**
     * @param published publication status
     */
    public void setPublished(Boolean published) {
        this.published = published;
    }

    /**
     * @return creator of the schedule
     */
    public User getCreatedBy() {
        return createdBy;
    }

    /**
     * @param createdBy creator of the schedule
     */
    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * @return associated schedules
     */
    public List<Schedule> getSchedules() {
        return schedules;
    }

    /**
     * @param schedules associated schedules
     */
    public void setSchedules(List<Schedule> schedules) {
        this.schedules = schedules;
    }
}