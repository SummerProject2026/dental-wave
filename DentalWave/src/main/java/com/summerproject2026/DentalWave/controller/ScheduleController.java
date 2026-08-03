package com.summerproject2026.DentalWave.controller;

import com.summerproject2026.DentalWave.dto.ScheduleDto;
import com.summerproject2026.DentalWave.dto.EmployeeDto;
import com.summerproject2026.DentalWave.dto.PartialDayNoteRequest;
import com.summerproject2026.DentalWave.service.EmployeeService;
import com.summerproject2026.DentalWave.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST controller exposing schedule management endpoints.
 * Base path: /api/schedules
 *
 * Responsibilities:
 *  - CRUD for schedules
 *  - Filtering by date, calendar, or employee
 *  - Team assignment / removal
 *  - Publishing a schedule
 */
@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final EmployeeService employeeService;

    @Autowired
    public ScheduleController(ScheduleService scheduleService,
                              EmployeeService employeeService) {
        this.scheduleService = scheduleService;
        this.employeeService = employeeService;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(
            IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
    }

    // -------------------------------------------------------------------------
    // POST /api/schedules — create
    // -------------------------------------------------------------------------

    /**
     * Creates a standalone schedule.
     *
     * @param scheduleDto the schedule data from the request body
     * @return 201 Created with the persisted ScheduleDto
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ScheduleDto> createSchedule(@RequestBody ScheduleDto scheduleDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(scheduleService.createSchedule(scheduleDto));
    }

    // -------------------------------------------------------------------------
    // GET /api/schedules/{id} — read one
    // -------------------------------------------------------------------------

    /**
     * Returns a single schedule by ID.
     *
     * @param id the schedule ID
     * @return 200 OK with the ScheduleDto
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'ADMIN')")
    public ResponseEntity<ScheduleDto> getScheduleById(@PathVariable Long id) {
        return ResponseEntity.ok(scheduleService.getScheduleById(id));
    }

    // -------------------------------------------------------------------------
    // GET /api/schedules — read all
    // -------------------------------------------------------------------------

    /**
     * Returns all schedules in the system.
     *
     * @return 200 OK with the full list
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<ScheduleDto>> getAllSchedules() {
        return ResponseEntity.ok(scheduleService.getAllSchedules());
    }

    // -------------------------------------------------------------------------
    // PUT /api/schedules/{id} — update
    // -------------------------------------------------------------------------

    /**
     * Updates an existing schedule's scalar fields.
     *
     * @param id          the schedule ID
     * @param scheduleDto the updated data
     * @return 200 OK with the updated ScheduleDto
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ScheduleDto> updateSchedule(@PathVariable Long id,
                                                      @RequestBody ScheduleDto scheduleDto) {
        return ResponseEntity.ok(scheduleService.updateSchedule(id, scheduleDto));
    }

    // -------------------------------------------------------------------------
    // DELETE /api/schedules/{id} — delete
    // -------------------------------------------------------------------------

    /**
     * Deletes a schedule by ID.
     *
     * @param id the schedule ID
     * @return 200 OK with a confirmation message
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<String> deleteSchedule(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        return ResponseEntity.ok("Schedule with id " + id + " deleted successfully.");
    }

    // -------------------------------------------------------------------------
    // GET /api/schedules/date/{date} — filter by date
    // -------------------------------------------------------------------------

    /**
     * Returns all schedules on a specific date.
     *
     * @param date ISO date string e.g. /date/2025-06-15
     * @return 200 OK with the matching list
     */
    @GetMapping("/date/{date}")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<ScheduleDto>> getSchedulesByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(scheduleService.getSchedulesByDate(date));
    }

    // -------------------------------------------------------------------------
    // GET /api/schedules/calendar/{calendarId} — filter by calendar
    // -------------------------------------------------------------------------

    /**
     * Returns all schedules belonging to a given calendar.
     *
     * @param calendarId the parent calendar ID
     * @return 200 OK with the matching list
     */
    @GetMapping("/calendar/{calendarId}")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<ScheduleDto>> getSchedulesByCalendar(@PathVariable Long calendarId) {
        return ResponseEntity.ok(scheduleService.getSchedulesByCalendar(calendarId));
    }

    // -------------------------------------------------------------------------
    // GET /api/schedules/employee/{employeeId} — UC2: employee views personal calendar
    // -------------------------------------------------------------------------

    /**
     * Returns all published schedules assigned to a given employee.
     * Used by UC2 — Employee Views Personal Calendar.
     * Only returns published schedules — drafts are not visible to assistants.
     *
     * @param employeeId the employee ID
     * @return 200 OK with the list of published schedules for the employee
     */
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ASSISTANT', 'HR', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<ScheduleDto>> getSchedulesByEmployee(
            @PathVariable Long employeeId,
            Authentication authentication) {
        assertCanAccessEmployeeSchedule(employeeId, authentication);
        return ResponseEntity.ok(scheduleService.getSchedulesByEmployee(employeeId));
    }

    // -------------------------------------------------------------------------
    // POST /api/schedules/{scheduleId}/teams/{userId}/employees/{employeeId}
    // -------------------------------------------------------------------------

    /**
     * Assigns an employee to a team lead's team on this schedule.
     *
     * @param scheduleId the schedule to update
     * @param userId     the team lead (User) ID
     * @param employeeId the Employee ID to assign
     * @return 200 OK with the updated ScheduleDto
     */
    @PostMapping("/{scheduleId}/teams/{userId}/employees/{employeeId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ScheduleDto> assignEmployeeToTeam(@PathVariable Long scheduleId,
                                                            @PathVariable Long userId,
                                                            @PathVariable Long employeeId) {
        return ResponseEntity.ok(
                scheduleService.assignEmployeeToTeam(scheduleId, userId, employeeId));
    }

    // -------------------------------------------------------------------------
    // DELETE /api/schedules/{scheduleId}/teams/{userId}/employees/{employeeId}
    // -------------------------------------------------------------------------

    /**
     * Removes an employee from a team lead's team on this schedule.
     *
     * @param scheduleId the schedule to update
     * @param userId     the team lead (User) ID
     * @param employeeId the Employee ID to remove
     * @return 200 OK with the updated ScheduleDto
     */
    @DeleteMapping("/{scheduleId}/teams/{userId}/employees/{employeeId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ScheduleDto> removeEmployeeFromTeam(@PathVariable Long scheduleId,
                                                              @PathVariable Long userId,
                                                              @PathVariable Long employeeId) {
        return ResponseEntity.ok(
                scheduleService.removeEmployeeFromTeam(scheduleId, userId, employeeId));
    }

    @PostMapping("/{scheduleId}/teams/{teamId}/resources/{resourceId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ScheduleDto> assignResourceToTeam(@PathVariable Long scheduleId,
                                                            @PathVariable Long teamId,
                                                            @PathVariable Long resourceId) {
        return ResponseEntity.ok(
                scheduleService.assignResourceToTeam(scheduleId, teamId, resourceId));
    }

    @DeleteMapping("/{scheduleId}/teams/{teamId}/resources/{resourceId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ScheduleDto> removeResourceFromTeam(@PathVariable Long scheduleId,
                                                              @PathVariable Long teamId,
                                                              @PathVariable Long resourceId) {
        return ResponseEntity.ok(
                scheduleService.removeResourceFromTeam(scheduleId, teamId, resourceId));
    }

    @PutMapping("/{scheduleId}/teams/{teamId}/employees/{employeeId}/partial-day")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ScheduleDto> updateEmployeePartialDayNote(
            @PathVariable Long scheduleId,
            @PathVariable Long teamId,
            @PathVariable Long employeeId,
            @RequestBody PartialDayNoteRequest request) {
        return ResponseEntity.ok(scheduleService.updateAssignmentPartialDayNote(
                scheduleId, teamId, employeeId, false,
                request == null ? null : request.note()));
    }

    @PutMapping("/{scheduleId}/teams/{teamId}/resources/{resourceId}/partial-day")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ScheduleDto> updateResourcePartialDayNote(
            @PathVariable Long scheduleId,
            @PathVariable Long teamId,
            @PathVariable Long resourceId,
            @RequestBody PartialDayNoteRequest request) {
        return ResponseEntity.ok(scheduleService.updateAssignmentPartialDayNote(
                scheduleId, teamId, resourceId, true,
                request == null ? null : request.note()));
    }

    // -------------------------------------------------------------------------
    // PATCH /api/schedules/{id}/publish — publish
    // -------------------------------------------------------------------------

    /**
     * Publishes a schedule.
     *
     * @param id the schedule ID
     * @return 200 OK with the updated ScheduleDto
     */
    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ScheduleDto> publishSchedule(@PathVariable Long id) {
        return ResponseEntity.ok(scheduleService.publishSchedule(id));
    }

    /**
     * Returns all published schedules assigned to a given employee by name.
     * Used by UC2 — Employee Views Personal Calendar.
     *
     * @param employeeName the full name of the employee e.g. "Jane Smith"
     * @return 200 OK with the list of published schedules for the employee
     */
    @GetMapping("/employee/name/{employeeName}")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<ScheduleDto>> getSchedulesByEmployeeName(
            @PathVariable String employeeName) {
        return ResponseEntity.ok(scheduleService.getSchedulesByEmployeeName(employeeName));
    }

    private void assertCanAccessEmployeeSchedule(Long employeeId, Authentication authentication) {
        if (hasAnyAuthority(authentication, "ROLE_HR", "ROLE_MANAGER", "ROLE_ADMIN")) {
            return;
        }

        EmployeeDto employee = employeeService.getEmployeeById(employeeId);
        String principal = authentication != null ? authentication.getName() : null;
        boolean ownsSchedule = principal != null
                && (principal.equals(employee.getUsername()) || principal.equals(employee.getEmail()));

        if (!ownsSchedule) {
            throw new AccessDeniedException("Employees may only view their own schedule.");
        }
    }

    private boolean hasAnyAuthority(Authentication authentication, String... authorities) {
        if (authentication == null) return false;
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> {
                    String value = grantedAuthority.getAuthority();
                    for (String authority : authorities) {
                        if (authority.equals(value)) return true;
                    }
                    return false;
                });
    }
}
