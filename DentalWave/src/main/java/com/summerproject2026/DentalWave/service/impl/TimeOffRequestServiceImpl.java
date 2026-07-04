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
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of {@link TimeOffRequestService} containing the
 * business logic for managing time-off request entities.
 *
 * <p>Sends notifications to HR when a new request is submitted, to
 * the employee when their request is approved or denied, and always
 * notifies managers when a request is approved — with an extra note
 * if the approval impacts a published schedule.</p>
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

    @Override
    @Transactional(readOnly = true)
    public TimeOffRequestDto getTimeOffRequestById(Long id) {
        TimeOffRequest timeOffRequest = timeOffRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "TimeOffRequest not found with id: " + id));
        return timeOffRequestMapper.mapToTimeOffRequestDto(timeOffRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeOffRequestDto> getAllRequests() {
        return timeOffRequestRepository.findAll()
                .stream()
                .map(timeOffRequestMapper::mapToTimeOffRequestDto)
                .collect(Collectors.toList());
    }

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

    @Override
    @Transactional(readOnly = true)
    public List<TimeOffRequestDto> getRequestsByStatus(RequestStatus status) {
        return timeOffRequestRepository.findByStatus(status)
                .stream()
                .map(timeOffRequestMapper::mapToTimeOffRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public TimeOffRequestDto approveRequest(Long id, Long reviewedById, String reviewComment) {
        return reviewRequest(id, reviewedById, reviewComment, RequestStatus.APPROVED);
    }

    @Override
    public TimeOffRequestDto denyRequest(Long id, Long reviewedById, String reviewComment) {
        return reviewRequest(id, reviewedById, reviewComment, RequestStatus.DENIED);
    }

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

    private void deductPtoHours(TimeOffRequest request) {
        try {
            Employee employee = request.getEmployee();
            double hoursToDeduct;

            if (request.getStartDate() != null && request.getEndDate() != null
                    && request.getStartDate().equals(request.getEndDate())
                    && request.getStartTime() != null && request.getEndTime() != null) {
                long minutes = ChronoUnit.MINUTES.between(request.getStartTime(), request.getEndTime());
                hoursToDeduct = minutes / 60.0;
            } else if (request.getStartDate() != null && request.getEndDate() != null) {
                long days = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
                hoursToDeduct = days * 8.0;
            } else {
                hoursToDeduct = 8.0;
            }

            double newBalance = (employee.getTimeOff() != null ? employee.getTimeOff() : 0.0) - hoursToDeduct;
            employee.setTimeOff(newBalance);
            employeeRepository.save(employee);
        } catch (Exception e) {
            System.err.println("Failed to deduct PTO hours for request " + request.getId() + ": " + e.getMessage());
        }
    }

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

        // Deduct PTO hours from employee balance when approved
        if (targetStatus == RequestStatus.APPROVED) {
            deductPtoHours(updatedRequest);
        }

        // Notify the employee of the decision
        notifyEmployeeOfDecision(updatedRequest, targetStatus);

        // Always notify manager when approved
        if (targetStatus == RequestStatus.APPROVED) {
            notifyManagerOfScheduleImpact(updatedRequest);
        }

        return timeOffRequestMapper.mapToTimeOffRequestDto(updatedRequest);
    }

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
     * Always notifies managers when a time-off request is approved.
     * If the approval overlaps a published schedule, the message
     * includes a note that a schedule update may be required and
     * only notifies the managers who created those schedules.
     * If there is no schedule impact, all managers are notified.
     */
    private void notifyManagerOfScheduleImpact(TimeOffRequest request) {
        try {
            Long employeeId = request.getEmployee().getId();
            String employeeName = request.getEmployee().getUser().getFirstName()
                    + " " + request.getEmployee().getUser().getLastName();

            String dateRange = request.getStartDate().equals(request.getEndDate())
                    ? request.getStartDate().toString()
                    : request.getStartDate() + " to " + request.getEndDate();

            List<Schedule> affectedSchedules = scheduleRepository
                    .findPublishedSchedulesByEmployeeIdAndDateRange(
                            employeeId,
                            request.getStartDate(),
                            request.getEndDate());

            Set<Long> notifiedManagerIds = new HashSet<>();

            if (!affectedSchedules.isEmpty()) {
                // Schedule is impacted — notify the specific managers
                // who created the affected schedules
                String message = employeeName + " has approved time off for " + dateRange
                        + " which affects a published schedule. "
                        + "Schedule update may be required.";

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
                        System.err.println("Failed to notify manager "
                                + manager.getId() + ": " + e.getMessage());
                    }
                }
            } else {
                // No schedule impact — still notify all managers
                // so they are always aware of approved time-off
                String message = employeeName + " has been approved for time off on "
                        + dateRange + ".";

                userRepository.findByRoles_NameIgnoreCase("ROLE_MANAGER")
                        .forEach(manager -> {
                            if (!notifiedManagerIds.add(manager.getId())) return;
                            try {
                                notificationService.sendNotification(
                                        manager.getId(),
                                        message,
                                        NotificationType.TIME_OFF_APPROVED,
                                        "REQUESTS",
                                        request.getId());
                            } catch (Exception e) {
                                System.err.println("Failed to notify manager "
                                        + manager.getId() + ": " + e.getMessage());
                            }
                        });
            }
        } catch (Exception e) {
            System.err.println("Failed to check schedule impact for request "
                    + request.getId() + ": " + e.getMessage());
        }
    }
}