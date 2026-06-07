package com.dentalwave.service.impl;

import com.dentalwave.dto.CalendarDto;
import com.dentalwave.dto.ScheduleDto;
import com.dentalwave.exception.ResourceNotFoundException;
import com.dentalwave.mapper.CalendarMapper;
import com.dentalwave.mapper.ScheduleMapper;
import com.dentalwave.model.Calendar;
import com.dentalwave.model.Schedule;
import com.dentalwave.model.User;
import com.dentalwave.repository.CalendarRepository;
import com.dentalwave.repository.ScheduleRepository;
import com.dentalwave.repository.UserRepository;
import com.dentalwave.service.CalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of CalendarService.
 * Handles all calendar business logic including creation, updates,
 * publish/unpublish lifecycle, and nested schedule management.
 */
@Service
@Transactional
public class CalendarServiceImpl implements CalendarService {

    private final CalendarRepository calendarRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final CalendarMapper calendarMapper;
    private final ScheduleMapper scheduleMapper;

    @Autowired
    public CalendarServiceImpl(CalendarRepository calendarRepository,
                               ScheduleRepository scheduleRepository,
                               UserRepository userRepository,
                               CalendarMapper calendarMapper,
                               ScheduleMapper scheduleMapper) {
        this.calendarRepository = calendarRepository;
        this.scheduleRepository = scheduleRepository;
        this.userRepository = userRepository;
        this.calendarMapper = calendarMapper;
        this.scheduleMapper = scheduleMapper;
    }

    // -------------------------------------------------------------------------
    // Create
    // -------------------------------------------------------------------------

    /**
     * Creates and persists a new calendar.
     * Resolves the createdBy User from the database using the DTO's createdById.
     */
    @Override
    public CalendarDto createCalendar(CalendarDto calendarDto) {
        // Map DTO → entity (createdBy will be a stub at this point)
        Calendar calendar = calendarMapper.mapToCalendar(calendarDto);

        // Replace stub createdBy with a fully managed User entity
        if (calendarDto.getCreatedById() != null) {
            User creator = userRepository.findById(calendarDto.getCreatedById())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "User not found with id: " + calendarDto.getCreatedById()));
            calendar.setCreatedBy(creator);
        }

        Calendar saved = calendarRepository.save(calendar);
        return calendarMapper.mapToCalendarDto(saved);
    }

    // -------------------------------------------------------------------------
    // Read
    // -------------------------------------------------------------------------

    /** Fetches a single calendar by ID, throwing if absent */
    @Override
    @Transactional(readOnly = true)
    public CalendarDto getCalendarById(Long id) {
        Calendar calendar = findCalendarOrThrow(id);
        return calendarMapper.mapToCalendarDto(calendar);
    }

    /** Returns all calendars in the system */
    @Override
    @Transactional(readOnly = true)
    public List<CalendarDto> getAllCalendars() {
        return calendarRepository.findAll().stream()
                .map(calendarMapper::mapToCalendarDto)
                .collect(Collectors.toList());
    }

    /** Returns all calendars matching a month label (e.g. "June 2025") */
    @Override
    @Transactional(readOnly = true)
    public List<CalendarDto> getCalendarsByMonth(String month) {
        return calendarRepository.findByMonth(month).stream()
                .map(calendarMapper::mapToCalendarDto)
                .collect(Collectors.toList());
    }

    /** Returns all published calendars */
    @Override
    @Transactional(readOnly = true)
    public List<CalendarDto> getPublishedCalendars() {
        return calendarRepository.findByPublishedTrue().stream()
                .map(calendarMapper::mapToCalendarDto)
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------

    /**
     * Updates the scalar fields of an existing calendar.
     * Does not replace the schedules list on update (manage via addSchedule/removeSchedule).
     */
    @Override
    public CalendarDto updateCalendar(Long id, CalendarDto calendarDto) {
        Calendar existing = findCalendarOrThrow(id);

        existing.setMonth(calendarDto.getMonth());
        existing.setStartCalendarDate(calendarDto.getStartCalendarDate());
        existing.setEndCalendarDate(calendarDto.getEndCalendarDate());

        // Only update published flag if explicitly provided
        if (calendarDto.getPublished() != null) {
            existing.setPublished(calendarDto.getPublished());
        }

        // Update creator if a new one is provided
        if (calendarDto.getCreatedById() != null) {
            User creator = userRepository.findById(calendarDto.getCreatedById())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "User not found with id: " + calendarDto.getCreatedById()));
            existing.setCreatedBy(creator);
        }

        Calendar updated = calendarRepository.save(existing);
        return calendarMapper.mapToCalendarDto(updated);
    }

    // -------------------------------------------------------------------------
    // Delete
    // -------------------------------------------------------------------------

    /** Deletes a calendar and its cascaded schedules */
    @Override
    public void deleteCalendar(Long id) {
        Calendar calendar = findCalendarOrThrow(id);
        calendarRepository.delete(calendar);
    }

    // -------------------------------------------------------------------------
    // Publish / Unpublish lifecycle
    // -------------------------------------------------------------------------

    /** Marks the calendar as published */
    @Override
    public CalendarDto publishCalendar(Long id) {
        Calendar calendar = findCalendarOrThrow(id);
        calendar.setPublished(true);
        return calendarMapper.mapToCalendarDto(calendarRepository.save(calendar));
    }

    /** Reverts the calendar to draft (unpublished) */
    @Override
    public CalendarDto unpublishCalendar(Long id) {
        Calendar calendar = findCalendarOrThrow(id);
        calendar.setPublished(false);
        return calendarMapper.mapToCalendarDto(calendarRepository.save(calendar));
    }

    // -------------------------------------------------------------------------
    // Nested schedule management
    // -------------------------------------------------------------------------

    /**
     * Creates a new schedule and links it to the specified calendar.
     * Uses Calendar#addSchedule to keep the bidirectional relationship consistent.
     */
    @Override
    public ScheduleDto addSchedule(Long calendarId, ScheduleDto scheduleDto) {
        Calendar calendar = findCalendarOrThrow(calendarId);

        // Map DTO → entity; calendar reference will be set via addSchedule()
        Schedule schedule = scheduleMapper.mapToSchedule(scheduleDto);
        calendar.addSchedule(schedule);

        // Save the calendar so the cascade persists the new schedule
        calendarRepository.save(calendar);

        // Return the newly persisted schedule (it now has a generated ID)
        return scheduleMapper.mapToScheduleDto(schedule);
    }

    /**
     * Removes a schedule from a calendar and deletes it.
     * Validates that the schedule actually belongs to the given calendar.
     */
    @Override
    public void removeSchedule(Long calendarId, Long scheduleId) {
        Calendar calendar = findCalendarOrThrow(calendarId);
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Schedule not found with id: " + scheduleId));

        // Guard: ensure the schedule belongs to this calendar
        if (!schedule.getCalendar().getId().equals(calendarId)) {
            throw new IllegalArgumentException(
                    "Schedule " + scheduleId + " does not belong to calendar " + calendarId);
        }

        calendar.removeSchedule(schedule);
        calendarRepository.save(calendar);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /** Convenience method — fetches a calendar or throws ResourceNotFoundException */
    private Calendar findCalendarOrThrow(Long id) {
        return calendarRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Calendar not found with id: " + id));
    }
}
