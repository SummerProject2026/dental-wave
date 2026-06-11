package com.summerproject2026.DentalWave.mapper;

import com.summerproject2026.DentalWave.dto.AvailabilityDto;
import com.summerproject2026.DentalWave.entity.Availability;
import com.summerproject2026.DentalWave.entity.Employee;
import org.springframework.stereotype.Component;

/**
 * Maps between Availability entities and AvailabilityDto objects.
 * Converts the Employee reference to/from a plain employeeId Long.
 */
@Component
public class AvailabilityMapper {

    // -------------------------------------------------------------------------
    // Entity → DTO
    // -------------------------------------------------------------------------

    /**
     * Converts an Availability entity to an AvailabilityDto.
     * Extracts the employee's ID from the associated Employee entity.
     *
     * @param availability the entity to convert
     * @return the corresponding DTO, or null if input is null
     */
    public AvailabilityDto mapToAvailabilityDto(Availability availability) {
        if (availability == null) return null;

        Long employeeId = availability.getEmployee() != null
                ? availability.getEmployee().getId()
                : null;

        return new AvailabilityDto(
                availability.getId(),
                employeeId,
                availability.getDayOfWeek(),
                availability.getStartTime(),
                availability.getEndTime(),
                availability.getAvailable()
        );
    }

    // -------------------------------------------------------------------------
    // DTO → Entity
    // -------------------------------------------------------------------------

    /**
     * Converts an AvailabilityDto to an Availability entity.
     * Creates a stub Employee containing only the ID; the service layer
     * must fetch and replace it with a managed Employee before persisting.
     *
     * @param availabilityDto the DTO to convert
     * @return the corresponding entity, or null if input is null
     */
    public Availability mapToAvailability(AvailabilityDto availabilityDto) {
        if (availabilityDto == null) return null;

        // Stub employee – service must hydrate with a real managed entity
        Employee employeeStub = null;
        if (availabilityDto.getEmployeeId() != null) {
            employeeStub = new Employee();
            employeeStub.setId(availabilityDto.getEmployeeId());
        }

        return new Availability(
                availabilityDto.getId(),
                employeeStub,
                availabilityDto.getDayOfWeek(),
                availabilityDto.getStartTime(),
                availabilityDto.getEndTime(),
                availabilityDto.getAvailable()
        );
    }
}
