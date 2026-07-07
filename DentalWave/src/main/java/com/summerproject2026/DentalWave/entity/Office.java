package com.summerproject2026.DentalWave.entity;

import jakarta.persistence.*;
import java.time.*;

/**
 * Represents one physical office location in the practice.
 *
 * Offices are used when assigning employees and building monthly schedules.
 */
@Entity
@Table(name = "offices")
public class Office {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String address;
    private String phoneNumber;

    /** Default constructor required by JPA. */
    public Office() {}

    /**
     * Creates an office with basic contact information.
     *
     * @param id office id, usually assigned by the database
     * @param name office display name
     * @param address office street address
     * @param phoneNumber office contact phone number
     */
    public Office(Long id, String name, String address, String phoneNumber) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}
