package com.summerproject2026.DentalWave.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * Lightweight local-only scheduling resources. These assistants do not
     * require login accounts and are used by the scheduling workspace.
     */
    @ManyToMany
    @JoinTable(
            name = "schedule_team_resources",
            joinColumns = @JoinColumn(name = "team_id"),
            inverseJoinColumns = @JoinColumn(name = "resource_id")
    )
    private List<SchedulingResource> resources = new ArrayList<>();

    /**
     * Optional per-day notes for individual team assignments.
     * Keys use "employee:{id}" or "resource:{id}" so account-backed and
     * local-only assistants can safely share the same numeric id.
     */
    @ElementCollection
    @CollectionTable(
            name = "schedule_team_assignment_notes",
            joinColumns = @JoinColumn(name = "team_id")
    )
    @MapKeyColumn(name = "assignment_key", length = 80)
    @Column(name = "partial_day_note", nullable = false, length = 40)
    private Map<String, String> assignmentNotes = new HashMap<>();
}
