package com.summerproject2026.DentalWave.controller;

import com.summerproject2026.DentalWave.dto.ScheduleDto;
import com.summerproject2026.DentalWave.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

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

    @Autowired
    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
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
    public ResponseEntity<List<ScheduleDto>> getSchedulesByEmployee(
            @PathVariable Long employeeId) {
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
    public ResponseEntity<ScheduleDto> removeEmployeeFromTeam(@PathVariable Long scheduleId,
                                                              @PathVariable Long userId,
                                                              @PathVariable Long employeeId) {
        return ResponseEntity.ok(
                scheduleService.removeEmployeeFromTeam(scheduleId, userId, employeeId));
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
    public ResponseEntity<List<ScheduleDto>> getSchedulesByEmployeeName(
            @PathVariable String employeeName) {
        return ResponseEntity.ok(scheduleService.getSchedulesByEmployeeName(employeeName));
    }
}