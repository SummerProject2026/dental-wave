package com.dentalwave.dto;

/**
 * Data Transfer Object for Employee.
 * Used as the value type in ScheduleDto's team map and in AvailabilityDto.
 * Extend this class with additional fields as your Employee entity grows.
 */
public class EmployeeDto {

    private Long id;

    /** Employee's first name */
    private String firstName;

    /** Employee's last name */
    private String lastName;

    /** Employee's role or position (e.g. "Dental Hygienist") */
    private String role;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public EmployeeDto() {}

    public EmployeeDto(Long id, String firstName, String lastName, String role) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
