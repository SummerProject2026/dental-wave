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
 * Converts between Employee entities and EmployeeDto objects.
 *
 * The mapper flattens selected User fields into EmployeeDto
 * so the frontend can display and edit employee information
 * without needing to navigate nested User objects.
 */
@Component
public class EmployeeMapper {

    private final AvailabilityMapper availabilityMapper;

    @Autowired
    public EmployeeMapper(AvailabilityMapper availabilityMapper) {
        this.availabilityMapper = availabilityMapper;
    }

    /**
     * Converts an Employee entity into an EmployeeDto.
     *
     * User fields mapped:
     * - id -> userId
     * - firstName -> firstName
     * - lastName -> lastName
     * - username -> username
     * - phoneNumber -> phoneNumber
     * - email -> email
     *
     * @param employee Employee entity to convert
     * @return populated EmployeeDto, or null if employee is null
     */
    public EmployeeDto mapToEmployeeDto(Employee employee) {
        if (employee == null) return null;

        Long userId = null;
        String firstName = null;
        String lastName = null;
        String username = null;
        String phoneNumber = null;
        String email = null;

        if (employee.getUser() != null) {
            User user = employee.getUser();

            userId = user.getId();
            firstName = user.getFirstName();
            lastName = user.getLastName();
            username = user.getUsername();
            phoneNumber = user.getPhoneNumber();
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
                phoneNumber,
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
     * For related entities like User and Office, this mapper only creates
     * temporary ID-based stub objects. The service layer should replace
     * them with managed JPA entities before saving.
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
            userStub.setFirstName(dto.getFirstName());
            userStub.setLastName(dto.getLastName());
            userStub.setUsername(dto.getUsername());
            userStub.setPhoneNumber(dto.getPhoneNumber());
            userStub.setEmail(dto.getEmail());

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

    /**
     * Converts an Office entity into an OfficeDto.
     *
     * @param office Office entity to convert
     * @return populated OfficeDto, or null if office is null
     */
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