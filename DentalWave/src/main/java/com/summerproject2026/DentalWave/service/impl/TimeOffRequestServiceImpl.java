package com.summerproject2026.DentalWave.service.impl;

import com.summerproject2026.DentalWave.dto.TimeOffRequestDto;
import com.summerproject2026.DentalWave.entity.Employee;
import com.summerproject2026.DentalWave.entity.TimeOffRequest;
import com.summerproject2026.DentalWave.entity.User;
import com.summerproject2026.DentalWave.enums.RequestStatus;
import com.summerproject2026.DentalWave.exception.ResourceNotFoundException;
import com.summerproject2026.DentalWave.mapper.TimeOffRequestMapper;
import com.summerproject2026.DentalWave.repository.EmployeeRepository;
import com.summerproject2026.DentalWave.repository.TimeOffRequestRepository;
import com.summerproject2026.DentalWave.repository.UserRepository;
import com.summerproject2026.DentalWave.service.NotificationService;
import com.summerproject2026.DentalWave.service.TimeOffRequestService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of {@link TimeOffRequestService} containing the
 * business logic for managing time-off request entities.
 *
 * <p>Delegates persistence to {@link TimeOffRequestRepository} and
 * uses {@link TimeOffRequestMapper} for entity-to-DTO conversion.
 * Also depends on {@link EmployeeRepository} and {@link UserRepository}
 * to resolve related entities during approval and denial workflows.
 * Sends notifications to HR via {@link NotificationService} when
 * a new time-off request is submitted.</p>
 */
@Service
@Transactional
public class TimeOffRequestServiceImpl implements TimeOffRequestService {

    /** Repository for TimeOffRequest persistence */
    private final TimeOffRequestRepository timeOffRequestRepository;

    /** Mapper for entity-DTO conversion */
    private final TimeOffRequestMapper timeOffRequestMapper;

    /** Repository for resolving Employee entities */
    private final EmployeeRepository employeeRepository;

    /** Repository for resolving User entities and finding HR users */
    private final UserRepository userRepository;

    /** Service for sending in-app and email notifications to HR */
    private final NotificationService notificationService;

    /**
     * Constructs the service with its required dependencies.
     *
     * @param timeOffRequestRepository the repository for TimeOffRequest persistence
     * @param timeOffRequestMapper the mapper for entity-DTO conversion
     * @param employeeRepository the repository for resolving Employee entities
     * @param userRepository the repository for resolving User entities
     * @param notificationService the service for sending notifications
     */
    public TimeOffRequestServiceImpl(TimeOffRequestRepository timeOffRequestRepository,
                                     TimeOffRequestMapper timeOffRequestMapper,
                                     EmployeeRepository employeeRepository,
                                     UserRepository userRepository,
                                     NotificationService notificationService) {
        this.timeOffRequestRepository = timeOffRequestRepository;
        this.timeOffRequestMapper = timeOffRequestMapper;
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    /**
     * Creates a new time-off request and notifies all HR users.
     *
     * <p>Steps:
     * <ol>
     *   <li>Resolve the Employee entity from the repository.</li>
     *   <li>Map the DTO to an entity.</li>
     *   <li>Set status to PENDING and submission timestamp server-side.</li>
     *   <li>Save the request.</li>
     *   <li>Notify all HR users via in-app and email notifications.</li>
     * </ol>
     *
     * @param timeOffRequestDto the request data from the assistant
     * @return the saved TimeOffRequestDto
     * @throws ResourceNotFoundException if the employee does not exist
     */
    @Override
    public TimeOffRequestDto createTimeOffRequest(TimeOffRequestDto timeOffRequestDto) {
        // Resolve the full Employee entity to ensure it exists
        Employee employee = employeeRepository
                .findById(timeOffRequestDto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found with id: " + timeOffRequestDto.getEmployeeId()));

        // Map the DTO to an entity
        TimeOffRequest timeOffRequest = timeOffRequestMapper
                .mapToTimeOffRequest(timeOffRequestDto);

        // Set the employee back reference
        timeOffRequest.setEmployee(employee);

        // Always initialize new requests as PENDING regardless of client input
        timeOffRequest.setStatus(RequestStatus.PENDING);

        // Set submission timestamp server-side to prevent client manipulation
        timeOffRequest.setSubmittedAt(LocalDateTime.now());

        // Save the request to the database
        TimeOffRequest savedRequest = timeOffRequestRepository.save(timeOffRequest);

        // Build employee full name for notification message
        String employeeName = employee.getUser().getFirstName()
                + " " + employee.getUser().getLastName();

        // Determine if this is an emergency request
        boolean isEmergency = Boolean.TRUE.equals(timeOffRequestDto.getEmergency());

        // Notify all HR users about the new time-off request
        userRepository.findByRoles_NameIgnoreCase("ROLE_HR")
                .forEach(hrUser -> {
                    try {
                        // Send in-app notification and email to each HR user
                        notificationService.notifyHrOfTimeOffRequest(
                                employeeName,
                                savedRequest.getId(),
                                isEmergency,
                                hrUser.getId());
                    } catch (Exception e) {
                        // Log but don't fail the request submission
                        // Notification failure should not block the assistant
                        System.err.println("Failed to notify HR user "
                                + hrUser.getId() + ": " + e.getMessage());
                    }
                });

        return timeOffRequestMapper.mapToTimeOffRequestDto(savedRequest);
    }

    /**
     * Retrieves a single time-off request by its unique ID.
     *
     * @param id the ID of the request to retrieve
     * @return the matching TimeOffRequestDto
     * @throws ResourceNotFoundException if no request exists with the given id
     */
    @Override
    @Transactional(readOnly = true)
    public TimeOffRequestDto getTimeOffRequestById(Long id) {
        TimeOffRequest timeOffRequest = timeOffRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "TimeOffRequest not found with id: " + id));
        return timeOffRequestMapper.mapToTimeOffRequestDto(timeOffRequest);
    }

