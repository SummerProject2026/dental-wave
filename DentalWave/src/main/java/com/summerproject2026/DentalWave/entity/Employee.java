package com.summerproject2026.DentalWave.entity;

import com.summerproject2026.DentalWave.enums.WorkStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a dental practice employee.
 *
 * Every Employee is backed by a User account (authentication/roles live there).
 * The Employee entity adds HR/scheduling concerns: position, offices, availability,
 * time-off balance, and time-off requests.
 *
 * Relationships:
 *  - OneToOne   → User          (one user account per employee)
 *  - ManyToMany → Office        (employee may work across multiple locations)
 *  - OneToMany  → Availability  (weekly availability windows)
 *  - OneToMany  → TimeOffRequest (submitted PTO / leave requests)
 *
 * responsibilities is stored as an @ElementCollection to avoid a separate
 * entity for what is essentially a list of strings.
 */
@Entity
@Table(name = "employees")
@Getter
@Setter
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The User account associated with this employee.
     * Eagerly loaded so firstName / lastName / email are always available
     * without an extra query when building the DTO.
     */
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /**
     * Job title within the practice (e.g. "Dental Hygienist", "Front Desk").
     * Distinct from the security Role stored on User.
     */
    @Column(nullable = false)
    private String position;

    /**
     * The offices / locations this employee is assigned to.
     * Stored in a join table: employee_offices (employee_id, office_id).
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "employee_offices",
            joinColumns = @JoinColumn(name = "employee_id"),
            inverseJoinColumns = @JoinColumn(name = "office_id")
    )
    private List<Office> offices = new ArrayList<>();

    /**
     * Free-text job responsibilities for this employee.
     * Stored in a separate table (employee_responsibilities) as VARCHAR rows.
     */
    @ElementCollection
    @CollectionTable(
            name = "employee_responsibilities",
            joinColumns = @JoinColumn(name = "employee_id")
    )
    @Column(name = "responsibility")
    private List<String> responsibilities = new ArrayList<>();

    /** Date the employee was hired */
    @Column(nullable = false)
    private LocalDate hireDate;

    /**
     * Remaining time-off balance in days.
     * Supports half-day increments via Double (e.g. 2.5 days).
     * Defaults to 0.0 on creation and is adjusted when time-off requests are approved.
     */
    @Column(nullable = false)
    private Double timeOff = 0.0;

    /**
     * Current employment/work status.
     * Stored as a string so the enum label is human-readable in the database.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkStatus status = WorkStatus.ACTIVE;

    /**
     * Weekly availability windows used by the scheduler.
     * Cascaded so availability records are created/removed with the employee.
     */
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Availability> availabilities = new ArrayList<>();

    /**
     * PTO / leave requests submitted by or on behalf of this employee.
     * Cascaded so requests are cleaned up when the employee is deleted.
     */
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimeOffRequest> timeOffRequests = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /** Default no-arg constructor required by JPA */
    public Employee() {}

    /** Full constructor for programmatic / test creation */
    public Employee(Long id,
                    User user,
                    String position,
                    List<Office> offices,
                    List<String> responsibilities,
                    LocalDate hireDate,
                    Double timeOff,
                    WorkStatus status,
                    List<Availability> availabilities,
                    List<TimeOffRequest> timeOffRequests) {
        this.id = id;
        this.user = user;
        this.position = position;
        this.offices = offices != null ? offices : new ArrayList<>();
        this.responsibilities = responsibilities != null ? responsibilities : new ArrayList<>();
        this.hireDate = hireDate;
        this.timeOff = timeOff != null ? timeOff : 0.0;
        this.status = status != null ? status : WorkStatus.ACTIVE;
        this.availabilities = availabilities != null ? availabilities : new ArrayList<>();
        this.timeOffRequests = timeOffRequests != null ? timeOffRequests : new ArrayList<>();
    }

    // -------------------------------------------------------------------------
    // Bidirectional relationship helpers
    // -------------------------------------------------------------------------

    /** Adds an availability record and sets its back-reference to this employee */
    public void addAvailability(Availability availability) {
        availabilities.add(availability);
        availability.setEmployee(this);
    }

    /** Removes an availability record and clears its back-reference */
    public void removeAvailability(Availability availability) {
        availabilities.remove(availability);
        availability.setEmployee(null);
    }

    /** Adds a time-off request and sets its back-reference */
    public void addTimeOffRequest(TimeOffRequest request) {
        timeOffRequests.add(request);
        request.setEmployee(this);
    }

    /** Removes a time-off request and clears its back-reference */
    public void removeTimeOffRequest(TimeOffRequest request) {
        timeOffRequests.remove(request);
        request.setEmployee(null);
    }
}
