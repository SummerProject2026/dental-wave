package com.summerproject2026.DentalWave.service;

import com.summerproject2026.DentalWave.dto.AvailabilityDto;
import com.summerproject2026.DentalWave.dto.EmployeeDto;
import com.summerproject2026.DentalWave.enums.WorkStatus;

import java.util.List;

/**
 * Service interface defining all business operations for Employee management.
 *
 * Covers:
 *  - Full CRUD for employees
 *  - Filtering by office and work status
 *  - Nested availability management (add / update / delete)
 */
public interface EmployeeService {

    /**
     * Creates and persists a new employee.
     * The linked User must already exist; userId in the DTO is used to resolve it.
     */
    EmployeeDto createEmployee(EmployeeDto employeeDto);

    /**
     * Retrieves an employee by primary key.
     *
     * @throws com.summerproject2026.DentalWave.exception.ResourceNotFoundException if not found
     */
    EmployeeDto getEmployeeById(Long id);

    /** Returns all employees in the system (may be empty). */
    List<EmployeeDto> getAllEmployees();

    /**
     * Updates an existing employee's fields.
     * The linked User account is NOT changed on update.
     *
     * @throws com.summerproject2026.DentalWave.exception.ResourceNotFoundException if not found
     */
    EmployeeDto updateEmployee(Long id, EmployeeDto employeeDto);

    /**
     * Deletes an employee and all cascaded data (availabilities, time-off requests).
     *
     * @throws com.summerproject2026.DentalWave.exception.ResourceNotFoundException if not found
     */
    void deleteEmployee(Long id);

    /**
     * Returns all employees assigned to a given office.
     */
    List<EmployeeDto> getEmployeesByOffice(Long officeId);

    /**
     * Returns all employees with the given work status (e.g. ACTIVE, ON_LEAVE).
     */
    List<EmployeeDto> getEmployeesByStatus(WorkStatus status);

    /**
     * Adds a new availability record to an employee's schedule.
     *
     * @throws com.summerproject2026.DentalWave.exception.ResourceNotFoundException if employee not found
     */
    AvailabilityDto addAvailability(Long employeeId, AvailabilityDto availabilityDto);

    /**
     * Updates an existing availability record that belongs to the given employee.
     *
     * @throws com.summerproject2026.DentalWave.exception.ResourceNotFoundException if either not found
     * @throws IllegalArgumentException if the availability does not belong to this employee
     */
    AvailabilityDto updateAvailability(Long employeeId, Long availabilityId,
                                       AvailabilityDto availabilityDto);

    /**
     * Deletes an availability record from an employee.
     *
     * @throws com.summerproject2026.DentalWave.exception.ResourceNotFoundException if either not found
     * @throws IllegalArgumentException if the availability does not belong to this employee
     */
    void deleteAvailability(Long employeeId, Long availabilityId);
}
