package com.summerproject2026.DentalWave.service.impl;

import com.summerproject2026.DentalWave.dto.ScheduleDto;
import com.summerproject2026.DentalWave.entity.Employee;
import com.summerproject2026.DentalWave.entity.Calendar;
import com.summerproject2026.DentalWave.exception.ResourceNotFoundException;
import com.summerproject2026.DentalWave.mapper.ScheduleMapper;
import com.summerproject2026.DentalWave.entity.Schedule;
import com.summerproject2026.DentalWave.entity.ScheduleTeam;
import com.summerproject2026.DentalWave.entity.SchedulingResource;
import com.summerproject2026.DentalWave.enums.NotificationType;
import com.summerproject2026.DentalWave.repository.EmployeeRepository;
import com.summerproject2026.DentalWave.repository.ScheduleRepository;
import com.summerproject2026.DentalWave.repository.ScheduleTeamRepository;
import com.summerproject2026.DentalWave.repository.SchedulingResourceRepository;
import com.summerproject2026.DentalWave.repository.UserRepository;
import com.summerproject2026.DentalWave.service.NotificationService;
import com.summerproject2026.DentalWave.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of ScheduleService.
 *
 * <p>Note: the {@code userId} parameter in
 * {@link #assignEmployeeToTeam(Long, Long, Long)} and
 * {@link #removeEmployeeFromTeam(Long, Long, Long)} is actually the
 * {@link ScheduleTeam} id, not a User id. The parameter name is kept
 * for interface compatibility with the existing controller routes.</p>
 *
 * <p>When a team assignment changes on a published calendar, the affected
 * employee receives a SCHEDULE_UPDATE notification (UC12).</p>
 */
@Service
@Transactional
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final ScheduleTeamRepository scheduleTeamRepository;
    @Autowired
    private SchedulingResourceRepository schedulingResourceRepository;
    private final ScheduleMapper scheduleMapper;
    private final NotificationService notificationService;

    @Autowired
    public ScheduleServiceImpl(ScheduleRepository scheduleRepository,
                               UserRepository userRepository,
                               EmployeeRepository employeeRepository,
                               ScheduleTeamRepository scheduleTeamRepository,
                               ScheduleMapper scheduleMapper,
                               NotificationService notificationService) {
        this.scheduleRepository = scheduleRepository;
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.scheduleTeamRepository = scheduleTeamRepository;
        this.scheduleMapper = scheduleMapper;
        this.notificationService = notificationService;
    }

    /** Creates a new schedule */
    @Override
    public ScheduleDto createSchedule(ScheduleDto scheduleDto) {
        Schedule schedule = scheduleMapper.mapToSchedule(scheduleDto);
        return scheduleMapper.mapToScheduleDto(scheduleRepository.save(schedule));
    }

    /** Fetches a single schedule by ID */
    @Override
    @Transactional(readOnly = true)
    public ScheduleDto getScheduleById(Long id) {
        return scheduleMapper.mapToScheduleDto(findScheduleOrThrow(id));
    }

    /** Returns all schedules */
    @Override
    @Transactional(readOnly = true)
    public List<ScheduleDto> getAllSchedules() {
        return scheduleRepository.findAll().stream()
                .map(scheduleMapper::mapToScheduleDto)
                .collect(Collectors.toList());
    }

    /** Returns schedules that fall on the given date */
    @Override
    @Transactional(readOnly = true)
    public List<ScheduleDto> getSchedulesByDate(LocalDate date) {
        return scheduleRepository.findByDate(date).stream()
                .map(scheduleMapper::mapToScheduleDto)
                .collect(Collectors.toList());
    }

    /** Returns all schedules belonging to a given calendar */
    @Override
    @Transactional(readOnly = true)
    public List<ScheduleDto> getSchedulesByCalendar(Long calendarId) {
        return scheduleRepository.findByCalendarId(calendarId).stream()
                .map(scheduleMapper::mapToScheduleDto)
                .collect(Collectors.toList());
    }

    /**
     * Returns all published schedules assigned to a given employee.
     * Used by UC2 — Employee Views Personal Calendar.
     */
    @Override
    @Transactional(readOnly = true)
    public List<ScheduleDto> getSchedulesByEmployee(Long employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException(
                    "Employee not found with id: " + employeeId);
        }
        return scheduleRepository.findPublishedSchedulesByEmployeeId(employeeId).stream()
                .map(scheduleMapper::mapToScheduleDto)
                .collect(Collectors.toList());
    }

    /** Updates scalar fields on an existing schedule */
    @Override
    public ScheduleDto updateSchedule(Long id, ScheduleDto scheduleDto) {
        Schedule existing = findScheduleOrThrow(id);
        existing.setDate(scheduleDto.getDate());
        existing.setStartTime(scheduleDto.getStartTime());
        existing.setEndTime(scheduleDto.getEndTime());
        existing.setNotes(scheduleDto.getNotes());
        return scheduleMapper.mapToScheduleDto(scheduleRepository.save(existing));
    }

    /** Deletes a schedule by ID */
    @Override
    public void deleteSchedule(Long id) {
        scheduleRepository.delete(findScheduleOrThrow(id));
    }

    /**
     * Adds an employee to a team within a schedule.
     * If the parent calendar is published, sends a SCHEDULE_UPDATE
     * notification to the newly added employee (UC12).
     */
    @Override
    public ScheduleDto assignEmployeeToTeam(Long scheduleId, Long teamId, Long employeeId) {
        Schedule schedule = findScheduleOrThrow(scheduleId);

        ScheduleTeam team = scheduleTeamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Team not found with id: " + teamId));

        if (!team.getSchedule().getId().equals(scheduleId)) {
            throw new IllegalArgumentException(
                    "Team " + teamId + " does not belong to schedule " + scheduleId);
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found with id: " + employeeId));

        boolean alreadyOnTeam = team.getEmployees().stream()
                .anyMatch(e -> e.getId().equals(employeeId));

        if (!alreadyOnTeam) {
            team.getEmployees().add(employee);
            scheduleTeamRepository.save(team);

            // UC12 — notify employee of schedule update if calendar is published
            notifyEmployeeIfPublished(schedule, employee, NotificationType.SCHEDULE_UPDATE);
        }

        return scheduleMapper.mapToScheduleDto(findScheduleOrThrow(scheduleId));
    }

    /**
     * Removes an employee from a team within a schedule.
     * If the parent calendar is published, sends a SCHEDULE_UPDATE
     * notification to the removed employee (UC12).
     */
    @Override
    public ScheduleDto removeEmployeeFromTeam(Long scheduleId, Long teamId, Long employeeId) {
        Schedule schedule = findScheduleOrThrow(scheduleId);

        ScheduleTeam team = scheduleTeamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Team not found with id: " + teamId));

        if (!team.getSchedule().getId().equals(scheduleId)) {
            throw new IllegalArgumentException(
                    "Team " + teamId + " does not belong to schedule " + scheduleId);
        }

        boolean wasOnTeam = team.getEmployees().removeIf(e -> e.getId().equals(employeeId));
        team.getAssignmentNotes().remove("employee:" + employeeId);
        scheduleTeamRepository.save(team);

        // UC12 — notify employee of schedule update if calendar is published
        if (wasOnTeam) {
            Employee employee = employeeRepository.findById(employeeId).orElse(null);
            if (employee != null) {
                notifyEmployeeIfPublished(schedule, employee, NotificationType.SCHEDULE_UPDATE);
            }
        }

        return scheduleMapper.mapToScheduleDto(findScheduleOrThrow(scheduleId));
    }

    @Override
    public ScheduleDto assignResourceToTeam(Long scheduleId, Long teamId, Long resourceId) {
        Schedule schedule = findScheduleOrThrow(scheduleId);
        ScheduleTeam team = findTeamForSchedule(teamId, scheduleId);
        SchedulingResource resource = schedulingResourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Scheduling resource not found with id: " + resourceId));
        if (!resource.isActive() || resource.getType() != SchedulingResource.Type.ASSISTANT) {
            throw new IllegalArgumentException("Only active assistants can be assigned.");
        }
        boolean assignedElsewhere = schedule.getTeams().stream()
                .flatMap(value -> value.getResources().stream())
                .anyMatch(value -> value.getId().equals(resourceId));
        if (!assignedElsewhere) {
            team.getResources().add(resource);
            scheduleTeamRepository.save(team);
        }
        return scheduleMapper.mapToScheduleDto(findScheduleOrThrow(scheduleId));
    }

    @Override
    public ScheduleDto removeResourceFromTeam(Long scheduleId, Long teamId, Long resourceId) {
        findScheduleOrThrow(scheduleId);
        ScheduleTeam team = findTeamForSchedule(teamId, scheduleId);
        team.getResources().removeIf(resource -> resource.getId().equals(resourceId));
        team.getAssignmentNotes().remove("resource:" + resourceId);
        scheduleTeamRepository.save(team);
        return scheduleMapper.mapToScheduleDto(findScheduleOrThrow(scheduleId));
    }

    @Override
    public ScheduleDto updateAssignmentPartialDayNote(
            Long scheduleId,
            Long teamId,
            Long assignmentId,
            boolean schedulingResource,
            String note) {
        findScheduleOrThrow(scheduleId);
        ScheduleTeam team = findTeamForSchedule(teamId, scheduleId);
        boolean assigned = schedulingResource
                ? team.getResources().stream().anyMatch(
                        resource -> resource.getId().equals(assignmentId))
                : team.getEmployees().stream().anyMatch(
                        employee -> employee.getId().equals(assignmentId));
        if (!assigned) {
            throw new IllegalArgumentException(
                    "The selected assistant is not assigned to this team.");
        }

        String normalized = note == null ? "" : note.trim().replaceAll("\\s+", " ");
        if (normalized.length() > 40) {
            throw new IllegalArgumentException(
                    "Assistant notes must be 40 characters or fewer.");
        }

        String assignmentKey =
                (schedulingResource ? "resource:" : "employee:") + assignmentId;
        if (normalized.isBlank()) {
            team.getAssignmentNotes().remove(assignmentKey);
        } else {
            team.getAssignmentNotes().put(assignmentKey, normalized);
        }
        scheduleTeamRepository.save(team);
        return scheduleMapper.mapToScheduleDto(findScheduleOrThrow(scheduleId));
    }

    /** Marks a schedule as published */
    @Override
    public ScheduleDto publishSchedule(Long id) {
        Schedule schedule = findScheduleOrThrow(id);
        schedule.setPublished(true);
        return scheduleMapper.mapToScheduleDto(scheduleRepository.save(schedule));
    }

    /** Fetches a schedule or throws ResourceNotFoundException */
    private Schedule findScheduleOrThrow(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Schedule not found with id: " + id));
    }

    private ScheduleTeam findTeamForSchedule(Long teamId, Long scheduleId) {
        ScheduleTeam team = scheduleTeamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Team not found with id: " + teamId));
        if (!team.getSchedule().getId().equals(scheduleId)) {
            throw new IllegalArgumentException(
                    "Team " + teamId + " does not belong to schedule " + scheduleId);
        }
        return team;
    }

    /** Returns all published schedules assigned to a given employee by name */
    @Override
    @Transactional(readOnly = true)
    public List<ScheduleDto> getSchedulesByEmployeeName(String employeeName) {
        return scheduleRepository.findPublishedSchedulesByEmployeeName(employeeName).stream()
                .map(scheduleMapper::mapToScheduleDto)
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Sends a schedule notification to an employee if the schedule's
     * parent calendar is currently published.
     *
     * @param schedule the schedule that was edited
     * @param employee the employee who was added or removed
     * @param type     SCHEDULE_UPDATE
     */
    private void notifyEmployeeIfPublished(Schedule schedule,
                                           Employee employee,
                                           NotificationType type) {
        try {
            Calendar calendar = schedule.getCalendar();
            if (calendar == null || !Boolean.TRUE.equals(calendar.getPublished())) return;
            if (employee.getUser() == null) return;

            notificationService.notifyEmployeeOfSchedule(
                    employee.getUser().getId(),
                    type,
                    calendar.getMonth(),
                    calendar.getId());
        } catch (Exception e) {
            System.err.println("Failed to send schedule notification to employee "
                    + employee.getId() + ": " + e.getMessage());
        }
    }
}
