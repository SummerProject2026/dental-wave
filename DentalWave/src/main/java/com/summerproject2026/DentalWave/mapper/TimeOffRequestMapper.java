package com.dentalwave.mapper;

import com.summerproject2026.DentalWave.dto.TimeOffRequestDto;
import com.summerproject2026.DentalWave.entity.Employee;
import com.summerproject2026.DentalWave.entity.TimeOffRequest;
import com.summerproject2026.DentalWave.entity.User;
import org.springframework.stereotype.Component;

/**
 * Mapper class responsible for converting between
 * {@link TimeOffRequest} entity and {@link TimeOffRequestDto} objects.
 *
 * <p>Registered as a Spring component so it can be
 * injected wherever mapping is needed (e.g. service layer).</p>
 *
 * <p>Note: Since {@link TimeOffRequestDto} flattens nested relationships
 * (e.g. employee name, reviewer name) into plain fields, the mapper
 * handles that flattening on the way to the DTO, and reconstructs
 * minimal entity references (id only) on the way back to the entity.</p>
 */
@Component
public class TimeOffRequestMapper {

    /**
     * Converts a {@link TimeOffRequest} entity to a {@link TimeOffRequestDto}.
     *
     * <p>Flattens the nested {@code employee} and {@code reviewedBy}
     * relationships into their respective ID and name fields on the DTO,
     * avoiding unnecessary exposure of full entity graphs to the client.</p>
     *
     * @param timeOffRequest the TimeOffRequest entity to convert; must not be null
     * @return a populated {@link TimeOffRequestDto} reflecting the entity's data
     * @throws IllegalArgumentException if the provided timeOffRequest is null
     */
    public TimeOffRequestDto mapToTimeOffRequestDto(TimeOffRequest timeOffRequest) {
        if (timeOffRequest == null) {
            throw new IllegalArgumentException("TimeOffRequest entity must not be null");
        }

        // Safely extract employee fields — employee should always be present
        Long employeeId = null;
        String employeeName = null;
        if (timeOffRequest.getEmployee() != null) {
            Employee employee = timeOffRequest.getEmployee();
            employeeId = employee.getId();

            // Build full name from the nested User inside Employee
            if (employee.getUser() != null) {
                employeeName = employee.getUser().getFirstName()
                        + " " + employee.getUser().getLastName();
            }
        }

        // Safely extract reviewer fields — reviewer may be null if not yet reviewed
        Long reviewedById = null;
        String reviewedByName = null;
        if (timeOffRequest.getReviewedBy() != null) {
            User reviewer = timeOffRequest.getReviewedBy();
            reviewedById = reviewer.getId();
            reviewedByName = reviewer.getFirstName() + " " + reviewer.getLastName();
        }

        return new TimeOffRequestDto(
                timeOffRequest.getId(),
                employeeId,
                employeeName,
                timeOffRequest.getStartDate(),
                timeOffRequest.getEndDate(),
                timeOffRequest.getStartTime(),
                timeOffRequest.getEndTime(),
                timeOffRequest.getReason(),
                timeOffRequest.getStatus(),
                reviewedById,
                reviewedByName,
                timeOffRequest.getReviewedAt(),
                timeOffRequest.getReviewComment(),
                timeOffRequest.getEmergency(),
                timeOffRequest.getSubmittedAt()
        );
    }

    /**
     * Converts a {@link TimeOffRequestDto} to a {@link TimeOffRequest} entity.
     *
     * <p>Since the DTO only carries IDs for related entities (employee, reviewer),
     * this method reconstructs minimal {@link Employee} and {@link User} references
     * using only those IDs. Full entity resolution (fetching from DB) should be
     * handled in the service layer before or after this mapping step if needed.</p>
     *
     * @param timeOffRequestDto the TimeOffRequestDto to convert; must not be null
     * @return a populated {@link TimeOffRequest} entity reflecting the DTO's data
     * @throws IllegalArgumentException if the provided timeOffRequestDto is null
     */
    public TimeOffRequest mapToTimeOffRequest(TimeOffRequestDto timeOffRequestDto) {
        if (timeOffRequestDto == null) {
            throw new IllegalArgumentException("TimeOffRequestDto must not be null");
        }

        // Reconstruct a minimal Employee reference using only the ID.
        // The service layer is responsible for fetching the full Employee if needed.
        Employee employee = null;
        if (timeOffRequestDto.getEmployeeId() != null) {
            employee = new Employee();
            employee.setId(timeOffRequestDto.getEmployeeId());
        }

        // Reconstruct a minimal User reference for the reviewer using only the ID.
        // The service layer is responsible for fetching the full User if needed.
        User reviewedBy = null;
        if (timeOffRequestDto.getReviewedById() != null) {
            reviewedBy = new User();
            reviewedBy.setId(timeOffRequestDto.getReviewedById());
        }

        return new TimeOffRequest(
                timeOffRequestDto.getId(),
                employee,
                timeOffRequestDto.getStartDate(),
                timeOffRequestDto.getEndDate(),
                timeOffRequestDto.getStartTime(),
                timeOffRequestDto.getEndTime(),
                timeOffRequestDto.getReason(),
                timeOffRequestDto.getStatus(),
                reviewedBy,
                timeOffRequestDto.getReviewedAt(),
                timeOffRequestDto.getReviewComment(),
                timeOffRequestDto.getEmergency(),
                timeOffRequestDto.getSubmittedAt()
        );
    }
}