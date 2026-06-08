package com.summerproject2026.DentalWave.mapper;
import com.summerproject2026.DentalWave.dto.ScheduleDto;
import com.summerproject2026.DentalWave.entity.Schedule;
import com.summerproject2026.DentalWave.entity.User;
import org.springframework.stereotype.Component;

/**
 * Maps between Schedule entities and ScheduleDto objects.
 */
@Component
public class ScheduleMapper {

    // -------------------------------------------------------------------------
    // Entity → DTO
    // -------------------------------------------------------------------------

    /**
     * Converts a Schedule entity to a ScheduleDto.
     *
     * @param schedule the entity to convert
     * @return the corresponding DTO, or null if input is null
     */
    public ScheduleDto mapToScheduleDto(Schedule schedule) {
        if (schedule == null) return null;

        // Safely extract createdBy user id
        Long createdById = schedule.getCreatedBy() != null
                ? schedule.getCreatedBy().getId()
                : null;

        return new ScheduleDto(
                schedule.getId(),
                schedule.getStartScheduleDate(),
                schedule.getEndScheduleDate(),
                schedule.getPublished(),
                createdById
        );
    }

    // -------------------------------------------------------------------------
    // DTO → Entity
    // -------------------------------------------------------------------------

    /**
     * Converts a ScheduleDto to a Schedule entity.
     * The createdBy User is only partially hydrated (id only).
     * The service layer must fetch the full User before persisting.
     *
     * @param scheduleDto the DTO to convert
     * @return the corresponding entity, or null if input is null
     */
    public Schedule mapToSchedule(ScheduleDto scheduleDto) {
        if (scheduleDto == null) return null;

        Schedule schedule = new Schedule();
        schedule.setId(scheduleDto.getId());
        schedule.setStartScheduleDate(scheduleDto.getStartScheduleDate());
        schedule.setEndScheduleDate(scheduleDto.getEndScheduleDate());
        schedule.setPublished(scheduleDto.getPublished() != null
                ? scheduleDto.getPublished() : false);

        // Stub User — service must replace with managed entity
        if (scheduleDto.getCreatedById() != null) {
            User createdBy = new User();
            createdBy.setId(scheduleDto.getCreatedById());
            schedule.setCreatedBy(createdBy);
        }

        return schedule;
    }
}