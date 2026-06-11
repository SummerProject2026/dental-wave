package com.summerproject2026.DentalWave.service.impl;

import com.summerproject2026.DentalWave.dto.ScheduleTeamDto;
import com.summerproject2026.DentalWave.entity.Employee;
import com.summerproject2026.DentalWave.entity.Schedule;
import com.summerproject2026.DentalWave.entity.ScheduleTeam;
import com.summerproject2026.DentalWave.exception.ResourceNotFoundException;
import com.summerproject2026.DentalWave.mapper.ScheduleTeamMapper;
import com.summerproject2026.DentalWave.repository.EmployeeRepository;
import com.summerproject2026.DentalWave.repository.ScheduleRepository;
import com.summerproject2026.DentalWave.repository.ScheduleTeamRepository;
import com.summerproject2026.DentalWave.service.ScheduleTeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of ScheduleTeamService.
 */
@Service
@Transactional
public class ScheduleTeamServiceImpl implements ScheduleTeamService {

    private final ScheduleTeamRepository scheduleTeamRepository;
    private final ScheduleRepository scheduleRepository;
    private final EmployeeRepository employeeRepository;
    private final ScheduleTeamMapper scheduleTeamMapper;

    @Autowired
    public ScheduleTeamServiceImpl(ScheduleTeamRepository scheduleTeamRepository,
                                   ScheduleRepository scheduleRepository,
                                   EmployeeRepository employeeRepository,
                                   ScheduleTeamMapper scheduleTeamMapper) {
        this.scheduleTeamRepository = scheduleTeamRepository;
        this.scheduleRepository = scheduleRepository;
        this.employeeRepository = employeeRepository;
        this.scheduleTeamMapper = scheduleTeamMapper;
    }

    /**
     * Creates a new team within a schedule.
     */
    @Override
    public ScheduleTeamDto createTeam(ScheduleTeamDto scheduleTeamDto) {
        // Fetch the full schedule entity
        Schedule schedule = scheduleRepository.findById(scheduleTeamDto.getScheduleId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Schedule not found with id: " + scheduleTeamDto.getScheduleId()));

        ScheduleTeam scheduleTeam = scheduleTeamMapper.mapToScheduleTeam(scheduleTeamDto);

        // Replace stub schedule with managed entity
        scheduleTeam.setSchedule(schedule);

        return scheduleTeamMapper.mapToScheduleTeamDto(
                scheduleTeamRepository.save(scheduleTeam));
    }

    /**
     * Retrieves a team by its ID.
     */
    @Override
    @Transactional(readOnly = true)
    public ScheduleTeamDto getTeamById(Long id) {
        return scheduleTeamMapper.mapToScheduleTeamDto(findTeamOrThrow(id));
    }

    /**
     * Retrieves all teams belonging to a schedule.
     */
    @Override
    @Transactional(readOnly = true)
    public List<ScheduleTeamDto> getTeamsBySchedule(Long scheduleId) {
        // Confirm schedule exists
        if (!scheduleRepository.existsById(scheduleId)) {
            throw new ResourceNotFoundException(
                    "Schedule not found with id: " + scheduleId);
        }

        return scheduleTeamRepository.findByScheduleId(scheduleId)
                .stream()
                .map(scheduleTeamMapper::mapToScheduleTeamDto)
                .collect(Collectors.toList());
    }

    /**
     * Adds an employee to a team.
     */
    @Override
    public ScheduleTeamDto addEmployeeToTeam(Long teamId, Long employeeId) {
        ScheduleTeam team = findTeamOrThrow(teamId);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found with id: " + employeeId));

        // Add employee if not already in team
        if (!team.getEmployees().contains(employee)) {
            team.getEmployees().add(employee);
        }

        return scheduleTeamMapper.mapToScheduleTeamDto(
                scheduleTeamRepository.save(team));
    }

    /**
     * Removes an employee from a team.
     */
    @Override
    public ScheduleTeamDto removeEmployeeFromTeam(Long teamId, Long employeeId) {
        ScheduleTeam team = findTeamOrThrow(teamId);

        team.getEmployees().removeIf(emp -> emp.getId().equals(employeeId));

        return scheduleTeamMapper.mapToScheduleTeamDto(
                scheduleTeamRepository.save(team));
    }

    /**
     * Updates the name of a team.
     */
    @Override
    public ScheduleTeamDto updateTeamName(Long id, String name) {
        ScheduleTeam team = findTeamOrThrow(id);
        team.setName(name);
        return scheduleTeamMapper.mapToScheduleTeamDto(
                scheduleTeamRepository.save(team));
    }

    /**
     * Deletes a team by its ID.
     */
    @Override
    public void deleteTeam(Long id) {
        scheduleTeamRepository.delete(findTeamOrThrow(id));
    }

    /**
     * Fetches a team or throws ResourceNotFoundException.
     */
    private ScheduleTeam findTeamOrThrow(Long id) {
        return scheduleTeamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ScheduleTeam not found with id: " + id));
    }
}