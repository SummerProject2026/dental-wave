
package com.summerproject2026.DentalWave.mapper;

import com.summerproject2026.DentalWave.dto.CalendarDto;
import com.summerproject2026.DentalWave.dto.ScheduleDto;
import com.summerproject2026.DentalWave.entity.Calendar;
import com.summerproject2026.DentalWave.entity.Office;
import com.summerproject2026.DentalWave.entity.Schedule;
import com.summerproject2026.DentalWave.entity.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Maps between Calendar entities and CalendarDto objects.
 * Delegates schedule mapping to ScheduleMapper to avoid duplication.
 */
@Component
public class CalendarMapper {

    private final ScheduleMapper scheduleMapper;

    @Autowired
    public CalendarMapper(ScheduleMapper scheduleMapper) {
        this.scheduleMapper = scheduleMapper;
    }

    // -------------------------------------------------------------------------
    // Entity → DTO
    // -------------------------------------------------------------------------

    /**
     * Converts a Calendar entity to a CalendarDto.
     * Flattens the createdBy User and office into id and name fields.
     * Recursively maps all nested schedules.
     *
     * @param calendar the entity to convert
     * @return the corresponding DTO, or null if input is null
     */
    public CalendarDto mapToCalendarDto(Calendar calendar) {
        if (calendar == null) return null;

        // Flatten the User reference into two scalar fields
        Long createdById = null;
        String createdByName = null;
        if (calendar.getCreatedBy() != null) {
            User creator = calendar.getCreatedBy();
            createdById = creator.getId();
            createdByName = creator.getFirstName() + " " + creator.getLastName();
        }

        // Flatten the Office reference into two scalar fields
        Long officeId = null;
        String officeName = null;
        if (calendar.getOffice() != null) {
            Office office = calendar.getOffice();
            officeId = office.getId();
            officeName = office.getName();
        }

        // Map nested schedules
        List<ScheduleDto> scheduleDtos = calendar.getSchedules() == null
                ? new ArrayList<>()
                : calendar.getSchedules().stream()
                .map(scheduleMapper::mapToScheduleDto)
                .collect(Collectors.toList());

        return new CalendarDto(
                calendar.getId(),
                calendar.getMonth(),
                calendar.getStartCalendarDate(),
                calendar.getEndCalendarDate(),
                calendar.getPublished(),
                createdById,
                createdByName,
                officeId,
                officeName,
                scheduleDtos
        );
    }

    // -------------------------------------------------------------------------
    // DTO → Entity
    // -------------------------------------------------------------------------

    /**
     * Converts a CalendarDto back into a Calendar entity.
     * NOTE: The createdBy User and Office are only partially hydrated (id only).
     * The service layer must fetch and set the full entities from the repository
     * before persisting to avoid detached-entity issues.
     *
     * @param calendarDto the DTO to convert
     * @return the corresponding entity, or null if input is null
     */
    public Calendar mapToCalendar(CalendarDto calendarDto) {
        if (calendarDto == null) return null;

        Calendar calendar = new Calendar();
        calendar.setId(calendarDto.getId());
        calendar.setMonth(calendarDto.getMonth());
        calendar.setStartCalendarDate(calendarDto.getStartCalendarDate());
        calendar.setEndCalendarDate(calendarDto.getEndCalendarDate());
        calendar.setPublished(calendarDto.getPublished() != null ? calendarDto.getPublished() : false);

        // Stub User — service must replace this with a managed entity
        if (calendarDto.getCreatedById() != null) {
            User createdBy = new User();
            createdBy.setId(calendarDto.getCreatedById());
            calendar.setCreatedBy(createdBy);
        }

        // Stub Office — service must replace this with a managed entity
        if (calendarDto.getOfficeId() != null) {
            Office office = new Office();
            office.setId(calendarDto.getOfficeId());
            calendar.setOffice(office);
        }

        // Map nested schedule DTOs
        if (calendarDto.getSchedules() != null) {
            List<Schedule> schedules = calendarDto.getSchedules().stream()
                    .map(scheduleMapper::mapToSchedule)
                    .collect(Collectors.toList());
            schedules.forEach(s -> s.setCalendar(calendar));
            calendar.setSchedules(schedules);
        }

        return calendar;
    }
}