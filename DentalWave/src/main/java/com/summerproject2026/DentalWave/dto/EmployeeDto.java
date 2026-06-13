package com.summerproject2026.DentalWave.dto;

import com.summerproject2026.DentalWave.enums.WorkStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for Employee.
 *
 * Flattens the associated User into scalar fields
 * so the API layer never exposes JPA-managed entities directly.
 */
public class EmployeeDto {

    private Long id;

    /** ID of the linked User account */
    private Long userId;

    /** Pulled from User.firstName */
    private String firstName;

    /** Pulled from User.lastName */
    private String lastName;

    /** Pulled from User.username */
    private String username;

    /** Pulled from User.email */
    private String email;

    /** Job title within the dental practice */
    private String position;

    /** Offices / locations this employee is assigned to */
    private List<OfficeDto> offices = new ArrayList<>();

    /** Free-text job responsibilities */
    private List<String> responsibilities = new ArrayList<>();

    /** Date the employee was hired */
    private LocalDate hireDate;

    /** Remaining time-off balance in days */
    private Double timeOff;

    /** Current work/employment status */
    private WorkStatus status;

    /** Weekly availability windows */
    private List<AvailabilityDto> availabilities = new ArrayList<>();

    public EmployeeDto() {}

    public EmployeeDto(Long id, Long userId, String firstName, String lastName,
                       String username, String email, String position,
                       List<OfficeDto> offices,
                       List<String> responsibilities, LocalDate hireDate,
                       Double timeOff, WorkStatus status,
                       List<AvailabilityDto> availabilities) {
        this.id = id;
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.email = email;
        this.position = position;
        this.offices = offices != null ? offices : new ArrayList<>();
        this.responsibilities = responsibilities != null ? responsibilities : new ArrayList<>();
        this.hireDate = hireDate;
        this.timeOff = timeOff;
        this.status = status;
        this.availabilities = availabilities != null ? availabilities : new ArrayList<>();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public List<OfficeDto> getOffices() { return offices; }
    public void setOffices(List<OfficeDto> offices) {
        this.offices = offices != null ? offices : new ArrayList<>();
    }

    public List<String> getResponsibilities() { return responsibilities; }
    public void setResponsibilities(List<String> responsibilities) {
        this.responsibilities = responsibilities != null ? responsibilities : new ArrayList<>();
    }

    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }

    public Double getTimeOff() { return timeOff; }
    public void setTimeOff(Double timeOff) { this.timeOff = timeOff; }

    public WorkStatus getStatus() { return status; }
    public void setStatus(WorkStatus status) { this.status = status; }

    public List<AvailabilityDto> getAvailabilities() { return availabilities; }
    public void setAvailabilities(List<AvailabilityDto> availabilities) {
        this.availabilities = availabilities != null ? availabilities : new ArrayList<>();
    }
}