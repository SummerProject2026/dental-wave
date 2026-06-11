package com.summerproject2026.DentalWave.service;

import com.summerproject2026.DentalWave.dto.AvailabilityDto;

import java.util.List;

/**
 * Service interface defining business operations for managing
 * employee availability records.
 */
public interface AvailabilityService {

    /**
     * Creates a new availability record for an employee.
     *
     * @param availabilityDto the data for the new record
     * @return the persisted AvailabilityDto (with generated ID)
     */
    AvailabilityDto createAvailability(AvailabilityDto availabilityDto);

    /**
     * Returns all availability records for a specific employee.
     *
     * @param employeeId the ID of the employee
     * @return list of AvailabilityDtos (one per day of week configured)
     */
    List<AvailabilityDto> getAvailabilityByEmployee(Long employeeId);

    /**
     * Updates an existing availability record.
     *
     * @param id              the ID of the record to update
     * @param availabilityDto the new values
     * @return the updated AvailabilityDto
     * @throws com.dentalwave.exception.ResourceNotFoundException if not found
     */
    AvailabilityDto updateAvailability(Long id, AvailabilityDto availabilityDto);

    /**
     * Deletes an availability record by ID.
     *
     * @param id the ID of the record to delete
     * @throws com.dentalwave.exception.ResourceNotFoundException if not found
     */
    void deleteAvailability(Long id);
}
