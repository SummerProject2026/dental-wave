package com.summerproject2026.DentalWave.service.impl;

import com.summerproject2026.DentalWave.dto.CalendarDto;
import com.summerproject2026.DentalWave.dto.ScheduleDto;
import com.summerproject2026.DentalWave.enums.NotificationType;
import com.summerproject2026.DentalWave.exception.ResourceNotFoundException;
import com.summerproject2026.DentalWave.mapper.CalendarMapper;
import com.summerproject2026.DentalWave.mapper.ScheduleMapper;
import com.summerproject2026.DentalWave.entity.Calendar;
import com.summerproject2026.DentalWave.entity.Employee;
import com.summerproject2026.DentalWave.entity.Office;
import com.summerproject2026.DentalWave.entity.Schedule;
import com.summerproject2026.DentalWave.entity.ScheduleTeam;
import com.summerproject2026.DentalWave.entity.User;
import com.summerproject2026.DentalWave.repository.CalendarRepository;
import com.summerproject2026.DentalWave.repository.EmployeeRepository;
import com.summerproject2026.DentalWave.repository.OfficeRepository;
import com.summerproject2026.DentalWave.repository.ScheduleRepository;
import com.summerproject2026.DentalWave.repository.UserRepository;
import com.summerproject2026.DentalWave.service.CalendarService;
import com.summerproject2026.DentalWave.service.NotificationService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of CalendarService.
 * Handles all calendar business logic including creation, updates,
 * publish/unpublish lifecycle, nested schedule management,
 * auto-generation of draft calendars with role-based team assignment,
 * and bulk scheduling/unscheduling an employee across an entire calendar.
 */
@Slf4j
@Service
@Transactional
public class CalendarServiceImpl implements CalendarService {

    private final CalendarRepository calendarRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final OfficeRepository officeRepository;
    private final EmployeeRepository employeeRepository;
    private final CalendarMapper calendarMapper;
    private final ScheduleMapper scheduleMapper;
    private final NotificationService notificationService;

