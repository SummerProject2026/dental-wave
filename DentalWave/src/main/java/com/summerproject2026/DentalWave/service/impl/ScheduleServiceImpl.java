package com.dentalwave.service.impl;

import com.dentalwave.dto.ScheduleDto;
import com.dentalwave.exception.ResourceNotFoundException;
import com.dentalwave.mapper.ScheduleMapper;
import com.dentalwave.model.Employee;
import com.dentalwave.model.Schedule;
import com.dentalwave.model.User;
import com.dentalwave.repository.EmployeeRepository;
import com.dentalwave.repository.ScheduleRepository;
import com.dentalwave.repository.UserRepository;
import com.dentalwave.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of ScheduleService.
 * Manages individual schedule records, team assignments, and the publish action.
 */
@Service
@Transactional
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final ScheduleMapper scheduleMapper;

    @Autowired
    public ScheduleServiceImpl(ScheduleRepository scheduleRepository,
                                UserRepository userRepository,
                                EmployeeRepository employeeRepository,
                                ScheduleMapper scheduleMapper) {
        this.scheduleRepository = scheduleRepository;
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.scheduleMapper = scheduleMapper;
    }

    // -------------------------------------------------------------------------
    // Create
    // -------------------------------------------------------------------------

    /**
     * Persists a new schedule from the provided DTO.
     * Team map entries (user stubs) are replaced with managed entities.
     */
    @Override
    public ScheduleDto createSchedule(ScheduleDto scheduleDto) {
        Schedule schedule = scheduleMapper.mapToSchedule(scheduleDto);
        hydrateTeamMap(schedule);
        return scheduleMapper.mapToScheduleDto(scheduleRepository.save(schedule));
    }

    // -------------------------------------------------------------------------
    // Read
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------

    /**
     * Updates scalar fields (date, times, notes) on an existing schedule.
     * Does not overwrite the teams map — use assignEmployeeToTeam / removeEmployeeFromTeam.
     */
    @Override
    public ScheduleDto updateSchedule(Long id, ScheduleDto scheduleDto) {
        Schedule existing = findScheduleOrThrow(id);

        existing.setDate(scheduleDto.getDate());
        existing.setStartTime(scheduleDto.getStartTime());
        existing.setEndTime(scheduleDto.getEndTime());
        existing.setNotes(scheduleDto.getNotes());

        return scheduleMapper.mapToScheduleDto(scheduleRepository.save(existing));
    }

    // -------------------------------------------------------------------------
    // Delete
    // -------------------------------------------------------------------------

    /** Deletes a schedule by ID */
    @Override
    public void deleteSchedule(Long id) {
        scheduleRepository.delete(findScheduleOrThrow(id));
    }

    // -------------------------------------------------------------------------
    // Team assignment
    // -------------------------------------------------------------------------

    /**
     * Adds an employee to the team of a specific team lead on this schedule.
     * Creates the team lead entry if it does not exist yet.
     *
     * @param scheduleId the schedule to update
     * @param userId     the team lead / dentist user ID
     * @param employeeId the employee to add
     */
    @Override
    public ScheduleDto assignEmployeeToTeam(Long scheduleId, Long userId, Long employeeId) {
        Schedule schedule = findScheduleOrThrow(scheduleId);

        // Fetch managed entities to avoid detached-entity exceptions
        User teamLead = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        // Add employee to the team lead's list, creating the list if absent
        schedule.getTeams()
                .computeIfAbsent(teamLead, k -> new ArrayList<>())
                .add(employee);

        return scheduleMapper.mapToScheduleDto(scheduleRepository.save(schedule));
    }

    /**
     * Removes an employee from the team of a specific team lead on this schedule.
     * Does nothing (silently) if the employee was not in that team.
     *
     * @param scheduleId the schedule to update
     * @param userId     the team lead / dentist user ID
     * @param employeeId the employee to remove
     */
    @Override
    public ScheduleDto removeEmployeeFromTeam(Long scheduleId, Long userId, Long employeeId) {
        Schedule schedule = findScheduleOrThrow(scheduleId);

        User teamLead = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Remove the employee from the team list if present
        List<Employee> team = schedule.getTeams().get(teamLead);
        if (team != null) {
            team.removeIf(emp -> emp.getId().equals(employeeId));
            // Clean up empty team entries
            if (team.isEmpty()) {
                schedule.getTeams().remove(teamLead);
            }
        }

        return scheduleMapper.mapToScheduleDto(scheduleRepository.save(schedule));
    }

    // -------------------------------------------------------------------------
    // Publish
    // -------------------------------------------------------------------------

    /**
     * Marks a schedule as published.
     * NOTE: Ensure the Schedule entity has a 'published' Boolean field.
     * This method assumes it exists and follows the same pattern as Calendar.
     */
    @Override
    public ScheduleDto publishSchedule(Long id) {
        Schedule schedule = findScheduleOrThrow(id);
        // schedule.setPublished(true); // Uncomment once field is added to Schedule entity
        return scheduleMapper.mapToScheduleDto(scheduleRepository.save(schedule));
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /** Fetches a schedule or throws ResourceNotFoundException */
    private Schedule findScheduleOrThrow(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Schedule not found with id: " + id));
    }

    /**
     * Replaces stub User keys in the teams map with fully managed entities
     * fetched from the database. Called when creating a new schedule.
     */
    private void hydrateTeamMap(Schedule schedule) {
        if (schedule.getTeams() == null || schedule.getTeams().isEmpty()) return;

        // We must rebuild the map because the keys (User stubs) have no persistence context
        java.util.Map<User, List<Employee>> hydratedTeams = new java.util.HashMap<>();

        for (java.util.Map.Entry<User, List<Employee>> entry : schedule.getTeams().entrySet()) {
            Long userId = entry.getKey().getId();
            User managedUser = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

            // Hydrate employee stubs as well
            List<Employee> managedEmployees = new ArrayList<>();
            for (Employee empStub : entry.getValue()) {
                Employee managed = employeeRepository.findById(empStub.getId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Employee not found with id: " + empStub.getId()));
                managedEmployees.add(managed);
            }

            hydratedTeams.put(managedUser, managedEmployees);
        }

        schedule.setTeams(hydratedTeams);
    }
}
