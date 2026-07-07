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
import com.summerproject2026.DentalWave.entity.TimeOffRequest;
import com.summerproject2026.DentalWave.entity.User;
import com.summerproject2026.DentalWave.enums.NotificationType;
import com.summerproject2026.DentalWave.enums.RequestStatus;
import com.summerproject2026.DentalWave.enums.WorkStatus;
import com.summerproject2026.DentalWave.repository.CalendarRepository;
import com.summerproject2026.DentalWave.repository.EmployeeRepository;
import com.summerproject2026.DentalWave.repository.OfficeRepository;
import com.summerproject2026.DentalWave.repository.ScheduleRepository;
import com.summerproject2026.DentalWave.repository.ScheduleTeamRepository;
import com.summerproject2026.DentalWave.repository.TimeOffRequestRepository;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
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
    private final TimeOffRequestRepository timeOffRequestRepository;
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
                               TimeOffRequestRepository timeOffRequestRepository,
                               CalendarMapper calendarMapper,
                               ScheduleMapper scheduleMapper,
                               NotificationService notificationService) {
        this.calendarRepository = calendarRepository;
        this.scheduleRepository = scheduleRepository;
        this.scheduleTeamRepository = scheduleTeamRepository;
        this.userRepository = userRepository;
        this.officeRepository = officeRepository;
        this.employeeRepository = employeeRepository;
        this.timeOffRequestRepository = timeOffRequestRepository;
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

        if (Boolean.TRUE.equals(calendarDto.getPublished())
                && !Boolean.TRUE.equals(existing.getPublished())) {
            validateCalendarCanPublish(existing);
        }

        if (calendarDto.getPublished() != null) {
            existing.setPublished(calendarDto.getPublished());
            for (Schedule schedule : existing.getSchedules()) {
                schedule.setPublished(calendarDto.getPublished());
            }
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
        validateCalendarCanPublish(calendar);
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

        List<Employee> allAssistants = getSchedulableAssistants();
        GenerationContext generationContext = buildGenerationContext(calendarDto.getMonth());

        LocalDate current = calendarDto.getStartCalendarDate();
        LocalDate end = calendarDto.getEndCalendarDate();

        while (!current.isAfter(end)) {
            List<String> teamNames = getDefaultTeamNamesForOfficeDay(office.getName(), current.getDayOfWeek());
            if (!teamNames.isEmpty()) {
                Schedule schedule = buildScheduleForDay(
                        current,
                        office,
                        teamNames,
                        allAssistants,
                        generationContext,
                        calendarDto.getMonth());
                calendar.addSchedule(schedule);
            }
            current = current.plusDays(1);
        }

        Calendar saved = calendarRepository.save(calendar);
        return calendarMapper.mapToCalendarDto(saved);
    }

    private Schedule buildScheduleForDay(LocalDate date,
                                         Office office,
                                         List<String> teamNames,
                                         List<Employee> allAssistants,
                                         GenerationContext generationContext,
                                         String month) {
        Schedule schedule = new Schedule();
        schedule.setDate(date);
        schedule.setStartTime(LocalTime.of(8, 0));
        schedule.setEndTime(LocalTime.of(17, 0));
        schedule.setPublished(false);

        List<ScheduleTeam> teams = new ArrayList<>();
        for (String teamName : teamNames) {
            ScheduleTeam team = new ScheduleTeam();
            team.setName(teamName);
            team.setSchedule(schedule);
            teams.add(team);
        }

        if (!teams.isEmpty()) {
            List<Employee> selectedAssistants = selectAssistantsForOfficeDay(
                    date,
                    office,
                    teams.size(),
                    allAssistants,
                    generationContext,
                    month);

            for (Employee assistant : selectedAssistants) {
                ScheduleTeam team = selectTeamForAssistant(assistant, teams, office, generationContext);
                team.getEmployees().add(assistant);
                markAssistantAssigned(assistant, office, team, date, generationContext);
            }
        }

        schedule.setTeams(teams);
        return schedule;
    }

    private List<String> getDefaultTeamNamesForOfficeDay(String officeName, DayOfWeek dayOfWeek) {
        Map<DayOfWeek, List<String>> officePattern = getDefaultMonthlyPattern()
                .getOrDefault(normalizeOfficeName(officeName), Map.of());

        return officePattern.getOrDefault(dayOfWeek, List.of());
    }

    private Map<String, Map<DayOfWeek, List<String>>> getDefaultMonthlyPattern() {
        Map<String, Map<DayOfWeek, List<String>>> pattern = new LinkedHashMap<>();

        pattern.put("raleigh", Map.of(
                DayOfWeek.MONDAY, List.of("C) Dr. Collie", "M) Dr. Macon"),
                DayOfWeek.TUESDAY, List.of("L) Dr. Lamb"),
                DayOfWeek.WEDNESDAY, List.of("L) Dr. Lamb", "McC) Dr. McCutchen"),
                DayOfWeek.THURSDAY, List.of("McC) Dr. McCutchen")
        ));

        pattern.put("garner", Map.of(
                DayOfWeek.MONDAY, List.of("L) Dr. Lamb"),
                DayOfWeek.TUESDAY, List.of("M) Dr. Macon"),
                DayOfWeek.WEDNESDAY, List.of("C) Dr. Collie"),
                DayOfWeek.THURSDAY, List.of("L) Dr. Lamb")
        ));

        pattern.put("smithfield", Map.of(
                DayOfWeek.TUESDAY, List.of("C) Dr. Collie")
        ));

        return pattern;
    }

    private String normalizeOfficeName(String officeName) {
        if (officeName == null) {
            return "";
        }
        return officeName.trim().toLowerCase();
    }

    private List<Employee> getActiveEmployeesByOfficeAndPosition(Long officeId, String position) {
        return employeeRepository.findByOfficeId(officeId).stream()
                .filter(employee -> employee.getStatus() == WorkStatus.ACTIVE)
                .filter(employee -> employee.getPosition() != null
                        && employee.getPosition().equalsIgnoreCase(position))
                .collect(Collectors.toList());
    }

    private List<Employee> getSchedulableAssistants() {
        return employeeRepository.findAll().stream()
                .filter(employee -> employee.getStatus() == WorkStatus.ACTIVE)
                .filter(this::isSchedulableAssistant)
                .collect(Collectors.toList());
    }

    private boolean isSchedulableAssistant(Employee employee) {
        if (employee.getPosition() == null) {
            return false;
        }

        String position = employee.getPosition().trim().toLowerCase();
        return !position.equals("tc")
                && !position.contains("doctor")
                && !position.equals("manager")
                && !position.equals("hr")
                && !position.equals("admin");
    }

    private GenerationContext buildGenerationContext(String month) {
        GenerationContext context = new GenerationContext();

        for (Calendar existingCalendar : calendarRepository.findByMonth(month)) {
            if (existingCalendar.getSchedules() == null) continue;
            Long officeId = existingCalendar.getOffice() != null
                    ? existingCalendar.getOffice().getId()
                    : null;

            for (Schedule schedule : existingCalendar.getSchedules()) {
                if (schedule.getDate() == null || schedule.getTeams() == null) continue;

                for (ScheduleTeam team : schedule.getTeams()) {
                    for (Employee employee : team.getEmployees()) {
                        Long employeeId = employee.getId();
                        context.assignmentCounts.merge(employeeId, 1, Integer::sum);
                        context.assignedByDate
                                .computeIfAbsent(schedule.getDate(), date -> new HashSet<>())
                                .add(employeeId);

                        if (officeId != null) {
                            context.officeAssignmentCounts
                                    .computeIfAbsent(employeeId, id -> new HashMap<>())
                                    .merge(officeId, 1, Integer::sum);
                        }

                        context.weekdayAssignmentCounts
                                .computeIfAbsent(employeeId, id -> new HashMap<>())
                                .merge(schedule.getDate().getDayOfWeek(), 1, Integer::sum);
                        if (officeId != null && team.getName() != null) {
                            context.teamAssignmentCounts
                                    .computeIfAbsent(employeeId, id -> new HashMap<>())
                                    .merge(getTeamKey(officeId, team.getName()), 1, Integer::sum);
                        }
                    }
                }
            }
        }

        timeOffRequestRepository.findByStatus(RequestStatus.APPROVED).forEach(request -> {
            if (request.getEmployee() == null || request.getStartDate() == null) return;

            Long employeeId = request.getEmployee().getId();
            LocalDate endDate = request.getEndDate() != null ? request.getEndDate() : request.getStartDate();
            LocalDate current = request.getStartDate();
            while (!current.isAfter(endDate)) {
                context.approvedLeaveByDate
                        .computeIfAbsent(current, date -> new HashSet<>())
                        .add(employeeId);
                current = current.plusDays(1);
            }
        });

        return context;
    }

    private List<Employee> selectAssistantsForOfficeDay(LocalDate date,
                                                        Office office,
                                                        int doctorTeamCount,
                                                        List<Employee> allAssistants,
                                                        GenerationContext context,
                                                        String month) {
        int totalDoctorTeamsForDay = Math.max(doctorTeamCount, getTotalDoctorTeamsForDay(date.getDayOfWeek()));
        List<Employee> availableAssistants = allAssistants.stream()
                .filter(employee -> isAssistantEligibleForOfficeDay(employee, office, date, context))
                .collect(Collectors.toList());

        if (availableAssistants.isEmpty()) {
            return List.of();
        }

        int targetCoverage = Math.max(doctorTeamCount,
                (int) Math.ceil((availableAssistants.size() * (double) doctorTeamCount) / totalDoctorTeamsForDay));
        targetCoverage = Math.min(targetCoverage, availableAssistants.size());

        List<Employee> sortedAssistants = availableAssistants.stream()
                .sorted(Comparator
                        .comparingInt((Employee employee) ->
                                context.assignmentCounts.getOrDefault(employee.getId(), 0))
                        .thenComparingInt(employee -> getOfficeAssignmentCount(employee, office, context))
                        .thenComparingInt(employee -> getWeekdayAssignmentCount(employee, date.getDayOfWeek(), context))
                        .thenComparingInt(employee -> getShuffleRank(employee, context))
                        .thenComparing(Employee::getId))
                .collect(Collectors.toList());

        List<Employee> selectedAssistants = new ArrayList<>();
        Set<Long> selectedIds = new HashSet<>();

        for (Employee assistant : sortedAssistants) {
            if (isLastEligibleOfficeForAssistant(month, office, date, assistant)) {
                selectedAssistants.add(assistant);
                selectedIds.add(assistant.getId());
            }
        }

        for (Employee assistant : sortedAssistants) {
            if (selectedAssistants.size() >= targetCoverage) {
                break;
            }
            if (selectedIds.add(assistant.getId())) {
                selectedAssistants.add(assistant);
            }
        }

        return selectedAssistants;
    }

    private boolean isAssistantEligibleForOfficeDay(Employee employee,
                                                   Office office,
                                                   LocalDate date,
                                                   GenerationContext context) {
        Long employeeId = employee.getId();
        if (context.assignedByDate.getOrDefault(date, Set.of()).contains(employeeId)) {
            return false;
        }
        if (context.approvedLeaveByDate.getOrDefault(date, Set.of()).contains(employeeId)) {
            return false;
        }
        if (!isAssignedToOffice(employee, office)) {
            return false;
        }
        return isAvailableOnDay(employee, date.getDayOfWeek());
    }

    private boolean isAssignedToOffice(Employee employee, Office office) {
        if (employee.getOffices() == null || employee.getOffices().isEmpty()) {
            return true;
        }
        return employee.getOffices().stream()
                .anyMatch(employeeOffice -> employeeOffice.getId().equals(office.getId()));
    }

    private boolean isAvailableOnDay(Employee employee, DayOfWeek dayOfWeek) {
        if (employee.getAvailabilities() == null || employee.getAvailabilities().isEmpty()) {
            return true;
        }
        return employee.getAvailabilities().stream()
                .anyMatch(availability ->
                        Boolean.TRUE.equals(availability.getAvailable())
                                && availability.getDayOfWeek() == dayOfWeek);
    }

    private int getOfficeAssignmentCount(Employee employee, Office office, GenerationContext context) {
        return context.officeAssignmentCounts
                .getOrDefault(employee.getId(), Map.of())
                .getOrDefault(office.getId(), 0);
    }

    private int getWeekdayAssignmentCount(Employee employee, DayOfWeek dayOfWeek, GenerationContext context) {
        return context.weekdayAssignmentCounts
                .getOrDefault(employee.getId(), Map.of())
                .getOrDefault(dayOfWeek, 0);
    }

    private int getShuffleRank(Employee employee, GenerationContext context) {
        return context.shuffleRanks.computeIfAbsent(employee.getId(),
                ignored -> seededRank(context, employee.getId(), "assistant"));
    }

    private ScheduleTeam selectTeamForAssistant(Employee assistant,
                                                List<ScheduleTeam> teams,
                                                Office office,
                                                GenerationContext context) {
        return teams.stream()
                .min(Comparator
                        .comparingInt((ScheduleTeam team) -> team.getEmployees().size())
                        .thenComparingInt(team -> getTeamAssignmentCount(assistant, office, team, context))
                        .thenComparingInt(team -> seededRank(context, assistant.getId(), office.getId(), team.getName()))
                        .thenComparing(ScheduleTeam::getName))
                .orElse(teams.get(0));
    }

    private int getTeamAssignmentCount(Employee employee,
                                       Office office,
                                       ScheduleTeam team,
                                       GenerationContext context) {
        return context.teamAssignmentCounts
                .getOrDefault(employee.getId(), Map.of())
                .getOrDefault(getTeamKey(office.getId(), team.getName()), 0);
    }

    private String getTeamKey(Long officeId, String teamName) {
        return officeId + ":" + normalizeTeamName(teamName);
    }

    private String normalizeTeamName(String teamName) {
        return teamName == null ? "" : teamName.trim().toLowerCase();
    }

    private int seededRank(GenerationContext context, Object... values) {
        long hash = context.randomSeed;
        for (Object value : values) {
            hash = (hash * 31) + (value == null ? 0 : value.hashCode());
        }
        return Math.floorMod(Long.hashCode(hash), Integer.MAX_VALUE);
    }

    private boolean isLastEligibleOfficeForAssistant(String month,
                                                     Office currentOffice,
                                                     LocalDate date,
                                                     Employee assistant) {
        Set<Long> existingOfficeIds = calendarRepository.findByMonth(month).stream()
                .filter(calendar -> calendar.getOffice() != null)
                .map(calendar -> calendar.getOffice().getId())
                .collect(Collectors.toSet());

        return officeRepository.findAll().stream()
                .filter(office -> office.getId() != null)
                .filter(office -> !office.getId().equals(currentOffice.getId()))
                .filter(office -> !existingOfficeIds.contains(office.getId()))
                .filter(office -> !getDefaultTeamNamesForOfficeDay(office.getName(), date.getDayOfWeek()).isEmpty())
                .noneMatch(office -> isAssignedToOffice(assistant, office));
    }

    private void markAssistantAssigned(Employee employee,
                                       Office office,
                                       ScheduleTeam team,
                                       LocalDate date,
                                       GenerationContext context) {
        Long employeeId = employee.getId();
        context.assignmentCounts.merge(employeeId, 1, Integer::sum);
        context.assignedByDate
                .computeIfAbsent(date, ignored -> new HashSet<>())
                .add(employeeId);
        context.officeAssignmentCounts
                .computeIfAbsent(employeeId, ignored -> new HashMap<>())
                .merge(office.getId(), 1, Integer::sum);
        context.weekdayAssignmentCounts
                .computeIfAbsent(employeeId, ignored -> new HashMap<>())
                .merge(date.getDayOfWeek(), 1, Integer::sum);
        context.teamAssignmentCounts
                .computeIfAbsent(employeeId, ignored -> new HashMap<>())
                .merge(getTeamKey(office.getId(), team.getName()), 1, Integer::sum);
    }

    private int getTotalDoctorTeamsForDay(DayOfWeek dayOfWeek) {
        return getDefaultMonthlyPattern().values().stream()
                .mapToInt(officePattern -> officePattern.getOrDefault(dayOfWeek, List.of()).size())
                .sum();
    }

    private static class GenerationContext {
        private final long randomSeed = ThreadLocalRandom.current().nextLong();
        private final Map<Long, Integer> assignmentCounts = new HashMap<>();
        private final Map<Long, Map<Long, Integer>> officeAssignmentCounts = new HashMap<>();
        private final Map<Long, Map<DayOfWeek, Integer>> weekdayAssignmentCounts = new HashMap<>();
        private final Map<Long, Map<String, Integer>> teamAssignmentCounts = new HashMap<>();
        private final Map<LocalDate, Set<Long>> assignedByDate = new HashMap<>();
        private final Map<LocalDate, Set<Long>> approvedLeaveByDate = new HashMap<>();
        private final Map<Long, Integer> shuffleRanks = new HashMap<>();
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

        boolean anyScheduleModified = false;

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
                anyScheduleModified = true;
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

        if (anyScheduleModified) {
            markApprovedRequestRemovedFromSchedule(employeeId, startDate, endDate);
        }
    }

    private void markApprovedRequestRemovedFromSchedule(Long employeeId,
                                                        LocalDate startDate,
                                                        LocalDate endDate) {
        List<TimeOffRequest> matchingRequests = timeOffRequestRepository
                .findByEmployeeIdAndStatus(employeeId, RequestStatus.APPROVED)
                .stream()
                .filter(request -> startDate.equals(request.getStartDate())
                        && endDate.equals(request.getEndDate()))
                .collect(Collectors.toList());

        for (TimeOffRequest request : matchingRequests) {
            request.setScheduleRemoved(true);
        }

        if (!matchingRequests.isEmpty()) {
            timeOffRequestRepository.saveAll(matchingRequests);
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

    /**
     * Backend source-of-truth validation before a draft becomes visible to staff.
     * Frontend warnings are helpful, but this protects the persisted workflow.
     *
     * @param calendar calendar draft being published
     * @throws IllegalStateException when the calendar has unsafe assignments
     */
    private void validateCalendarCanPublish(Calendar calendar) {
        List<String> issues = new ArrayList<>();

        if (calendar.getSchedules() == null || calendar.getSchedules().isEmpty()) {
            issues.add("Calendar has no scheduled days.");
        }

        for (Schedule schedule : calendar.getSchedules()) {
            LocalDate scheduleDate = schedule.getDate();
            String dateLabel = scheduleDate != null ? scheduleDate.toString() : "an unscheduled date";

            if (schedule.getTeams() == null || schedule.getTeams().isEmpty()) {
                issues.add("No teams are assigned on " + dateLabel + ".");
                continue;
            }

            Set<Long> employeesOnThisSchedule = new HashSet<>();

            for (ScheduleTeam team : schedule.getTeams()) {
                if (team.getEmployees() == null || team.getEmployees().isEmpty()) {
                    if (!isPlaceholderTeam(team.getName())) {
                        issues.add("Team " + team.getName() + " on " + dateLabel
                                + " has no assistants assigned.");
                    }
                    continue;
                }

                for (Employee employee : team.getEmployees()) {
                    if (employee == null || employee.getId() == null) {
                        issues.add("An invalid employee assignment exists on " + dateLabel + ".");
                        continue;
                    }

                    String employeeName = getEmployeeDisplayName(employee);

                    if (!employeesOnThisSchedule.add(employee.getId())) {
                        issues.add(employeeName + " is assigned more than once on " + dateLabel + ".");
                    }

                    if (employee.getStatus() == WorkStatus.INACTIVE
                            || employee.getStatus() == WorkStatus.TERMINATED) {
                        issues.add(employeeName + " is inactive or terminated but scheduled on "
                                + dateLabel + ".");
                    }

                    if (!employeeBelongsToCalendarOffice(employee, calendar)) {
                        String officeName = calendar.getOffice() != null
                                ? calendar.getOffice().getName()
                                : "this office";
                        issues.add(employeeName + " is not assigned to " + officeName
                                + " but is scheduled on " + dateLabel + ".");
                    }

                    if (hasApprovedTimeOffOnDate(employee.getId(), scheduleDate)) {
                        issues.add(employeeName + " is scheduled during approved time off on "
                                + dateLabel + ".");
                    }

                    if (isDoubleBookedOutsideSchedule(employee.getId(), schedule)) {
                        issues.add(employeeName + " is double-booked on " + dateLabel + ".");
                    }
                }
            }
        }

        if (!issues.isEmpty()) {
            throw new IllegalStateException(
                    "Schedule cannot be published until these issues are fixed: "
                            + String.join(" ", issues));
        }
    }

    private boolean isPlaceholderTeam(String teamName) {
        if (teamName == null) {
            return false;
        }

        String normalized = teamName.trim().toLowerCase();
        return normalized.contains("no dr")
                || normalized.contains("no doctor")
                || normalized.contains("vacation")
                || normalized.contains("pto")
                || normalized.contains("time off")
                || normalized.contains("note")
                || normalized.contains("closed")
                || normalized.contains("out");
    }

    /**
     * Checks whether an employee is assigned to the calendar's office.
     *
     * @param employee employee assigned to a team
     * @param calendar calendar being published
     * @return true if the employee belongs to that office
     */
    private boolean employeeBelongsToCalendarOffice(Employee employee, Calendar calendar) {
        if (calendar.getOffice() == null || calendar.getOffice().getId() == null) {
            return false;
        }

        return employee.getOffices() != null
                && employee.getOffices().stream()
                .anyMatch(office -> office.getId().equals(calendar.getOffice().getId()));
    }

    /**
     * Checks whether an employee has approved time off on a date.
     *
     * @param employeeId employee to check
     * @param date schedule date
     * @return true if approved leave overlaps the date
     */
    private boolean hasApprovedTimeOffOnDate(Long employeeId, LocalDate date) {
        if (employeeId == null || date == null) return false;

        return timeOffRequestRepository.findByEmployeeId(employeeId).stream()
                .filter(request -> request.getStatus() == RequestStatus.APPROVED)
                .anyMatch(request -> dateFallsWithinRequest(date, request));
    }

    /**
     * Checks whether a date falls inside a time-off request range.
     *
     * @param date date to check
     * @param request approved time-off request
     * @return true if the date is within the request range
     */
    private boolean dateFallsWithinRequest(LocalDate date, TimeOffRequest request) {
        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate() != null ? request.getEndDate() : startDate;
        return startDate != null
                && !date.isBefore(startDate)
                && !date.isAfter(endDate);
    }

    /**
     * Checks whether an employee is scheduled elsewhere on the same day.
     *
     * @param employeeId employee to check
     * @param currentSchedule schedule currently being validated
     * @return true if another schedule already includes the employee
     */
    private boolean isDoubleBookedOutsideSchedule(Long employeeId, Schedule currentSchedule) {
        if (employeeId == null || currentSchedule.getDate() == null) return false;

        return scheduleRepository.findByDate(currentSchedule.getDate()).stream()
                .filter(schedule -> currentSchedule.getId() == null
                        || !currentSchedule.getId().equals(schedule.getId()))
                .flatMap(schedule -> schedule.getTeams().stream())
                .flatMap(team -> team.getEmployees().stream())
                .anyMatch(employee -> employee.getId().equals(employeeId));
    }

    private String getEmployeeDisplayName(Employee employee) {
        if (employee.getUser() == null) {
            return "Employee " + employee.getId();
        }

        return (employee.getUser().getFirstName() + " "
                + employee.getUser().getLastName()).trim();
    }

    private Calendar findCalendarOrThrow(Long id) {
        return calendarRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Calendar not found with id: " + id));
    }
}