    @Autowired
    public CalendarServiceImpl(CalendarRepository calendarRepository,
                               ScheduleRepository scheduleRepository,
                               UserRepository userRepository,
                               OfficeRepository officeRepository,
                               EmployeeRepository employeeRepository,
                               CalendarMapper calendarMapper,
                               ScheduleMapper scheduleMapper,
                               NotificationService notificationService) {
        this.calendarRepository = calendarRepository;
        this.scheduleRepository = scheduleRepository;
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

    @Override
    public CalendarDto publishCalendar(Long id) {
        Calendar calendar = findCalendarOrThrow(id);

        // Track whether this is the first publish so we only send
        // NEW_SCHEDULE notifications once (UC12 business rule: employees
        // receive a New Schedule notification when the calendar is
        // published for the first time each month).
        boolean wasPublished = Boolean.TRUE.equals(calendar.getPublished());

        calendar.setPublished(true);
        Calendar saved = calendarRepository.save(calendar);

        if (!wasPublished) {
            notifyEmployees(saved, collectAssignedEmployees(saved), NotificationType.NEW_SCHEDULE);
        }

        return calendarMapper.mapToCalendarDto(saved);
    }

    @Override
    public CalendarDto unpublishCalendar(Long id) {
        Calendar calendar = findCalendarOrThrow(id);
        calendar.setPublished(false);
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

        // UC12: if the calendar is already published, employees on the
        // newly added day are directly affected by this post-publish edit.
        if (Boolean.TRUE.equals(calendar.getPublished())) {
            notifyEmployees(calendar, employeesOnSchedule(schedule), NotificationType.SCHEDULE_UPDATE);
        }

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

        // Capture affected employees before the day is removed (UC12).
        Set<Employee> affected = employeesOnSchedule(schedule);

        calendar.removeSchedule(schedule);
        calendarRepository.save(calendar);

        if (Boolean.TRUE.equals(calendar.getPublished())) {
            notifyEmployees(calendar, affected, NotificationType.SCHEDULE_UPDATE);
        }
    }

    // -------------------------------------------------------------------------
    // Auto-generation with role-based team assignment
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     *
     * <p>Builds a draft calendar covering every Monday-Friday in the
     * requested date range. For each day, employees at the given office
     * are grouped by position: Doctors are paired 1:1 with TCs to form
     * teams, and any remaining Assistants are distributed as evenly as
     * possible across the teams created that day.</p>
     *
     * <p>Throws {@link IllegalStateException} if a calendar already
     * exists for the same office and month, to prevent duplicates.</p>
     */
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

    /**
     * Builds a single day's schedule with teams assigned based on role.
     * Pairs each Doctor 1:1 with a TC to form a team, then distributes
     * Assistants as evenly as possible across the teams created.
     *
     * @param date       the date this schedule covers
     * @param doctors    all Doctors available at this office
     * @param tcs        all TCs available at this office
     * @param assistants all Assistants available at this office
     * @return the built Schedule entity (not yet persisted independently;
     *         it is added to the calendar via addSchedule)
     */
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
    // Bulk scheduling / unscheduling an employee across a calendar
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     *
     * <p>For each day's schedule in the calendar, finds the team with
     * the fewest members and adds the employee to it, balancing team
     * sizes across the month. If a schedule has no teams at all, it is
     * skipped (there is nothing to balance against).</p>
     */
    @Override
    public CalendarDto scheduleEmployeeAcrossCalendar(Long calendarId, Long employeeId) {
        Calendar calendar = findCalendarOrThrow(calendarId);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found with id: " + employeeId));

        for (Schedule schedule : calendar.getSchedules()) {
            List<ScheduleTeam> teams = schedule.getTeams();
            if (teams == null || teams.isEmpty()) {
                continue;
            }

            boolean alreadyAssigned = teams.stream()
                    .anyMatch(team -> team.getEmployees().stream()
                            .anyMatch(e -> e.getId().equals(employeeId)));
            if (alreadyAssigned) {
                continue;
            }

            ScheduleTeam smallestTeam = teams.stream()
                    .min(Comparator.comparingInt(team -> team.getEmployees().size()))
                    .orElse(null);

            if (smallestTeam != null) {
                smallestTeam.getEmployees().add(employee);
            }
        }

        Calendar saved = calendarRepository.save(calendar);

        // UC12: the bulk-scheduled employee is directly affected; notify them
        // of the schedule change if the calendar is already published.
        if (Boolean.TRUE.equals(saved.getPublished())) {
            notifyEmployees(saved, List.of(employee), NotificationType.SCHEDULE_UPDATE);
        }

        return calendarMapper.mapToCalendarDto(saved);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Removes the employee from every team on every day in the
     * calendar, regardless of which team they were on.</p>
     */
    @Override
    public CalendarDto removeEmployeeFromCalendar(Long calendarId, Long employeeId) {
        Calendar calendar = findCalendarOrThrow(calendarId);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found with id: " + employeeId));

        for (Schedule schedule : calendar.getSchedules()) {
            if (schedule.getTeams() == null) {
                continue;
            }
            for (ScheduleTeam team : schedule.getTeams()) {
                team.getEmployees().removeIf(e -> e.getId().equals(employeeId));
            }
        }

        Calendar saved = calendarRepository.save(calendar);

        // UC12: the removed employee is directly affected (their assignment
        // changed); notify them if the calendar is already published.
        if (Boolean.TRUE.equals(saved.getPublished())) {
            notifyEmployees(saved, List.of(employee), NotificationType.SCHEDULE_UPDATE);
        }

        return calendarMapper.mapToCalendarDto(saved);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private Calendar findCalendarOrThrow(Long id) {
        return calendarRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Calendar not found with id: " + id));
    }

    // -------------------------------------------------------------------------
    // UC12 — Employee schedule notifications
    // -------------------------------------------------------------------------

    /**
     * Collects the distinct set of employees assigned to any team on any
     * day of the calendar. Order is preserved for deterministic behaviour.
     *
     * @param calendar the calendar to scan
     * @return distinct employees assigned anywhere in the calendar
     */
    private Set<Employee> collectAssignedEmployees(Calendar calendar) {
        Set<Employee> employees = new LinkedHashSet<>();
        for (Schedule schedule : calendar.getSchedules()) {
            employees.addAll(employeesOnSchedule(schedule));
        }
        return employees;
    }

    /**
     * Collects the distinct employees assigned to any team on a single day.
     *
     * @param schedule the day's schedule
     * @return distinct employees on that day (empty if none)
     */
    private Set<Employee> employeesOnSchedule(Schedule schedule) {
        Set<Employee> employees = new LinkedHashSet<>();
        if (schedule.getTeams() == null) {
            return employees;
        }
        for (ScheduleTeam team : schedule.getTeams()) {
            if (team.getEmployees() != null) {
                employees.addAll(team.getEmployees());
            }
        }
        return employees;
    }

    /**
     * Sends a NEW_SCHEDULE or SCHEDULE_UPDATE notification to each affected
     * employee (UC12). Each delivery is isolated: a failure for one employee
     * is logged and does not prevent the others from being notified.
     *
     * <p>Implements the UC12 "Notification Delivery Failure" alternate flow:
     * if any deliveries fail, the publishing manager receives a SYSTEM
     * notification listing the employees who could not be reached.</p>
     *
     * @param calendar  the calendar the notification relates to
     * @param employees the employees to notify
     * @param type      NEW_SCHEDULE or SCHEDULE_UPDATE
     */
    private void notifyEmployees(Calendar calendar,
                                 Collection<Employee> employees,
                                 NotificationType type) {
        if (employees == null || employees.isEmpty()) {
            return;
        }

        List<String> failures = new ArrayList<>();

        for (Employee employee : employees) {
            // An employee must have a backing user account to receive notifications.
            if (employee.getUser() == null) {
                continue;
            }
            try {
                notificationService.notifyEmployeeOfSchedule(
                        employee.getUser().getId(),
                        type,
                        calendar.getMonth(),
                        calendar.getId());
            } catch (Exception ex) {
                // UC12: log the failed delivery with the employee and timestamp.
                log.error("Failed to deliver {} notification to employee {} ({}): {}",
                        type, employee.getId(), employee.getUser().getUsername(), ex.getMessage());
                failures.add(employee.getUser().getUsername());
            }
        }

        // UC12 alternate flow: alert the publishing manager of any failures.
        if (!failures.isEmpty() && calendar.getCreatedBy() != null) {
            try {
                notificationService.sendNotification(
                        calendar.getCreatedBy().getId(),
                        "Schedule notification could not be delivered to one or more "
                                + "employees: " + String.join(", ", failures),
                        NotificationType.SYSTEM,
                        "CALENDAR",
                        calendar.getId());
            } catch (Exception ex) {
                log.error("Failed to alert manager {} of notification delivery failures: {}",
                        calendar.getCreatedBy().getId(), ex.getMessage());
            }
        }
    }
}