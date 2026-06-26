
package com.summerproject2026.DentalWave.service.impl;

import com.summerproject2026.DentalWave.dto.TimeOffRequestDto;
import com.summerproject2026.DentalWave.entity.Employee;
import com.summerproject2026.DentalWave.entity.Schedule;
import com.summerproject2026.DentalWave.entity.TimeOffRequest;
import com.summerproject2026.DentalWave.entity.User;
import com.summerproject2026.DentalWave.enums.NotificationType;
import com.summerproject2026.DentalWave.enums.RequestStatus;
import com.summerproject2026.DentalWave.exception.ResourceNotFoundException;
import com.summerproject2026.DentalWave.mapper.TimeOffRequestMapper;
import com.summerproject2026.DentalWave.repository.EmployeeRepository;
import com.summerproject2026.DentalWave.repository.ScheduleRepository;
import com.summerproject2026.DentalWave.repository.TimeOffRequestRepository;
import com.summerproject2026.DentalWave.repository.UserRepository;
import com.summerproject2026.DentalWave.service.NotificationService;
import com.summerproject2026.DentalWave.service.TimeOffRequestService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of {@link TimeOffRequestService} containing the
 * business logic for managing time-off request entities.
 *
 * <p>Delegates persistence to {@link TimeOffRequestRepository} and
 * uses {@link TimeOffRequestMapper} for entity-to-DTO conversion.
 * Also depends on {@link EmployeeRepository} and {@link UserRepository}
 * to resolve related entities during approval and denial workflows.
 * Sends notifications to HR when a new request is submitted, to
 * the employee when their request is approved or denied, and to the
 * affected manager when an approved request impacts a published
 * schedule, via {@link NotificationService}.</p>
 */
@Service
@Transactional
public class TimeOffRequestServiceImpl implements TimeOffRequestService {

    private final TimeOffRequestRepository timeOffRequestRepository;
    private final TimeOffRequestMapper timeOffRequestMapper;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ScheduleRepository scheduleRepository;

