package com.summerproject2026.DentalWave.service.impl;

import com.summerproject2026.DentalWave.dto.CalendarDto;
import com.summerproject2026.DentalWave.dto.ScheduleDto;
import com.summerproject2026.DentalWave.exception.ResourceNotFoundException;
import com.summerproject2026.DentalWave.mapper.CalendarMapper;
import com.summerproject2026.DentalWave.mapper.ScheduleMapper;
import com.summerproject2026.DentalWave.entity.Calendar;
import com.summerproject2026.DentalWave.entity.Employee;
import com.summerproject2026.DentalWave.entity.Office;
import com.summerproject2026.DentalWave.entity.Schedule;
import com.summerproject2026.DentalWave.entity.ScheduleTeam;
import com.summerproject2026.DentalWave.entity.User;
import com.summerproject2026.DentalWave.enums.NotificationType;
import com.summerproject2026.DentalWave.repository.CalendarRepository;
import com.summerproject2026.DentalWave.repository.EmployeeRepository;
import com.summerproject2026.DentalWave.repository.OfficeRepository;
import com.summerproject2026.DentalWave.repository.ScheduleRepository;
import com.summerproject2026.DentalWave.repository.ScheduleTeamRepository;
import com.summerproject2026.DentalWave.repository.UserRepository;
import com.summerproject2026.DentalWave.service.CalendarService;
import com.summerproject2026.DentalWave.service.NotificationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of CalendarService.
 * Handles all calendar business logic including creation, updates,
 * publish/unpublish lifecycle, nested schedule management,
 * auto-generation of draft calendars with role-based team assignment,
 * bulk scheduling/unscheduling, and UC12 employee schedule notifications.
 */
@Service
@Transactional
public class CalendarServiceImpl implements CalendarService {

    private final CalendarRepository calendarRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleTeamRepository scheduleTeamRepository;
    private final UserRepository userRepository;
    private final OfficeRepository officeRepository;
    private final EmployeeRepository employeeRepository;
    private final CalendarMapper calendarMapper;
    private final ScheduleMapper scheduleMapper;
    private final NotificationService notificationService;

    @Autowired
    public CalendarServiceImpl(CalendarRepository calendarRepository,
                               ScheduleRepository scheduleRepository,
                               ScheduleTeamRepository scheduleTeamRepository,
                               UserRepository userRepository,
                               OfficeRepository officeRepository,
                               EmployeeRepository employeeRepository,
                               CalendarMapper calendarMapper,
                               ScheduleMapper scheduleMapper,
                               NotificationService notificationService) {
        this.calendarRepository = calendarRepository;
        this.scheduleRepository = scheduleRepository;
        this.scheduleTeamRepository = scheduleTeamRepository;
        this.userRepository = userRepository;
        this.officeRepository = officeRepository;
        this.employeeRepository = employeeRepository;
        this.calendarMapper = calendarMapper;
        this.scheduleMapper = scheduleMapper;
        this.notificationService = notificationService;
    }

    // -------------------------------------------------------------------------
    // Create
    // -------------------------------------------------------------------------

