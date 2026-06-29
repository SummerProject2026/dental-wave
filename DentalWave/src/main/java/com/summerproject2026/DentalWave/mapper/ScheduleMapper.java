package com.summerproject2026.DentalWave.mapper;

import com.summerproject2026.DentalWave.dto.EmployeeDto;
import com.summerproject2026.DentalWave.dto.ScheduleDto;
import com.summerproject2026.DentalWave.entity.Schedule;
import com.summerproject2026.DentalWave.entity.ScheduleTeam;
import com.summerproject2026.DentalWave.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Maps between Schedule entities and ScheduleDto objects.
 *
 * Teams are flattened into a Map keyed by the real ScheduleTeam id
 * to a list of EmployeeDtos, so the frontend can address a specific
 * team directly when adding or removing employees from it.
 */
@Component
public class ScheduleMapper {

    private final EmployeeMapper employeeMapper;

    @Autowired
    public ScheduleMapper(EmployeeMapper employeeMapper) {
        this.employeeMapper = employeeMapper;
    }

    // -------------------------------------------------------------------------
    // Entity → DTO
    // -------------------------------------------------------------------------

    /**
     * Converts a Schedule entity to a ScheduleDto.
     * Flattens the createdBy User into a scalar id field, and the
     * teams collection into a map keyed by the real team id.
     *
     * @param schedule the entity to convert
     * @return the corresponding DTO, or null if input is null
     */
    public ScheduleDto mapToScheduleDto(Schedule schedule) {
        if (schedule == null) return null;

        ScheduleDto dto = new ScheduleDto();
        dto.setId(schedule.getId());
        dto.setDate(schedule.getDate());
        dto.setStartTime(schedule.getStartTime());
        dto.setEndTime(schedule.getEndTime());
        dto.setNotes(schedule.getNotes());
        dto.setStartScheduleDate(schedule.getStartScheduleDate());
        dto.setEndScheduleDate(schedule.getEndScheduleDate());
        dto.setPublished(schedule.getPublished());

        if (schedule.getCreatedBy() != null) {
            dto.setCreatedById(schedule.getCreatedBy().getId());
        }

        if (schedule.getCalendar() != null) {
            dto.setCalendarId(schedule.getCalendar().getId());
        }

        // Flatten teams into a map keyed by the real ScheduleTeam id,
        // so the frontend can call assignEmployeeToTeam/removeEmployeeFromTeam
        // using a real, addressable team identifier
        if (schedule.getTeams() != null) {
            Map<Long, List<EmployeeDto>> teamsMap = new LinkedHashMap<>();
            for (ScheduleTeam team : schedule.getTeams()) {
                List<EmployeeDto> employeeDtos = team.getEmployees().stream()
                        .map(employeeMapper::mapToEmployeeDto)
                        .collect(Collectors.toList());
                teamsMap.put(team.getId(), employeeDtos);
            }
            dto.setTeams(teamsMap);
        }

        return dto;
    }

    // -------------------------------------------------------------------------
    // DTO → Entity
    // -------------------------------------------------------------------------

    /**
     * Converts a ScheduleDto to a Schedule entity.
     * The createdBy User is only partially hydrated (id only).
     * The service layer must fetch the full User before persisting.
     * Team assignments are not reconstructed here — use the dedicated
     * team assignment endpoints/service methods for that.
     *
     * @param scheduleDto the DTO to convert
     * @return the corresponding entity, or null if input is null
     */
    public Schedule mapToSchedule(ScheduleDto scheduleDto) {
        if (scheduleDto == null) return null;

        Schedule schedule = new Schedule();
        schedule.setId(scheduleDto.getId());
        schedule.setDate(scheduleDto.getDate());
        schedule.setStartTime(scheduleDto.getStartTime());
        schedule.setEndTime(scheduleDto.getEndTime());
        schedule.setNotes(scheduleDto.getNotes());
        schedule.setStartScheduleDate(scheduleDto.getStartScheduleDate());
        schedule.setEndScheduleDate(scheduleDto.getEndScheduleDate());
        schedule.setPublished(scheduleDto.getPublished() != null
                ? scheduleDto.getPublished() : false);

        if (scheduleDto.getCreatedById() != null) {
            User createdBy = new User();
            createdBy.setId(scheduleDto.getCreatedById());
            schedule.setCreatedBy(createdBy);
        }

        return schedule;
    }
}