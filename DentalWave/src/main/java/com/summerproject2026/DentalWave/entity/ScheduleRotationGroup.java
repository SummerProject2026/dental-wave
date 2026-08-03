package com.summerproject2026.DentalWave.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "schedule_rotation_groups")
@Getter @Setter
public class ScheduleRotationGroup {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String name;
    @Column(nullable = false)
    private int numberOfWeeks = 2;
    @Column(nullable = false)
    private LocalDate anchorDate;
    @Column(nullable = false)
    private boolean active = true;
}
