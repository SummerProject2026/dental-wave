package com.summerproject2026.DentalWave.service.impl;

import com.summerproject2026.DentalWave.dto.AvailabilityDto;
import com.summerproject2026.DentalWave.dto.EmployeeDto;
import com.summerproject2026.DentalWave.entity.Availability;
import com.summerproject2026.DentalWave.entity.Employee;
import com.summerproject2026.DentalWave.entity.Office;
import com.summerproject2026.DentalWave.entity.User;
import com.summerproject2026.DentalWave.enums.WorkStatus;
import com.summerproject2026.DentalWave.exception.ResourceNotFoundException;
import com.summerproject2026.DentalWave.mapper.AvailabilityMapper;
import com.summerproject2026.DentalWave.mapper.EmployeeMapper;
import com.summerproject2026.DentalWave.repository.AvailabilityRepository;
import com.summerproject2026.DentalWave.repository.EmployeeRepository;
import com.summerproject2026.DentalWave.repository.OfficeRepository;
import com.summerproject2026.DentalWave.repository.UserRepository;
import com.summerproject2026.DentalWave.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of EmployeeService.
 *
 * Design notes:
 *  - Mappers produce STUB entities (id-only refs for User / Office).
 *    This impl fetches managed entities from repositories before persisting
 *    to prevent JPA detached/transient exceptions.
 *  - Availability is managed through the Employee aggregate root
 *    (addAvailability / removeAvailability) so that orphanRemoval works correctly.
 */
