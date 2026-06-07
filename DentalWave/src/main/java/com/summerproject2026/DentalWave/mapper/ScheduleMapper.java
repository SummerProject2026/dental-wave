package com.dentalwave.mapper;

import com.dentalwave.dto.EmployeeDto;
import com.dentalwave.dto.ScheduleDto;
import com.dentalwave.model.Employee;
import com.dentalwave.model.Schedule;
import com.dentalwave.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Maps between Schedule entities and ScheduleDto objects.
 * Handles the Map<User, List<Employee>> ↔ Map<Long, List<EmployeeDto>>
 * conversion for team assignments.
 */
@Component
public class ScheduleMapper {

    private final AvailabilityMapper availabilityMapper;

    @Autowired
    public ScheduleMapper(AvailabilityMapper availabilityMapper) {
        this.availabilityMapper = availabilityMapper;
    }

    // -------------------------------------------------------------------------
    // Entity → DTO
    // -------------------------------------------------------------------------

    /**
     * Converts a Schedule entity to a ScheduleDto.
     * Translates the team map keys from User entities to user IDs (Long),
     * and maps each Employee list to a list of EmployeeDto.
     *
     * @param schedule the entity to convert
     * @return the corresponding DTO, or null if input is null
     */
    public ScheduleDto mapToScheduleDto(Schedule schedule) {
        if (schedule == null) return null;

        // Convert Map<User, List<Employee>> → Map<Long, List<EmployeeDto>>
        Map<Long, List<EmployeeDto>> teamDtos = new HashMap<>();
        if (schedule.getTeams() != null) {
            for (Map.Entry<User, List<Employee>> entry : schedule.getTeams().entrySet()) {
                Long userId = entry.getKey().getId();
                List<EmployeeDto> empDtos = entry.getValue() == null
                        ? new ArrayList<>()
                        : entry.getValue().stream()
                               .map(this::toEmployeeDto)
                               .collect(Collectors.toList());
                teamDtos.put(userId, empDtos);
            }
        }

        // Resolve parent calendar id safely
        Long calendarId = schedule.getCalendar() != null ? schedule.getCalendar().getId() : null;

        return new ScheduleDto(
                schedule.getId(),
                schedule.getDate(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                calendarId,
                teamDtos,
                schedule.getNotes()
        );
    }

    // -------------------------------------------------------------------------
    // DTO → Entity
    // -------------------------------------------------------------------------

    /**
     * Converts a ScheduleDto to a Schedule entity.
     * Team keys (userIds) are converted to stub User objects; the service
     * layer must re-hydrate them from the database before persisting.
     *
     * @param scheduleDto the DTO to convert
     * @return the corresponding entity, or null if input is null
     */
    public Schedule mapToSchedule(ScheduleDto scheduleDto) {
        if (scheduleDto == null) return null;

        // Convert Map<Long, List<EmployeeDto>> → Map<User, List<Employee>>
        Map<User, List<Employee>> teams = new HashMap<>();
        if (scheduleDto.getTeams() != null) {
            for (Map.Entry<Long, List<EmployeeDto>> entry : scheduleDto.getTeams().entrySet()) {
                // Stub User — service must replace with managed entity
                User userStub = new User();
                userStub.setId(entry.getKey());

                List<Employee> employees = entry.getValue() == null
                        ? new ArrayList<>()
                        : entry.getValue().stream()
                               .map(this::toEmployee)
                               .collect(Collectors.toList());

                teams.put(userStub, employees);
            }
        }

        return new Schedule(
                scheduleDto.getId(),
                scheduleDto.getDate(),
                scheduleDto.getStartTime(),
                scheduleDto.getEndTime(),
                teams,
                scheduleDto.getNotes()
        );
        // Note: calendar reference is set by CalendarMapper or the service layer
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /** Converts an Employee entity to an EmployeeDto */
    private EmployeeDto toEmployeeDto(Employee employee) {
        if (employee == null) return null;
        return new EmployeeDto(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getRole()
        );
    }

    /** Converts an EmployeeDto to a stub Employee entity */
    private Employee toEmployee(EmployeeDto dto) {
        if (dto == null) return null;
        Employee emp = new Employee();
        emp.setId(dto.getId());
        emp.setFirstName(dto.getFirstName());
        emp.setLastName(dto.getLastName());
        emp.setRole(dto.getRole());
        return emp;
    }
}
