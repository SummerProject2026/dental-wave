package com.summerproject2026.DentalWave.service.impl;

import com.summerproject2026.DentalWave.dto.AvailabilityDto;
import com.summerproject2026.DentalWave.dto.EmployeeDto;
import com.summerproject2026.DentalWave.entity.Availability;
import com.summerproject2026.DentalWave.entity.Employee;
import com.summerproject2026.DentalWave.entity.Office;
import com.summerproject2026.DentalWave.entity.ScheduleTeam;
import com.summerproject2026.DentalWave.entity.User;
import com.summerproject2026.DentalWave.enums.WorkStatus;
import com.summerproject2026.DentalWave.exception.ResourceNotFoundException;
import com.summerproject2026.DentalWave.mapper.AvailabilityMapper;
import com.summerproject2026.DentalWave.mapper.EmployeeMapper;
import com.summerproject2026.DentalWave.repository.AvailabilityRepository;
import com.summerproject2026.DentalWave.repository.EmployeeRepository;
import com.summerproject2026.DentalWave.repository.OfficeRepository;
import com.summerproject2026.DentalWave.repository.ScheduleTeamRepository;
import com.summerproject2026.DentalWave.repository.UserRepository;
import com.summerproject2026.DentalWave.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.summerproject2026.DentalWave.dto.CreateEmployeeDto;
import com.summerproject2026.DentalWave.dto.RegisterDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.summerproject2026.DentalWave.entity.Role;
import com.summerproject2026.DentalWave.repository.RoleRepository;
import java.util.Set;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of EmployeeService.
 *
 * Design notes:
 *    This impl fetches managed entities from repositories before persisting
 *    to prevent JPA detached/transient exceptions.
 *  - Availability is managed through the Employee aggregate root
 *    (addAvailability / removeAvailability) so that orphanRemoval works correctly.
 *  - When an employee's status changes to INACTIVE, they are automatically
 *    removed from every team they currently belong to across all schedules,
 *    so deactivated employees no longer appear on any upcoming schedule.
 */
@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository     employeeRepository;
    private final UserRepository         userRepository;
    private final OfficeRepository       officeRepository;
    private final AvailabilityRepository availabilityRepository;
    private final ScheduleTeamRepository scheduleTeamRepository;
    private final EmployeeMapper         employeeMapper;
    private final AvailabilityMapper     availabilityMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Autowired
    public EmployeeServiceImpl(EmployeeRepository employeeRepository,
                               UserRepository userRepository,
                               OfficeRepository officeRepository,
                               AvailabilityRepository availabilityRepository,
                               ScheduleTeamRepository scheduleTeamRepository,
                               EmployeeMapper employeeMapper,
                               AvailabilityMapper availabilityMapper,
                               PasswordEncoder passwordEncoder,
                               RoleRepository roleRepository) {
        this.employeeRepository     = employeeRepository;
        this.userRepository         = userRepository;
        this.officeRepository       = officeRepository;
        this.availabilityRepository = availabilityRepository;
        this.scheduleTeamRepository = scheduleTeamRepository;
        this.employeeMapper         = employeeMapper;
        this.availabilityMapper     = availabilityMapper;
        this.passwordEncoder        = passwordEncoder;
        this.roleRepository         = roleRepository;
    }

    // ------------------------------------------------------------------ //
    // Create
    // ------------------------------------------------------------------ //

    /**
     * Creates a new Employee.
     * Fetches managed User and Office entities to replace the mapper's stubs.
     */
    @Override
    @Transactional
    public EmployeeDto createEmployee(CreateEmployeeDto createEmployeeDto) {
        RegisterDto userDto = createEmployeeDto.getUser();
        EmployeeDto employeeDto = createEmployeeDto.getEmployee();

        // 1. Create the User first
        User user = new User();
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPhoneNumber(userDto.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setEnabled(true);

        // Assign the security role to the new User.
        // This creates the matching row in the user_roles join table.
        String roleName = createEmployeeDto.getRole();

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role not found: " + roleName));

        user.setRoles(Set.of(role));

        User savedUser = userRepository.save(user);

        // 2. Create the Employee and link it to the saved User
        Employee employee = employeeMapper.mapToEmployee(employeeDto);
        employee.setUser(savedUser);

        // 3. Hydrate Office stubs into real Office entities
        employee.setOffices(resolveOffices(employeeDto));

        // 4. Save Employee
        Employee savedEmployee = employeeRepository.save(employee);

        // 5. Return DTO
        return employeeMapper.mapToEmployeeDto(savedEmployee);
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
     *
     * <p>If the status transitions to INACTIVE, the employee is automatically
     * removed from every team they belong to across all schedules.</p>
     */
    @Override
    public EmployeeDto updateEmployee(Long id, EmployeeDto employeeDto) {
        Employee existing = findEmployeeOrThrow(id);

        WorkStatus previousStatus = existing.getStatus();

        User user = existing.getUser();

        if (user != null) {
            user.setFirstName(employeeDto.getFirstName());
            user.setLastName(employeeDto.getLastName());
            user.setUsername(employeeDto.getUsername());
            user.setEmail(employeeDto.getEmail());
            user.setPhoneNumber(employeeDto.getPhoneNumber());

            userRepository.save(user);
        }

        existing.setPosition(employeeDto.getPosition());
        existing.setHireDate(employeeDto.getHireDate());

        if (employeeDto.getTimeOff() != null) {
            existing.setTimeOff(employeeDto.getTimeOff());
        }

        if (employeeDto.getStatus() != null) {
            existing.setStatus(employeeDto.getStatus());
        }

        if (employeeDto.getResponsibilities() != null) {
            existing.setResponsibilities(new ArrayList<>(employeeDto.getResponsibilities()));
        }

        existing.setOffices(resolveOffices(employeeDto));

        Employee savedEmployee = employeeRepository.save(existing);

        // If the employee just became INACTIVE, remove them from every
        // team they currently belong to across all schedules
        boolean justDeactivated = previousStatus != WorkStatus.INACTIVE
                && savedEmployee.getStatus() == WorkStatus.INACTIVE;

        if (justDeactivated) {
            removeEmployeeFromAllTeams(savedEmployee.getId());
        }

        return employeeMapper.mapToEmployeeDto(savedEmployee);
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

    /**
     * Removes the given employee from every ScheduleTeam they currently
     * belong to, across all schedules and calendars. Called automatically
     * when an employee's status transitions to INACTIVE.
     *
     * @param employeeId the employee to remove from all team assignments
     */
    private void removeEmployeeFromAllTeams(Long employeeId) {
        List<ScheduleTeam> allTeams = scheduleTeamRepository.findAll();

        for (ScheduleTeam team : allTeams) {
            boolean removed = team.getEmployees()
                    .removeIf(e -> e.getId().equals(employeeId));
            if (removed) {
                scheduleTeamRepository.save(team);
            }
        }
    }
}