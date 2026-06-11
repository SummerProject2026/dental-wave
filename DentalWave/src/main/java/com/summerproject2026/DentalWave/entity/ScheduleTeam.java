package com.summerproject2026.DentalWave.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a named team within a schedule.
 * e.g. "Team A", "Team B", "Team C"
 */
@Entity
@Table(name = "schedule_teams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Team name e.g. "Team A", "Team B" */
    @Column(nullable = false)
    private String name;

    /** The schedule this team belongs to */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    /** Employees assigned to this team */
    @ManyToMany
    @JoinTable(
            name = "schedule_team_employees",
            joinColumns = @JoinColumn(name = "team_id"),
            inverseJoinColumns = @JoinColumn(name = "employee_id")
    )
    private List<Employee> employees = new ArrayList<>();
}