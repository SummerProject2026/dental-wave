package com.summerproject2026.DentalWave.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.DayOfWeek;
import java.time.LocalDate;

@Entity
@Table(name = "doctor_work_rules")
@Getter @Setter
public class DoctorWorkRule {
    public enum WorkStatus { WORKING, NOT_WORKING }
    public enum RecurrenceType { EVERY_WEEK, ROTATING }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) private SchedulingResource doctor;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private DayOfWeek dayOfWeek;
    @ManyToOne private Office office;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private WorkStatus workStatus;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private RecurrenceType recurrenceType;
    @ManyToOne private ScheduleRotationGroup rotationGroup;
    private Integer rotationPosition;
    private LocalDate effectiveStartDate;
    private LocalDate effectiveEndDate;
    @Column(nullable = false) private boolean active = true;
}
