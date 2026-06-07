package com.dentalwave.service.impl;

import com.dentalwave.dto.AvailabilityDto;
import com.dentalwave.exception.ResourceNotFoundException;
import com.dentalwave.mapper.AvailabilityMapper;
import com.dentalwave.model.Availability;
import com.dentalwave.model.Employee;
import com.dentalwave.repository.AvailabilityRepository;
import com.dentalwave.repository.EmployeeRepository;
import com.dentalwave.service.AvailabilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of AvailabilityService.
 * Manages employee availability records used for scheduling conflict detection
 * and schedule generation suggestions.
 */
@Service
@Transactional
public class AvailabilityServiceImpl implements AvailabilityService {

    private final AvailabilityRepository availabilityRepository;
    private final EmployeeRepository employeeRepository;
    private final AvailabilityMapper availabilityMapper;

    @Autowired
    public AvailabilityServiceImpl(AvailabilityRepository availabilityRepository,
                                   EmployeeRepository employeeRepository,
                                   AvailabilityMapper availabilityMapper) {
        this.availabilityRepository = availabilityRepository;
        this.employeeRepository = employeeRepository;
        this.availabilityMapper = availabilityMapper;
    }

    // -------------------------------------------------------------------------
    // Create
    // -------------------------------------------------------------------------

    /**
     * Creates a new availability record.
     * Resolves the employee stub from the DTO's employeeId before persisting.
     */
    @Override
    public AvailabilityDto createAvailability(AvailabilityDto availabilityDto) {
        Availability availability = availabilityMapper.mapToAvailability(availabilityDto);

        // Replace stub Employee with a fully managed entity
        if (availabilityDto.getEmployeeId() != null) {
            Employee employee = employeeRepository.findById(availabilityDto.getEmployeeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Employee not found with id: " + availabilityDto.getEmployeeId()));
            availability.setEmployee(employee);
        }

        Availability saved = availabilityRepository.save(availability);
        return availabilityMapper.mapToAvailabilityDto(saved);
    }

    // -------------------------------------------------------------------------
    // Read
    // -------------------------------------------------------------------------

    /**
     * Returns all availability records for a specific employee.
     * Typically returns up to 7 records (one per day of the week).
     */
    @Override
    @Transactional(readOnly = true)
    public List<AvailabilityDto> getAvailabilityByEmployee(Long employeeId) {
        // Verify the employee exists before querying
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee not found with id: " + employeeId);
        }

        return availabilityRepository.findByEmployeeId(employeeId).stream()
                .map(availabilityMapper::mapToAvailabilityDto)
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------

    /**
     * Updates the day, time window, and available flag of an existing record.
     * The employee reference is not changed on update.
     */
    @Override
    public AvailabilityDto updateAvailability(Long id, AvailabilityDto availabilityDto) {
        Availability existing = findAvailabilityOrThrow(id);

        existing.setDayOfWeek(availabilityDto.getDayOfWeek());
        existing.setStartTime(availabilityDto.getStartTime());
        existing.setEndTime(availabilityDto.getEndTime());
        existing.setAvailable(availabilityDto.getAvailable());

        // Allow re-assigning to a different employee if requested
        if (availabilityDto.getEmployeeId() != null
                && !availabilityDto.getEmployeeId().equals(existing.getEmployee().getId())) {
            Employee newEmployee = employeeRepository.findById(availabilityDto.getEmployeeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Employee not found with id: " + availabilityDto.getEmployeeId()));
            existing.setEmployee(newEmployee);
        }

        return availabilityMapper.mapToAvailabilityDto(availabilityRepository.save(existing));
    }

    // -------------------------------------------------------------------------
    // Delete
    // -------------------------------------------------------------------------

    /** Deletes an availability record by ID */
    @Override
    public void deleteAvailability(Long id) {
        availabilityRepository.delete(findAvailabilityOrThrow(id));
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /** Fetches an availability record or throws ResourceNotFoundException */
    private Availability findAvailabilityOrThrow(Long id) {
        return availabilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Availability not found with id: " + id));
    }
}
