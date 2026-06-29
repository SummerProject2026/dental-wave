package com.summerproject2026.DentalWave.service.impl;

import com.summerproject2026.DentalWave.dto.ScheduleDto;
import com.summerproject2026.DentalWave.entity.Employee;
import com.summerproject2026.DentalWave.exception.ResourceNotFoundException;
import com.summerproject2026.DentalWave.mapper.ScheduleMapper;
import com.summerproject2026.DentalWave.entity.Schedule;
import com.summerproject2026.DentalWave.entity.ScheduleTeam;
import com.summerproject2026.DentalWave.repository.EmployeeRepository;
import com.summerproject2026.DentalWave.repository.ScheduleRepository;
import com.summerproject2026.DentalWave.repository.ScheduleTeamRepository;
import com.summerproject2026.DentalWave.repository.UserRepository;
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
 */
@Service
@Transactional
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final ScheduleTeamRepository scheduleTeamRepository;
    private final ScheduleMapper scheduleMapper;

    @Autowired
    public ScheduleServiceImpl(ScheduleRepository scheduleRepository,
                               UserRepository userRepository,
                               EmployeeRepository employeeRepository,
                               ScheduleTeamRepository scheduleTeamRepository,
                               ScheduleMapper scheduleMapper) {
        this.scheduleRepository = scheduleRepository;
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.scheduleTeamRepository = scheduleTeamRepository;
        this.scheduleMapper = scheduleMapper;
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
     *
     * <p>The {@code teamId} parameter is passed through the existing
     * {@code userId} controller path variable for route compatibility.</p>
     *
     * @param scheduleId the schedule the team belongs to
     * @param teamId     the id of the ScheduleTeam to add the employee to
     * @param employeeId the employee to add
     * @return the updated ScheduleDto reflecting the new assignment
     * @throws ResourceNotFoundException if the schedule, team, or employee is not found
     * @throws IllegalArgumentException  if the team does not belong to the given schedule
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

        // Avoid adding the same employee twice
        boolean alreadyOnTeam = team.getEmployees().stream()
                .anyMatch(e -> e.getId().equals(employeeId));

        if (!alreadyOnTeam) {
            team.getEmployees().add(employee);
            scheduleTeamRepository.save(team);
        }

        return scheduleMapper.mapToScheduleDto(findScheduleOrThrow(scheduleId));
    }

    /**
     * Removes an employee from a team within a schedule.
     *
     * <p>The {@code teamId} parameter is passed through the existing
     * {@code userId} controller path variable for route compatibility.</p>
     *
     * @param scheduleId the schedule the team belongs to
     * @param teamId     the id of the ScheduleTeam to remove the employee from
     * @param employeeId the employee to remove
     * @return the updated ScheduleDto reflecting the removal
     * @throws ResourceNotFoundException if the schedule or team is not found
     * @throws IllegalArgumentException  if the team does not belong to the given schedule
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

        team.getEmployees().removeIf(e -> e.getId().equals(employeeId));
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

    /** Returns all published schedules assigned to a given employee by name */
    @Override
    @Transactional(readOnly = true)
    public List<ScheduleDto> getSchedulesByEmployeeName(String employeeName) {
        return scheduleRepository.findPublishedSchedulesByEmployeeName(employeeName).stream()
                .map(scheduleMapper::mapToScheduleDto)
                .collect(Collectors.toList());
    }
}