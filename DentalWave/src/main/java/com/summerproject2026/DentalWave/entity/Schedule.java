package com.summerproject2026.DentalWave.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a published or draft employee schedule.
 */
@Entity
@Table(name = "schedules")
@Getter
@Setter
@NoArgsConstructor
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    @Column(name = "start_schedule_date")
    private LocalDate startScheduleDate;

    /** Last day covered by the schedule */
    @Column(name = "end_schedule_date")
    private LocalDate endScheduleDate;

    /** Indicates whether the schedule has been published */
    @Column(nullable = false)
    private Boolean published = false;

    /** User who created the schedule */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    /** The calendar this schedule belongs to */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calendar_id")
    private Calendar calendar;

    /** Teams assigned to this schedule */
    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScheduleTeam> teams = new ArrayList<>();


}