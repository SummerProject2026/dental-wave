package com.summerproject2026.DentalWave.service;

import com.summerproject2026.DentalWave.dto.TimeOffRequestDto;
import com.summerproject2026.DentalWave.enums.RequestStatus;

import java.util.List;

/**
 * Service interface defining the business operations
 * available for managing {@link com.yourpackage.entity.TimeOffRequest} entities.
 *
 * <p>The service layer sits between the controller and repository,
 * handling business logic, validation, and entity-to-DTO mapping.
 * The concrete implementation is {@link TimeOffRequestServiceImpl}.</p>
 */
public interface TimeOffRequestService {

    /**
     * Creates a new time-off request and persists it to the database.
     *
     * <p>The request status is automatically set to
     * {@link RequestStatus#PENDING} on creation.</p>
     *
     * @param timeOffRequestDto the data for the new request; must not be null
     * @return the created request as a {@link TimeOffRequestDto} with its generated ID
     */
    TimeOffRequestDto createTimeOffRequest(TimeOffRequestDto timeOffRequestDto);

    /**
     * Retrieves a single time-off request by its unique ID.
     *
     * @param id the ID of the request to retrieve; must not be null
     * @return the matching request as a {@link TimeOffRequestDto}
     * @throws com.yourpackage.exception.ResourceNotFoundException
     *         if no request exists with the given ID
     */
    TimeOffRequestDto getTimeOffRequestById(Long id);

    /**
     * Retrieves all time-off requests stored in the database.
     *
     * @return a {@link List} of all requests as {@link TimeOffRequestDto} objects;
     *         returns an empty list if none exist
     */
    List<TimeOffRequestDto> getAllRequests();

    /**
     * Retrieves all time-off requests submitted by a specific employee.
     *
     * @param employeeId the ID of the employee whose requests to retrieve;
     *                   must not be null
     * @return a {@link List} of matching requests as {@link TimeOffRequestDto} objects;
     *         returns an empty list if none found
     * @throws com.yourpackage.exception.ResourceNotFoundException
     *         if no employee exists with the given ID
     */
    List<TimeOffRequestDto> getRequestsByEmployee(Long employeeId);

    /**
     * Retrieves all time-off requests filtered by their current status.
     *
     * @param status the {@link RequestStatus} to filter by; must not be null
     * @return a {@link List} of matching requests as {@link TimeOffRequestDto} objects;
     *         returns an empty list if none found
     */
    List<TimeOffRequestDto> getRequestsByStatus(RequestStatus status);

    /**
     * Approves a pending time-off request.
     *
     * <p>Sets the request status to {@link RequestStatus#APPROVED},
     * records the reviewer, timestamp, and optional comment.</p>
     *
     * @param id             the ID of the request to approve; must not be null
     * @param reviewedById   the ID of the user approving the request; must not be null
     * @param reviewComment  an optional comment from the reviewer; may be null
     * @return the updated request as a {@link TimeOffRequestDto}
     * @throws com.yourpackage.exception.ResourceNotFoundException
     *         if no request exists with the given ID, or no user exists
     *         with the given reviewedById
     * @throws IllegalStateException if the request is not in PENDING status
     */
    TimeOffRequestDto approveRequest(Long id, Long reviewedById, String reviewComment);

    /**
     * Denies a pending time-off request.
     *
     * <p>Sets the request status to {@link RequestStatus#DENIED},
     * records the reviewer, timestamp, and optional comment.</p>
     *
     * @param id             the ID of the request to deny; must not be null
     * @param reviewedById   the ID of the user denying the request; must not be null
     * @param reviewComment  an optional comment from the reviewer; may be null
     * @return the updated request as a {@link TimeOffRequestDto}
     * @throws com.yourpackage.exception.ResourceNotFoundException
     *         if no request exists with the given ID, or no user exists
     *         with the given reviewedById
     * @throws IllegalStateException if the request is not in PENDING status
     */
    TimeOffRequestDto denyRequest(Long id, Long reviewedById, String reviewComment);

    /**
     * Deletes the time-off request with the given ID from the database.
     *
     * @param id the ID of the request to delete; must not be null
     * @throws com.yourpackage.exception.ResourceNotFoundException
     *         if no request exists with the given ID
     */
    void deleteRequest(Long id);
}