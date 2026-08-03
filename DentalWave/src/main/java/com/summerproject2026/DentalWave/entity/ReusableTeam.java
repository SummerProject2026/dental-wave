package com.summerproject2026.DentalWave.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reusable_teams")
@Getter @Setter
public class ReusableTeam {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true) private String name;
    @ManyToOne(optional = false) private SchedulingResource doctor;
    @ManyToMany
    @JoinTable(name = "reusable_team_assistants", joinColumns = @JoinColumn(name = "team_id"), inverseJoinColumns = @JoinColumn(name = "resource_id"))
    private List<SchedulingResource> assistants = new ArrayList<>();
    @ManyToOne private Office defaultOffice;
    private String color;
    @Column(length = 2000) private String notes;
    @Column(nullable = false) private boolean active = true;
}