    /**
     * Retrieves all time-off requests in the system.
     *
     * @return list of all TimeOffRequestDtos
     */
    @Override
    @Transactional(readOnly = true)
    public List<TimeOffRequestDto> getAllRequests() {
        return timeOffRequestRepository.findAll()
                .stream()
                .map(timeOffRequestMapper::mapToTimeOffRequestDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all time-off requests submitted by a specific employee.
     *
     * @param employeeId the ID of the employee
     * @return list of matching TimeOffRequestDtos
     * @throws ResourceNotFoundException if the employee does not exist
     */
    @Override
    @Transactional(readOnly = true)
    public List<TimeOffRequestDto> getRequestsByEmployee(Long employeeId) {
        // Confirm the employee exists before fetching their requests
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException(
                    "Employee not found with id: " + employeeId);
        }
        return timeOffRequestRepository.findByEmployeeId(employeeId)
                .stream()
                .map(timeOffRequestMapper::mapToTimeOffRequestDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all time-off requests filtered by status.
     *
     * @param status the RequestStatus to filter by
     * @return list of matching TimeOffRequestDtos
     */
    @Override
    @Transactional(readOnly = true)
    public List<TimeOffRequestDto> getRequestsByStatus(RequestStatus status) {
        return timeOffRequestRepository.findByStatus(status)
                .stream()
                .map(timeOffRequestMapper::mapToTimeOffRequestDto)
                .collect(Collectors.toList());
    }

    /**
     * Approves a pending time-off request.
     *
     * @param id the ID of the request to approve
     * @param reviewedById the ID of the reviewing HR user
     * @param reviewComment an optional comment from the reviewer
     * @return the updated TimeOffRequestDto with APPROVED status
     * @throws ResourceNotFoundException if the request or reviewer does not exist
     * @throws IllegalStateException if the request is not PENDING
     */
    @Override
    public TimeOffRequestDto approveRequest(Long id, Long reviewedById, String reviewComment) {
        return reviewRequest(id, reviewedById, reviewComment, RequestStatus.APPROVED);
    }

    /**
     * Denies a pending time-off request.
     *
     * @param id the ID of the request to deny
     * @param reviewedById the ID of the reviewing HR user
     * @param reviewComment an optional comment from the reviewer
     * @return the updated TimeOffRequestDto with DENIED status
     * @throws ResourceNotFoundException if the request or reviewer does not exist
     * @throws IllegalStateException if the request is not PENDING
     */
    @Override
    public TimeOffRequestDto denyRequest(Long id, Long reviewedById, String reviewComment) {
        return reviewRequest(id, reviewedById, reviewComment, RequestStatus.DENIED);
    }

    /**
     * Deletes a time-off request by ID.
     *
     * @param id the ID of the request to delete
     * @throws ResourceNotFoundException if no request exists with the given id
     */
    @Override
    public void deleteRequest(Long id) {
        TimeOffRequest timeOffRequest = timeOffRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "TimeOffRequest not found with id: " + id));
        timeOffRequestRepository.delete(timeOffRequest);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Shared internal logic for approving and denying requests.
     *
     * @param id the ID of the request to review
     * @param reviewedById the ID of the reviewing user
     * @param reviewComment an optional comment from the reviewer
     * @param targetStatus the status to set APPROVED or DENIED
     * @return the updated TimeOffRequestDto
     * @throws ResourceNotFoundException if the request or reviewer is not found
     * @throws IllegalStateException if the request is not currently PENDING
     */
    private TimeOffRequestDto reviewRequest(Long id,
                                            Long reviewedById,
                                            String reviewComment,
                                            RequestStatus targetStatus) {
        // Fetch the request or throw 404
        TimeOffRequest timeOffRequest = timeOffRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "TimeOffRequest not found with id: " + id));

        // Only PENDING requests can be approved or denied
        if (timeOffRequest.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException(
                    "Only PENDING requests can be reviewed. " +
                            "Current status: " + timeOffRequest.getStatus());
        }

        // Resolve the reviewing user to ensure they exist
        User reviewer = userRepository.findById(reviewedById)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reviewer (User) not found with id: " + reviewedById));

        // Apply the review outcome
        timeOffRequest.setStatus(targetStatus);
        timeOffRequest.setReviewedBy(reviewer);
        timeOffRequest.setReviewedAt(LocalDateTime.now());
        timeOffRequest.setReviewComment(reviewComment);

        // Save and return the updated request
        TimeOffRequest updatedRequest = timeOffRequestRepository.save(timeOffRequest);
        return timeOffRequestMapper.mapToTimeOffRequestDto(updatedRequest);
    }
}