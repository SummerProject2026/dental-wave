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
 * to resolve related entities during approval and denial workflows.</p>
 */
@Service
@Transactional
public class TimeOffRequestServiceImpl implements TimeOffRequestService {

    private final TimeOffRequestRepository timeOffRequestRepository;
    private final TimeOffRequestMapper timeOffRequestMapper;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    /**
     * Constructs the service with its required dependencies.
     * Constructor injection is preferred over field injection
     * for testability and immutability.
     *
     * @param timeOffRequestRepository the repository for TimeOffRequest persistence
     * @param timeOffRequestMapper     the mapper for entity-DTO conversion
     * @param employeeRepository       the repository for resolving Employee entities
     * @param userRepository           the repository for resolving User entities (reviewers)
     */
    public TimeOffRequestServiceImpl(TimeOffRequestRepository timeOffRequestRepository,
                                     TimeOffRequestMapper timeOffRequestMapper,
                                     EmployeeRepository employeeRepository,
                                     UserRepository userRepository) {
        this.timeOffRequestRepository = timeOffRequestRepository;
        this.timeOffRequestMapper = timeOffRequestMapper;
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Resolves the full {@link Employee} entity from the repository
     * using the ID provided in the DTO, enforces PENDING as the initial
     * status, and sets the submission timestamp before persisting.</p>
     */
    @Override
    public TimeOffRequestDto createTimeOffRequest(TimeOffRequestDto timeOffRequestDto) {
        // Resolve the full Employee entity to ensure it exists
        Employee employee = employeeRepository
                .findById(timeOffRequestDto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found with id: " + timeOffRequestDto.getEmployeeId()));

        // Map the DTO to an entity, then override fields that must be
        // set server-side regardless of what the client sends
        TimeOffRequest timeOffRequest = timeOffRequestMapper
                .mapToTimeOffRequest(timeOffRequestDto);

        timeOffRequest.setEmployee(employee);

        // Always initialize new requests as PENDING
        timeOffRequest.setStatus(RequestStatus.PENDING);

        // Set submission timestamp server-side to prevent client manipulation
        timeOffRequest.setSubmittedAt(LocalDateTime.now());

        TimeOffRequest savedRequest = timeOffRequestRepository.save(timeOffRequest);
        return timeOffRequestMapper.mapToTimeOffRequestDto(savedRequest);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Throws {@link ResourceNotFoundException} if the request
     * does not exist, so the controller can return a 404 response.</p>
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
     * {@inheritDoc}
     *
     * <p>Marked as read-only to allow the JPA provider to
     * apply query optimizations (no dirty checking needed).</p>
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
     * {@inheritDoc}
     *
     * <p>Confirms the employee exists before querying their requests,
     * so the caller receives a meaningful 404 rather than an empty list
     * when the employee ID is invalid.</p>
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
     * {@inheritDoc}
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
     * {@inheritDoc}
     *
     * <p>Guards against approving a request that is not currently PENDING
     * to prevent double-approvals or approving already denied requests.</p>
     */
    @Override
    public TimeOffRequestDto approveRequest(Long id, Long reviewedById, String reviewComment) {
        return reviewRequest(id, reviewedById, reviewComment, RequestStatus.APPROVED);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Guards against denying a request that is not currently PENDING
     * to prevent double-denials or denying already approved requests.</p>
     */
    @Override
    public TimeOffRequestDto denyRequest(Long id, Long reviewedById, String reviewComment) {
        return reviewRequest(id, reviewedById, reviewComment, RequestStatus.DENIED);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Confirms the request exists before deletion so the caller
     * receives a meaningful error rather than a silent no-op.</p>
     */
    @Override
    public void deleteRequest(Long id) {
        TimeOffRequest timeOffRequest = timeOffRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "TimeOffRequest not found with id: " + id));
        timeOffRequestRepository.delete(timeOffRequest);
    }

    // -------------------------
    // Private Helpers
    // -------------------------

    /**
     * Shared internal logic for approving and denying requests.
     *
     * <p>Extracted to avoid duplicating the fetch-validate-update-save
     * pattern between {@link #approveRequest} and {@link #denyRequest}.</p>
     *
     * @param id             the ID of the request to review
     * @param reviewedById   the ID of the reviewing user
     * @param reviewComment  an optional comment from the reviewer
     * @param targetStatus   the status to set (APPROVED or DENIED)
     * @return the updated request as a {@link TimeOffRequestDto}
     * @throws ResourceNotFoundException if the request or reviewer is not found
     * @throws IllegalStateException     if the request is not currently PENDING
     */
    private TimeOffRequestDto reviewRequest(Long id,
                                            Long reviewedById,
                                            String reviewComment,
                                            RequestStatus targetStatus) {
        // Fetch the request or 404
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

        TimeOffRequest updatedRequest = timeOffRequestRepository.save(timeOffRequest);
        return timeOffRequestMapper.mapToTimeOffRequestDto(updatedRequest);
    }
}