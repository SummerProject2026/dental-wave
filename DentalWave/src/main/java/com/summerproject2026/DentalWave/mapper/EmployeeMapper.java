package com.summerproject2026.DentalWave.mapper;

import com.summerproject2026.DentalWave.dto.AvailabilityDto;
import com.summerproject2026.DentalWave.dto.EmployeeDto;
import com.summerproject2026.DentalWave.dto.OfficeDto;
import com.summerproject2026.DentalWave.entity.Employee;
import com.summerproject2026.DentalWave.entity.Office;
import com.summerproject2026.DentalWave.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Converts an Employee entity into an EmployeeDto.
 *
 * <p>This method flattens selected fields from the associated
 * {@link User} entity so the frontend does not need to navigate
 * nested objects to access common employee information.</p>
 *
 * <p>Mapped User fields:
 * <ul>
 *     <li>id → userId</li>
 *     <li>firstName → firstName</li>
 *     <li>lastName → lastName</li>
 *     <li>username → username</li>
 *     <li>email → email</li>
 * </ul>
 * </p>
 *
 * <p>Also maps:
 * <ul>
 *     <li>Office entities → OfficeDto list</li>
 *     <li>Availability entities → AvailabilityDto list</li>
 *     <li>Responsibilities collection</li>
 * </ul>
 * </p>
 *
 * @param employee Employee entity to convert
 * @return populated EmployeeDto, or null if employee is null
 */
@Component
public class EmployeeMapper {

    private final AvailabilityMapper availabilityMapper;

    @Autowired
    public EmployeeMapper(AvailabilityMapper availabilityMapper) {
        this.availabilityMapper = availabilityMapper;
    }

    /**
     * Converts between Employee entities and EmployeeDto objects.
     *
     * Key mapping decisions:
     *
     * User
     *  - userId
     *  - firstName
     *  - lastName
     *  - username
     *  - email
     *
     * Office
     *  - Office → OfficeDto
     *
     * Availability
     *  - Availability → AvailabilityDto
     *
     * The mapper intentionally flattens User information into the
     * EmployeeDto so the frontend can easily display employee
     * information without traversing nested objects.
     */
    public EmployeeDto mapToEmployeeDto(Employee employee) {
        if (employee == null) return null;

        // Flatten User fields into scalar DTO values
        Long userId = null;
        String firstName = null;
        String lastName = null;
        String username = null;
        String email = null;

        if (employee.getUser() != null) {
            // Copy User information into EmployeeDto fields
            User user = employee.getUser();
            userId = user.getId();
            firstName = user.getFirstName();
            lastName = user.getLastName();
            username = user.getUsername();
            email = user.getEmail();
        }

        List<OfficeDto> officeDtos = employee.getOffices() == null
                ? new ArrayList<>()
                : employee.getOffices().stream()
                .map(this::toOfficeDto)
                .collect(Collectors.toList());

        List<AvailabilityDto> availabilityDtos = employee.getAvailabilities() == null
                ? new ArrayList<>()
                : employee.getAvailabilities().stream()
                .map(availabilityMapper::mapToAvailabilityDto)
                .collect(Collectors.toList());

        return new EmployeeDto(
                employee.getId(),
                userId,
                firstName,
                lastName,
                username,
                email,
                employee.getPosition(),
                officeDtos,
                employee.getResponsibilities() != null
                        ? new ArrayList<>(employee.getResponsibilities())
                        : new ArrayList<>(),
                employee.getHireDate(),
                employee.getTimeOff(),
                employee.getStatus(),
                availabilityDtos
        );
    }

    /**
     * Converts an EmployeeDto into an Employee entity.
     *
     * <p>For related entities (User and Office), only ID values
     * are mapped. These become temporary stub objects that must
     * later be replaced with managed JPA entities by the service
     * layer before persistence.</p>
     *
     * <p>This prevents detached entity issues and ensures all
     * relationships are attached to the current persistence
     * context before saving.</p>
     *
     * @param dto EmployeeDto to convert
     * @return populated Employee entity, or null if dto is null
     */
    public Employee mapToEmployee(EmployeeDto dto) {
        if (dto == null) return null;

        Employee employee = new Employee();
        employee.setId(dto.getId());
        employee.setPosition(dto.getPosition());
        employee.setHireDate(dto.getHireDate());
        employee.setTimeOff(dto.getTimeOff() != null ? dto.getTimeOff() : 0.0);
        employee.setStatus(dto.getStatus());
        employee.setResponsibilities(
                dto.getResponsibilities() != null
                        ? new ArrayList<>(dto.getResponsibilities())
                        : new ArrayList<>()
        );

        if (dto.getUserId() != null) {
            User userStub = new User();
            userStub.setId(dto.getUserId());
            employee.setUser(userStub);
        }

        if (dto.getOffices() != null) {
            List<Office> officeStubs = dto.getOffices().stream()
                    .map(officeDto -> {
                        Office office = new Office();
                        office.setId(officeDto.getId());
                        return office;
                    })
                    .collect(Collectors.toList());

            employee.setOffices(officeStubs);
        }

        if (dto.getAvailabilities() != null) {
            dto.getAvailabilities().stream()
                    .map(availabilityMapper::mapToAvailability)
                    .forEach(employee::addAvailability);
        }

        return employee;
    }

    private OfficeDto toOfficeDto(Office office) {
        if (office == null) return null;

        return new OfficeDto(
                office.getId(),
                office.getName(),
                office.getAddress(),
                office.getPhoneNumber()
        );
    }
}