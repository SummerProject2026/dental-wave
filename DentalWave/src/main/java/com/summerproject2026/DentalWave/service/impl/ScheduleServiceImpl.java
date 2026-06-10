package com.summerproject2026.DentalWave.service.impl;

import com.summerproject2026.DentalWave.dto.ScheduleDto;
import com.summerproject2026.DentalWave.exception.ResourceNotFoundException;
import com.summerproject2026.DentalWave.mapper.ScheduleMapper;
import com.summerproject2026.DentalWave.entity.Schedule;
import com.summerproject2026.DentalWave.repository.EmployeeRepository;
import com.summerproject2026.DentalWave.repository.ScheduleRepository;
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
        // Verify employee exists
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

    /** Team assignment now handled by ScheduleTeamService */
    @Override
    public ScheduleDto assignEmployeeToTeam(Long scheduleId, Long userId, Long employeeId) {
        return scheduleMapper.mapToScheduleDto(findScheduleOrThrow(scheduleId));
    }

    /** Team removal now handled by ScheduleTeamService */
    @Override
    public ScheduleDto removeEmployeeFromTeam(Long scheduleId, Long userId, Long employeeId) {
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