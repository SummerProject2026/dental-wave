package com.summerproject2026.DentalWave.mapper;

import com.summerproject2026.DentalWave.dto.EmployeeDto;
import com.summerproject2026.DentalWave.dto.ScheduleTeamDto;
import com.summerproject2026.DentalWave.entity.Employee;
import com.summerproject2026.DentalWave.entity.Schedule;
import com.summerproject2026.DentalWave.entity.ScheduleTeam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Maps between ScheduleTeam entities and ScheduleTeamDto objects.
 */
@Component
public class ScheduleTeamMapper {

    private final EmployeeMapper employeeMapper;

    @Autowired
    public ScheduleTeamMapper(EmployeeMapper employeeMapper) {
        this.employeeMapper = employeeMapper;
    }

    // -------------------------------------------------------------------------
    // Entity → DTO
    // -------------------------------------------------------------------------

    /**
     * Converts a ScheduleTeam entity to a ScheduleTeamDto.
     *
     * @param scheduleTeam the entity to convert
     * @return the corresponding DTO or null if input is null
     */
    public ScheduleTeamDto mapToScheduleTeamDto(ScheduleTeam scheduleTeam) {
        if (scheduleTeam == null) return null;

        // Safely extract schedule id
        Long scheduleId = scheduleTeam.getSchedule() != null
                ? scheduleTeam.getSchedule().getId()
                : null;

        // Map employees
        List<EmployeeDto> employeeDtos = scheduleTeam.getEmployees() == null
                ? new ArrayList<>()
                : scheduleTeam.getEmployees().stream()
                .map(employeeMapper::mapToEmployeeDto)
                .collect(Collectors.toList());

        return new ScheduleTeamDto(
                scheduleTeam.getId(),
                scheduleTeam.getName(),
                scheduleId,
                employeeDtos
        );
    }

    // -------------------------------------------------------------------------
    // DTO → Entity
    // -------------------------------------------------------------------------

    /**
     * Converts a ScheduleTeamDto to a ScheduleTeam entity.
     * Schedule reference is only partially hydrated (id only).
     * Service layer must fetch the full Schedule before persisting.
     *
     * @param scheduleTeamDto the DTO to convert
     * @return the corresponding entity or null if input is null
     */
    public ScheduleTeam mapToScheduleTeam(ScheduleTeamDto scheduleTeamDto) {
        if (scheduleTeamDto == null) return null;

        ScheduleTeam scheduleTeam = new ScheduleTeam();
        scheduleTeam.setId(scheduleTeamDto.getId());
        scheduleTeam.setName(scheduleTeamDto.getName());

        // Stub Schedule — service must replace with managed entity
        if (scheduleTeamDto.getScheduleId() != null) {
            Schedule schedule = new Schedule();
            schedule.setId(scheduleTeamDto.getScheduleId());
            scheduleTeam.setSchedule(schedule);
        }

        // Map employee DTOs to stub entities
        if (scheduleTeamDto.getEmployees() != null) {
            List<Employee> employees = scheduleTeamDto.getEmployees().stream()
                    .map(employeeMapper::mapToEmployee)
                    .collect(Collectors.toList());
            scheduleTeam.setEmployees(employees);
        }

        return scheduleTeam;
    }
}