@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository     employeeRepository;
    private final UserRepository         userRepository;
    private final OfficeRepository       officeRepository;
    private final AvailabilityRepository availabilityRepository;
    private final EmployeeMapper         employeeMapper;
    private final AvailabilityMapper     availabilityMapper;

    @Autowired
    public EmployeeServiceImpl(EmployeeRepository employeeRepository,
                                UserRepository userRepository,
                                OfficeRepository officeRepository,
                                AvailabilityRepository availabilityRepository,
                                EmployeeMapper employeeMapper,
                                AvailabilityMapper availabilityMapper) {
        this.employeeRepository     = employeeRepository;
        this.userRepository         = userRepository;
        this.officeRepository       = officeRepository;
        this.availabilityRepository = availabilityRepository;
        this.employeeMapper         = employeeMapper;
        this.availabilityMapper     = availabilityMapper;
    }

    // ------------------------------------------------------------------ //
    // Create
    // ------------------------------------------------------------------ //

    /**
     * Creates a new Employee.
     * Fetches managed User and Office entities to replace the mapper's stubs.
     */
    @Override
    public EmployeeDto createEmployee(EmployeeDto employeeDto) {
        Employee employee = employeeMapper.mapToEmployee(employeeDto);

        // Hydrate User stub → managed entity
        if (employeeDto.getUserId() != null) {
            User managedUser = userRepository.findById(employeeDto.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "User not found with id: " + employeeDto.getUserId()));
            employee.setUser(managedUser);
        }

        // Hydrate Office stubs → managed entities
        employee.setOffices(resolveOffices(employeeDto));

        return employeeMapper.mapToEmployeeDto(employeeRepository.save(employee));
    }

    // ------------------------------------------------------------------ //
    // Read
    // ------------------------------------------------------------------ //

    @Override
    @Transactional(readOnly = true)
    public EmployeeDto getEmployeeById(Long id) {
        return employeeMapper.mapToEmployeeDto(findEmployeeOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeDto> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(employeeMapper::mapToEmployeeDto)
                .collect(Collectors.toList());
    }

    /** Returns all employees assigned to a specific office */
    @Override
    @Transactional(readOnly = true)
    public List<EmployeeDto> getEmployeesByOffice(Long officeId) {
        return employeeRepository.findByOfficeId(officeId).stream()
                .map(employeeMapper::mapToEmployeeDto)
                .collect(Collectors.toList());
    }

    /** Returns all employees with the given work status */
    @Override
    @Transactional(readOnly = true)
    public List<EmployeeDto> getEmployeesByStatus(WorkStatus status) {
        return employeeRepository.findByStatus(status).stream()
                .map(employeeMapper::mapToEmployeeDto)
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------------------ //
    // Update
    // ------------------------------------------------------------------ //

    /**
     * Updates an existing employee's fields.
     * The linked User account is intentionally NOT changed on update.
     * Office list is fully replaced with freshly resolved managed entities.
     */
    @Override
    public EmployeeDto updateEmployee(Long id, EmployeeDto employeeDto) {
        Employee existing = findEmployeeOrThrow(id);

        existing.setPosition(employeeDto.getPosition());
        existing.setHireDate(employeeDto.getHireDate());
        if (employeeDto.getTimeOff() != null) existing.setTimeOff(employeeDto.getTimeOff());
        if (employeeDto.getStatus()  != null) existing.setStatus(employeeDto.getStatus());
        if (employeeDto.getResponsibilities() != null) {
            existing.setResponsibilities(new ArrayList<>(employeeDto.getResponsibilities()));
        }

        // Fully replace office assignments
        existing.setOffices(resolveOffices(employeeDto));

        return employeeMapper.mapToEmployeeDto(employeeRepository.save(existing));
    }

    // ------------------------------------------------------------------ //
    // Delete
    // ------------------------------------------------------------------ //

    @Override
    public void deleteEmployee(Long id) {
        employeeRepository.delete(findEmployeeOrThrow(id));
    }

    // ------------------------------------------------------------------ //
    // Nested Availability management
    // ------------------------------------------------------------------ //

    /**
     * Adds a new availability record to an employee.
     * Uses Employee#addAvailability to wire the bidirectional reference so
     * JPA cascade/orphanRemoval works correctly.
     */
    @Override
    public AvailabilityDto addAvailability(Long employeeId, AvailabilityDto availabilityDto) {
        Employee employee = findEmployeeOrThrow(employeeId);

        Availability availability = availabilityMapper.mapToAvailability(availabilityDto);

        // Wire back-reference — sets availability.employee = employee
        employee.addAvailability(availability);

        // Saving the employee cascades the insert for the new availability row
        employeeRepository.save(employee);

        return availabilityMapper.mapToAvailabilityDto(availability);
    }

    /**
     * Updates an existing availability record, verifying ownership first.
     */
    @Override
    public AvailabilityDto updateAvailability(Long employeeId,
                                               Long availabilityId,
                                               AvailabilityDto availabilityDto) {
        // Verify employee exists
        findEmployeeOrThrow(employeeId);

        Availability existing = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Availability not found with id: " + availabilityId));

        // Ownership guard
        if (!existing.getEmployee().getId().equals(employeeId)) {
            throw new IllegalArgumentException(
                    "Availability " + availabilityId + " does not belong to employee " + employeeId);
        }

        // Update mutable fields — employee reference stays unchanged
        existing.setDayOfWeek(availabilityDto.getDayOfWeek());
        existing.setStartTime(availabilityDto.getStartTime());
        existing.setEndTime(availabilityDto.getEndTime());
        existing.setAvailable(availabilityDto.getAvailable());

        return availabilityMapper.mapToAvailabilityDto(availabilityRepository.save(existing));
    }

    /**
     * Removes an availability record from an employee.
     * Uses Employee#removeAvailability so orphanRemoval deletes the row.
     */
    @Override
    public void deleteAvailability(Long employeeId, Long availabilityId) {
        Employee employee = findEmployeeOrThrow(employeeId);

        Availability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Availability not found with id: " + availabilityId));

        // Ownership guard
        if (!availability.getEmployee().getId().equals(employeeId)) {
            throw new IllegalArgumentException(
                    "Availability " + availabilityId + " does not belong to employee " + employeeId);
        }

        // Clears the back-reference; orphanRemoval issues the DELETE
        employee.removeAvailability(availability);
        employeeRepository.save(employee);
    }

    // ------------------------------------------------------------------ //
    // Private helpers
    // ------------------------------------------------------------------ //

    private Employee findEmployeeOrThrow(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found with id: " + id));
    }

    /**
     * Resolves OfficeDto list from the DTO to managed Office entities.
     * Throws ResourceNotFoundException for any ID that doesn't exist in the DB.
     */
    private List<Office> resolveOffices(EmployeeDto dto) {
        if (dto.getOffices() == null || dto.getOffices().isEmpty()) return new ArrayList<>();
        return dto.getOffices().stream()
                .map(officeDto -> officeRepository.findById(officeDto.getId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Office not found with id: " + officeDto.getId())))
                .collect(Collectors.toList());
    }
}
