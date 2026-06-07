package com.dentalwave.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "schedules")
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_schedule_date", nullable = false)
    private LocalDate startScheduleDate;

    @Column(name = "end_schedule_date", nullable = false)
    private LocalDate endScheduleDate;

    @Column(nullable = false)
    private Boolean published = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Shift> shifts;

    public Schedule() {}

    public Schedule(Long id, LocalDate startScheduleDate, LocalDate endScheduleDate,
                    Boolean published, User createdBy, List<Shift> shifts) {
        this.id = id;
        this.startScheduleDate = startScheduleDate;
        this.endScheduleDate = endScheduleDate;
        this.published = published;
        this.createdBy = createdBy;
        this.shifts = shifts;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getStartScheduleDate() { return startScheduleDate; }
    public void setStartScheduleDate(LocalDate startScheduleDate) { this.startScheduleDate = startScheduleDate; }

    public LocalDate getEndScheduleDate() { return endScheduleDate; }
    public void setEndScheduleDate(LocalDate endScheduleDate) { this.endScheduleDate = endScheduleDate; }

    public Boolean getPublished() { return published; }
    public void setPublished(Boolean published) { this.published = published; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public List<Shift> getShifts() { return shifts; }
    public void setShifts(List<Shift> shifts) { this.shifts = shifts; }
}