    /**
     * Constructs the service with its required dependencies.
     *
     * @param timeOffRequestRepository the repository for TimeOffRequest persistence
     * @param timeOffRequestMapper     the mapper for entity-DTO conversion
     * @param employeeRepository       the repository for resolving Employee entities
     * @param userRepository           the repository for resolving User entities (reviewers)
     * @param notificationService      the service for sending notifications
     * @param scheduleRepository       the repository for checking schedule impact (UC8)
     */
    public TimeOffRequestServiceImpl(TimeOffRequestRepository timeOffRequestRepository,
                                     TimeOffRequestMapper timeOffRequestMapper,
                                     EmployeeRepository employeeRepository,
                                     UserRepository userRepository,
                                     NotificationService notificationService,
                                     ScheduleRepository scheduleRepository) {
        this.timeOffRequestRepository = timeOffRequestRepository;
        this.timeOffRequestMapper = timeOffRequestMapper;
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.scheduleRepository = scheduleRepository;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Resolves the full {@link Employee} entity from the repository
     * using the ID provided in the DTO, enforces PENDING as the initial
     * status, and sets the submission timestamp before persisting.
     * Notifies all HR users after the request is saved.</p>
     */
    @Override
    public TimeOffRequestDto createTimeOffRequest(TimeOffRequestDto timeOffRequestDto) {
        Employee employee = employeeRepository
                .findById(timeOffRequestDto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found with id: " + timeOffRequestDto.getEmployeeId()));

        TimeOffRequest timeOffRequest = timeOffRequestMapper
                .mapToTimeOffRequest(timeOffRequestDto);

        timeOffRequest.setEmployee(employee);
        timeOffRequest.setStatus(RequestStatus.PENDING);
        timeOffRequest.setSubmittedAt(LocalDateTime.now());

        TimeOffRequest savedRequest = timeOffRequestRepository.save(timeOffRequest);

        String employeeName = employee.getUser().getFirstName()
                + " " + employee.getUser().getLastName();

        boolean isEmergency = Boolean.TRUE.equals(timeOffRequestDto.getEmergency());

        userRepository.findByRoles_NameIgnoreCase("ROLE_HR")
                .forEach(hrUser -> {
                    try {
                        notificationService.notifyHrOfTimeOffRequest(
                                employeeName,
                                savedRequest.getId(),
                                isEmergency,
                                hrUser.getId());
                    } catch (Exception e) {
                        System.err.println("Failed to notify HR user "
                                + hrUser.getId() + ": " + e.getMessage());
                    }
                });

        return timeOffRequestMapper.mapToTimeOffRequestDto(savedRequest);
    }

    /**
     * {@inheritDoc}
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
     */
    @Override
    @Transactional(readOnly = true)
    public List<TimeOffRequestDto> getRequestsByEmployee(Long employeeId) {
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
     * <p>Notifies the employee that their request was approved, and
     * notifies any affected manager if the approval impacts a
     * published schedule (UC8).</p>
     */
    @Override
    public TimeOffRequestDto approveRequest(Long id, Long reviewedById, String reviewComment) {
        return reviewRequest(id, reviewedById, reviewComment, RequestStatus.APPROVED);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Notifies the employee that their request was denied.</p>
     */
    @Override
    public TimeOffRequestDto denyRequest(Long id, Long reviewedById, String reviewComment) {
        return reviewRequest(id, reviewedById, reviewComment, RequestStatus.DENIED);
    }

    /**
     * {@inheritDoc}
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
     * Used by UC6 — HR Approves/Rejects Time-Off Requests.
     * On approval, also checks for schedule impact (UC8).
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
        TimeOffRequest timeOffRequest = timeOffRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "TimeOffRequest not found with id: " + id));

        if (timeOffRequest.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException(
                    "Only PENDING requests can be reviewed. " +
                            "Current status: " + timeOffRequest.getStatus());
        }

        User reviewer = userRepository.findById(reviewedById)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reviewer (User) not found with id: " + reviewedById));

        timeOffRequest.setStatus(targetStatus);
        timeOffRequest.setReviewedBy(reviewer);
        timeOffRequest.setReviewedAt(LocalDateTime.now());
        timeOffRequest.setReviewComment(reviewComment);

        TimeOffRequest updatedRequest = timeOffRequestRepository.save(timeOffRequest);

        // Notify the employee of the status update (UC6 main flow step 10)
        notifyEmployeeOfDecision(updatedRequest, targetStatus);

        // If approved, check whether this affects any published schedule
        // and notify the responsible manager (UC8)
        if (targetStatus == RequestStatus.APPROVED) {
            notifyManagerOfScheduleImpact(updatedRequest);
        }

        return timeOffRequestMapper.mapToTimeOffRequestDto(updatedRequest);
    }

    /**
     * Notifies the employee that their time-off request has been reviewed.
     * Used by UC6 — HR Approves/Rejects Time-Off Requests, Employee Notification sub-flow.
     *
     * @param request  the reviewed time-off request
     * @param decision the decision made APPROVED or DENIED
     */
    private void notifyEmployeeOfDecision(TimeOffRequest request, RequestStatus decision) {
        try {
            Long employeeUserId = request.getEmployee().getUser().getId();

            String message = decision == RequestStatus.APPROVED
                    ? "Your time-off request has been approved."
                    : "Your time-off request has been denied.";

            NotificationType type = decision == RequestStatus.APPROVED
                    ? NotificationType.TIME_OFF_APPROVED
                    : NotificationType.TIME_OFF_DENIED;

            notificationService.sendNotification(
                    employeeUserId,
                    message,
                    type,
                    "REQUESTS",
                    request.getId());
        } catch (Exception e) {
            System.err.println("Failed to notify employee of decision: " + e.getMessage());
        }
    }

    /**
     * Checks whether an approved time-off request overlaps any published
     * schedule entries for the employee, and if so, notifies each
     * affected manager (the schedule's creator) that a schedule update
     * may be required.
     *
     * Used by UC8 — HR Notifies Manager of Schedule Change.
     *
     * <p>If no published schedule is affected, no notification is sent
     * (Alternate Flow: No Schedule Impact). If notification delivery
     * fails for a manager, the failure is logged but the approved
     * request remains saved (Alternate Flow: Notification Delivery
     * Failure).</p>
     *
     * @param request the approved time-off request
     */
    private void notifyManagerOfScheduleImpact(TimeOffRequest request) {
        try {
            Long employeeId = request.getEmployee().getId();
            String employeeName = request.getEmployee().getUser().getFirstName()
                    + " " + request.getEmployee().getUser().getLastName();

            List<Schedule> affectedSchedules = scheduleRepository
                    .findPublishedSchedulesByEmployeeIdAndDateRange(
                            employeeId,
                            request.getStartDate(),
                            request.getEndDate());

            // Alternate Flow: No Schedule Impact — nothing further to do
            if (affectedSchedules.isEmpty()) {
                return;
            }

            // Build the dates string for the notification message
            String dateRange = request.getStartDate().equals(request.getEndDate())
                    ? request.getStartDate().toString()
                    : request.getStartDate() + " to " + request.getEndDate();

            String reasonPart = (request.getReason() != null && !request.getReason().isBlank())
                    ? " Reason: " + request.getReason()
                    : "";

            String message = employeeName + " has approved time off for " + dateRange
                    + " which affects a published schedule. Schedule update may be required."
                    + reasonPart;

            // Collect distinct managers who created the affected schedules
            Set<Long> notifiedManagerIds = new HashSet<>();

            for (Schedule schedule : affectedSchedules) {
                User manager = schedule.getCreatedBy();
                if (manager == null || !notifiedManagerIds.add(manager.getId())) {
                    continue;
                }

                try {
                    notificationService.sendNotification(
                            manager.getId(),
                            message,
                            NotificationType.SCHEDULE_UPDATE_REQUIRED,
                            "REQUESTS",
                            request.getId());
                } catch (Exception e) {
                    // Alternate Flow: Notification Delivery Failure
                    // The approved request remains saved regardless
                    System.err.println("Failed to notify manager "
                            + manager.getId() + " of schedule impact: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            // Schedule-impact check failure should never block the approval itself
            System.err.println("Failed to check schedule impact for request "
                    + request.getId() + ": " + e.getMessage());
        }
    }
}