package com.summerproject2026.DentalWave.entity;

import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Represents an employee's usual availability for one day of the week.
 *
 * The scheduler uses this to avoid assigning employees outside their
 * available working windows when possible.
 */
@Entity
@Table(name = "availabilities")
public class Availability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private Boolean available = true;

    /** Default constructor required by JPA. */
    public Availability() {}

    /**
     * Creates an availability window for an employee.
     *
     * @param id availability id, usually assigned by the database
     * @param employee employee this availability belongs to
     * @param dayOfWeek day of week this window applies to
     * @param startTime time the employee becomes available
     * @param endTime time the employee stops being available
     * @param available whether the employee is available during this window
     */
    public Availability(Long id, Employee employee, DayOfWeek dayOfWeek,
                        LocalTime startTime, LocalTime endTime, Boolean available) {
        this.id = id;
        this.employee = employee;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.available = available;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }
}