    @Override
    public CalendarDto createCalendar(CalendarDto calendarDto) {
        Calendar calendar = calendarMapper.mapToCalendar(calendarDto);

        if (calendarDto.getCreatedById() != null) {
            User creator = userRepository.findById(calendarDto.getCreatedById())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "User not found with id: " + calendarDto.getCreatedById()));
            calendar.setCreatedBy(creator);
        }

        if (calendarDto.getOfficeId() != null) {
            Office office = officeRepository.findById(calendarDto.getOfficeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Office not found with id: " + calendarDto.getOfficeId()));
            calendar.setOffice(office);
        }

        Calendar saved = calendarRepository.save(calendar);
        return calendarMapper.mapToCalendarDto(saved);
    }

    // -------------------------------------------------------------------------
    // Read
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public CalendarDto getCalendarById(Long id) {
        Calendar calendar = findCalendarOrThrow(id);
        return calendarMapper.mapToCalendarDto(calendar);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CalendarDto> getAllCalendars() {
        return calendarRepository.findAll().stream()
                .map(calendarMapper::mapToCalendarDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CalendarDto> getCalendarsByMonth(String month) {
        return calendarRepository.findByMonth(month).stream()
                .map(calendarMapper::mapToCalendarDto)
                .collect(Collectors.toList());
    }

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

    @Override
    public CalendarDto updateCalendar(Long id, CalendarDto calendarDto) {
        Calendar existing = findCalendarOrThrow(id);

        existing.setMonth(calendarDto.getMonth());
        existing.setStartCalendarDate(calendarDto.getStartCalendarDate());
        existing.setEndCalendarDate(calendarDto.getEndCalendarDate());

        if (calendarDto.getPublished() != null) {
            existing.setPublished(calendarDto.getPublished());
        }

        if (calendarDto.getCreatedById() != null) {
            User creator = userRepository.findById(calendarDto.getCreatedById())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "User not found with id: " + calendarDto.getCreatedById()));
            existing.setCreatedBy(creator);
        }

        if (calendarDto.getOfficeId() != null) {
            Office office = officeRepository.findById(calendarDto.getOfficeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Office not found with id: " + calendarDto.getOfficeId()));
            existing.setOffice(office);
        }

        Calendar updated = calendarRepository.save(existing);
        return calendarMapper.mapToCalendarDto(updated);
    }

    // -------------------------------------------------------------------------
    // Delete
    // -------------------------------------------------------------------------

    @Override
    public void deleteCalendar(Long id) {
        Calendar calendar = findCalendarOrThrow(id);
        calendarRepository.delete(calendar);
    }

    // -------------------------------------------------------------------------
    // Publish / Unpublish lifecycle
    // -------------------------------------------------------------------------

    /**
     * Publishes a calendar and sends NEW_SCHEDULE notifications to every
     * employee assigned to any team in the calendar (UC12).
     */
    @Override
    public CalendarDto publishCalendar(Long id) {
        Calendar calendar = findCalendarOrThrow(id);
        calendar.setPublished(true);
        for (Schedule schedule : calendar.getSchedules()) {
            schedule.setPublished(true);
        }
        Calendar saved = calendarRepository.save(calendar);

        // UC12 — notify every assigned employee that their schedule is available
        notifyAssignedEmployees(saved, NotificationType.NEW_SCHEDULE);

        return calendarMapper.mapToCalendarDto(saved);
    }

    @Override
    public CalendarDto unpublishCalendar(Long id) {
        Calendar calendar = findCalendarOrThrow(id);
        calendar.setPublished(false);
        for (Schedule schedule : calendar.getSchedules()) {
            schedule.setPublished(false);
        }
        return calendarMapper.mapToCalendarDto(calendarRepository.save(calendar));
    }

    // -------------------------------------------------------------------------
    // Nested schedule management
    // -------------------------------------------------------------------------

    @Override
    public ScheduleDto addSchedule(Long calendarId, ScheduleDto scheduleDto) {
        Calendar calendar = findCalendarOrThrow(calendarId);
        Schedule schedule = scheduleMapper.mapToSchedule(scheduleDto);
        calendar.addSchedule(schedule);
        calendarRepository.save(calendar);
        return scheduleMapper.mapToScheduleDto(schedule);
    }

    @Override
    public void removeSchedule(Long calendarId, Long scheduleId) {
        Calendar calendar = findCalendarOrThrow(calendarId);
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Schedule not found with id: " + scheduleId));

        if (!schedule.getCalendar().getId().equals(calendarId)) {
            throw new IllegalArgumentException(
                    "Schedule " + scheduleId + " does not belong to calendar " + calendarId);
        }

        calendar.removeSchedule(schedule);
        calendarRepository.save(calendar);
    }

    // -------------------------------------------------------------------------
    // Auto-generation with role-based team assignment
    // -------------------------------------------------------------------------

    @Override
    public CalendarDto generateCalendar(CalendarDto calendarDto) {
        Office office = officeRepository.findById(calendarDto.getOfficeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Office not found with id: " + calendarDto.getOfficeId()));

        // Prevent duplicate calendars for the same office and month
        boolean alreadyExists = calendarRepository.findByMonth(calendarDto.getMonth()).stream()
                .anyMatch(cal -> cal.getOffice() != null
                        && cal.getOffice().getId().equals(office.getId()));

        if (alreadyExists) {
            throw new IllegalStateException(
                    "A calendar already exists for " + office.getName()
                            + " in " + calendarDto.getMonth() + ".");
        }

        User creator = userRepository.findById(calendarDto.getCreatedById())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + calendarDto.getCreatedById()));

        Calendar calendar = new Calendar();
        calendar.setMonth(calendarDto.getMonth());
        calendar.setStartCalendarDate(calendarDto.getStartCalendarDate());
        calendar.setEndCalendarDate(calendarDto.getEndCalendarDate());
        calendar.setPublished(false);
        calendar.setCreatedBy(creator);
        calendar.setOffice(office);

        List<Employee> doctors = employeeRepository
                .findByOfficeIdAndPosition(office.getId(), "Doctor");
        List<Employee> tcs = employeeRepository
                .findByOfficeIdAndPosition(office.getId(), "TC");
        List<Employee> assistants = employeeRepository
                .findByOfficeIdAndPosition(office.getId(), "Assistant");

        LocalDate current = calendarDto.getStartCalendarDate();
        LocalDate end = calendarDto.getEndCalendarDate();

        while (!current.isAfter(end)) {
            DayOfWeek dayOfWeek = current.getDayOfWeek();
            if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
                Schedule schedule = buildScheduleForDay(current, doctors, tcs, assistants);
                calendar.addSchedule(schedule);
            }
            current = current.plusDays(1);
        }

        Calendar saved = calendarRepository.save(calendar);
        return calendarMapper.mapToCalendarDto(saved);
    }

    private Schedule buildScheduleForDay(LocalDate date,
                                         List<Employee> doctors,
                                         List<Employee> tcs,
                                         List<Employee> assistants) {
        Schedule schedule = new Schedule();
        schedule.setDate(date);
        schedule.setStartTime(LocalTime.of(8, 0));
        schedule.setEndTime(LocalTime.of(17, 0));
        schedule.setPublished(false);

        int teamCount = Math.min(doctors.size(), tcs.size());

        List<ScheduleTeam> teams = new ArrayList<>();
        for (int i = 0; i < teamCount; i++) {
            ScheduleTeam team = new ScheduleTeam();
            team.setName("Team " + (i + 1));
            team.setSchedule(schedule);

            List<Employee> teamEmployees = new ArrayList<>();
            teamEmployees.add(doctors.get(i));
            teamEmployees.add(tcs.get(i));
            team.setEmployees(teamEmployees);

            teams.add(team);
        }

        if (!teams.isEmpty()) {
            for (int i = 0; i < assistants.size(); i++) {
                ScheduleTeam team = teams.get(i % teams.size());
                team.getEmployees().add(assistants.get(i));
            }
        }

        schedule.setTeams(teams);
        return schedule;
    }

    // -------------------------------------------------------------------------
    // Bulk scheduling / unscheduling
    // -------------------------------------------------------------------------

    @Override
    public CalendarDto scheduleEmployeeAcrossCalendar(Long calendarId, Long employeeId) {
        Calendar calendar = findCalendarOrThrow(calendarId);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found with id: " + employeeId));

        for (Schedule schedule : calendar.getSchedules()) {
            List<ScheduleTeam> teams = schedule.getTeams();
            if (teams == null || teams.isEmpty()) continue;

            boolean alreadyAssigned = teams.stream()
                    .anyMatch(team -> team.getEmployees().stream()
                            .anyMatch(e -> e.getId().equals(employeeId)));
            if (alreadyAssigned) continue;

            ScheduleTeam smallestTeam = teams.stream()
                    .min(Comparator.comparingInt(team -> team.getEmployees().size()))
                    .orElse(null);

            if (smallestTeam != null) {
                smallestTeam.getEmployees().add(employee);
            }
        }

        Calendar saved = calendarRepository.save(calendar);

        // If calendar is already published, notify the newly scheduled employee
        if (Boolean.TRUE.equals(saved.getPublished()) && employee.getUser() != null) {
            try {
                notificationService.notifyEmployeeOfSchedule(
                        employee.getUser().getId(),
                        NotificationType.SCHEDULE_UPDATE,
                        saved.getMonth(),
                        saved.getId());
            } catch (Exception e) {
                System.err.println("Failed to notify employee " + employeeId
                        + " of schedule update: " + e.getMessage());
            }
        }

        return calendarMapper.mapToCalendarDto(saved);
    }

    @Override
    public CalendarDto removeEmployeeFromCalendar(Long calendarId, Long employeeId) {
        Calendar calendar = findCalendarOrThrow(calendarId);

        for (Schedule schedule : calendar.getSchedules()) {
            if (schedule.getTeams() == null) continue;
            for (ScheduleTeam team : schedule.getTeams()) {
                team.getEmployees().removeIf(e -> e.getId().equals(employeeId));
            }
        }

        Calendar saved = calendarRepository.save(calendar);

        // If calendar is published, notify the removed employee
        if (Boolean.TRUE.equals(saved.getPublished())) {
            Employee employee = employeeRepository.findById(employeeId).orElse(null);
            if (employee != null && employee.getUser() != null) {
                try {
                    notificationService.notifyEmployeeOfSchedule(
                            employee.getUser().getId(),
                            NotificationType.SCHEDULE_UPDATE,
                            saved.getMonth(),
                            saved.getId());
                } catch (Exception e) {
                    System.err.println("Failed to notify employee " + employeeId
                            + " of schedule update: " + e.getMessage());
                }
            }
        }

        return calendarMapper.mapToCalendarDto(saved);
    }

    // -------------------------------------------------------------------------
    // Remove employee from schedule on specific date range
    // -------------------------------------------------------------------------

    @Override
    public void removeEmployeeFromScheduleOnDates(Long officeId, Long employeeId,
                                                  LocalDate startDate, LocalDate endDate) {
        List<Calendar> officeCalendars = calendarRepository.findAll().stream()
                .filter(cal -> cal.getOffice() != null
                        && cal.getOffice().getId().equals(officeId))
                .collect(Collectors.toList());

        for (Calendar calendar : officeCalendars) {
            boolean modified = false;

            for (Schedule schedule : calendar.getSchedules()) {
                if (schedule.getDate() == null) continue;
                if (schedule.getDate().isBefore(startDate)
                        || schedule.getDate().isAfter(endDate)) continue;

                if (schedule.getTeams() == null) continue;
                for (ScheduleTeam team : schedule.getTeams()) {
                    boolean removed = team.getEmployees()
                            .removeIf(e -> e.getId().equals(employeeId));
                    if (removed) modified = true;
                }
            }

            if (modified) {
                calendarRepository.save(calendar);

                // Notify the employee if calendar is published
                if (Boolean.TRUE.equals(calendar.getPublished())) {
                    Employee employee = employeeRepository.findById(employeeId).orElse(null);
                    if (employee != null && employee.getUser() != null) {
                        try {
                            notificationService.notifyEmployeeOfSchedule(
                                    employee.getUser().getId(),
                                    NotificationType.SCHEDULE_UPDATE,
                                    calendar.getMonth(),
                                    calendar.getId());
                        } catch (Exception e) {
                            System.err.println("Failed to notify employee " + employeeId
                                    + " of schedule update: " + e.getMessage());
                        }
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // UC12 — notify all assigned employees helper
    // -------------------------------------------------------------------------

    /**
     * Collects every unique employee assigned to any team in any schedule
     * of the given calendar, then sends them a schedule notification.
     *
     * @param calendar the calendar that was just published or updated
     * @param type     NEW_SCHEDULE on first publish, SCHEDULE_UPDATE on edit
     */
    private void notifyAssignedEmployees(Calendar calendar, NotificationType type) {
        Set<Long> notifiedUserIds = new HashSet<>();

        for (Schedule schedule : calendar.getSchedules()) {
            if (schedule.getTeams() == null) continue;
            for (ScheduleTeam team : schedule.getTeams()) {
                for (Employee employee : team.getEmployees()) {
                    if (employee.getUser() == null) continue;
                    Long userId = employee.getUser().getId();
                    if (!notifiedUserIds.add(userId)) continue;

                    try {
                        notificationService.notifyEmployeeOfSchedule(
                                userId,
                                type,
                                calendar.getMonth(),
                                calendar.getId());
                    } catch (Exception e) {
                        System.err.println("Failed to notify employee user "
                                + userId + ": " + e.getMessage());
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private Calendar findCalendarOrThrow(Long id) {
        return calendarRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Calendar not found with id: " + id));
    }
}