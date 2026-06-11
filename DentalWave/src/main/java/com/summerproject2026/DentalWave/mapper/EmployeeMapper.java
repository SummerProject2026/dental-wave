package com.summerproject2026.DentalWave.mapper;

import com.summerproject2026.DentalWave.dto.AvailabilityDto;
import com.summerproject2026.DentalWave.dto.EmployeeDto;
import com.summerproject2026.DentalWave.dto.OfficeDto;
import com.summerproject2026.DentalWave.entity.Availability;
import com.summerproject2026.DentalWave.entity.Employee;
import com.summerproject2026.DentalWave.entity.Office;
import com.summerproject2026.DentalWave.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Converts between Employee entities and EmployeeDto objects.
 *
 * Key mapping decisions:
 *  - User          → userId / firstName / lastName / email  (flattened)
 *  - Office        → OfficeDto  (via private helper)
 *  - Availability  → AvailabilityDto  (delegated to AvailabilityMapper)
 */
@Component
public class EmployeeMapper {

    private final AvailabilityMapper availabilityMapper;

    @Autowired
    public EmployeeMapper(AvailabilityMapper availabilityMapper) {
        this.availabilityMapper = availabilityMapper;
    }

    // ------------------------------------------------------------------ //
    // Entity → DTO
    // ------------------------------------------------------------------ //

    /**
     * Converts an Employee entity to an EmployeeDto.
     * Flattens the User reference and maps nested Office + Availability lists.
     */
    public EmployeeDto mapToEmployeeDto(Employee employee) {
        if (employee == null) return null;

        // Flatten User fields
        Long userId = null;
        String firstName = null;
        String lastName = null;
        String email = null;

        if (employee.getUser() != null) {
            User user = employee.getUser();
            userId    = user.getId();
            firstName = user.getFirstName();
            lastName  = user.getLastName();
            email     = user.getEmail();
        }

        // Map Office list
        List<OfficeDto> officeDtos = employee.getOffices() == null
                ? new ArrayList<>()
                : employee.getOffices().stream()
                          .map(this::toOfficeDto)
                          .collect(Collectors.toList());

        // Map Availability list
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

    // ------------------------------------------------------------------ //
    // DTO → Entity
    // ------------------------------------------------------------------ //

    /**
     * Converts an EmployeeDto to an Employee entity.
     *
     * NOTE: User and Office references are STUBS containing only the ID.
     * The service layer must fetch managed entities from the repository
     * before calling save() to avoid JPA detached-entity exceptions.
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

        // Stub User — service must hydrate with managed entity
        if (dto.getUserId() != null) {
            User userStub = new User();
            userStub.setId(dto.getUserId());
            employee.setUser(userStub);
        }

        // Stub Offices — service must hydrate with managed entities
        if (dto.getOffices() != null) {
            List<Office> officeStubs = dto.getOffices().stream()
                    .map(officeDto -> {
                        Office o = new Office();
                        o.setId(officeDto.getId());
                        return o;
                    })
                    .collect(Collectors.toList());
            employee.setOffices(officeStubs);
        }

        // Map availabilities and wire back-reference
        if (dto.getAvailabilities() != null) {
            dto.getAvailabilities().stream()
               .map(availabilityMapper::mapToAvailability)
               .forEach(employee::addAvailability);
        }

        return employee;
    }

    // ------------------------------------------------------------------ //
    // Private helpers
    // ------------------------------------------------------------------ //

    private OfficeDto toOfficeDto(Office office) {
        if (office == null) return null;
        return new OfficeDto(office.getId(), office.getName(), office.getAddress(), office.getPhoneNumber());
    }
}
