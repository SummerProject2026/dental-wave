package com.summerproject2026.DentalWave.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "scheduling_resources")
@Getter @Setter
public class SchedulingResource {
    public enum Type { DOCTOR, ASSISTANT }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Type type;
    @Column(nullable = false) private String firstName;
    @Column(nullable = false) private String lastName;
    private String displayName;
    @Column(nullable = false) private boolean active = true;
    @ManyToOne(fetch = FetchType.LAZY) private Office defaultOffice;
    @ManyToMany
    @JoinTable(name = "scheduling_resource_offices", joinColumns = @JoinColumn(name = "resource_id"), inverseJoinColumns = @JoinColumn(name = "office_id"))
    private List<Office> offices = new ArrayList<>();
    private String normalWorkdays;
    private String color;
    @Column(length = 2000) private String notes;
}
