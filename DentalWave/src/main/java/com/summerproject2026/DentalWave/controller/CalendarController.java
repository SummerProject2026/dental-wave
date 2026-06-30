package com.summerproject2026.DentalWave.controller;

import com.summerproject2026.DentalWave.dto.CalendarDto;
import com.summerproject2026.DentalWave.dto.ScheduleDto;
import com.summerproject2026.DentalWave.service.CalendarService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller exposing calendar management endpoints.
 * Base path: /api/calendars
 *
 * Responsibilities:
 *  - CRUD for calendars
 *  - Publish / unpublish lifecycle
 *  - Filtering by month or published state
 *  - Adding and removing nested schedules
 *  - Auto-generating draft calendars with role-based team assignment
 *  - Bulk scheduling/unscheduling an employee across an entire calendar
 */
@RestController
@RequestMapping("/api/calendars")
public class CalendarController {

    private final CalendarService calendarService;

    @Autowired
    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    /**
     * Handles IllegalStateException — returned when an action would
     * create an invalid or conflicting state, such as generating a
     * duplicate calendar for the same office and month.
     *
     * @param ex the exception thrown
     * @return 409 Conflict with the exception message
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalStateException(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    // -------------------------------------------------------------------------
    // POST /api/calendars — create
    // -------------------------------------------------------------------------

    /**
     * Creates a new calendar.
     *
     * @param calendarDto the calendar data from the request body
     * @return 201 Created with the persisted CalendarDto
     */
    @PostMapping
    public ResponseEntity<CalendarDto> createCalendar(@RequestBody CalendarDto calendarDto) {
        CalendarDto created = calendarService.createCalendar(calendarDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // -------------------------------------------------------------------------
    // GET /api/calendars/{id} — read one
    // -------------------------------------------------------------------------

    /**
     * Returns a single calendar by ID.
     *
     * @param id the calendar ID from the path
     * @return 200 OK with the CalendarDto, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<CalendarDto> getCalendarById(@PathVariable Long id) {
        return ResponseEntity.ok(calendarService.getCalendarById(id));
    }

    // -------------------------------------------------------------------------
    // GET /api/calendars — read all
    // -------------------------------------------------------------------------

    /**
     * Returns all calendars.
     *
     * @return 200 OK with the list (may be empty)
     */
    @GetMapping
    public ResponseEntity<List<CalendarDto>> getAllCalendars() {
        return ResponseEntity.ok(calendarService.getAllCalendars());
    }

    // -------------------------------------------------------------------------
    // PUT /api/calendars/{id} — update
    // -------------------------------------------------------------------------

    /**
     * Updates an existing calendar.
     *
     * @param id          the ID of the calendar to update
     * @param calendarDto the updated data from the request body
     * @return 200 OK with the updated CalendarDto
     */
    @PutMapping("/{id}")
    public ResponseEntity<CalendarDto> updateCalendar(@PathVariable Long id,
                                                      @RequestBody CalendarDto calendarDto) {
        return ResponseEntity.ok(calendarService.updateCalendar(id, calendarDto));
    }

    // -------------------------------------------------------------------------
    // DELETE /api/calendars/{id} — delete
    // -------------------------------------------------------------------------

    /**
     * Deletes a calendar by ID.
     *
     * @param id the calendar ID
     * @return 200 OK with a confirmation message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCalendar(@PathVariable Long id) {
        calendarService.deleteCalendar(id);
        return ResponseEntity.ok("Calendar with id " + id + " deleted successfully.");
    }

    // -------------------------------------------------------------------------
    // PATCH /api/calendars/{id}/publish — publish
    // -------------------------------------------------------------------------

    /**
     * Publishes a calendar, making it visible to staff.
     *
     * @param id the calendar ID
     * @return 200 OK with the updated CalendarDto (published = true)
     */
    @PatchMapping("/{id}/publish")
    public ResponseEntity<CalendarDto> publishCalendar(@PathVariable Long id) {
        return ResponseEntity.ok(calendarService.publishCalendar(id));
    }

    // -------------------------------------------------------------------------
    // PATCH /api/calendars/{id}/unpublish — unpublish
    // -------------------------------------------------------------------------

    /**
     * Reverts a calendar to draft state.
     *
     * @param id the calendar ID
     * @return 200 OK with the updated CalendarDto (published = false)
     */
    @PatchMapping("/{id}/unpublish")
    public ResponseEntity<CalendarDto> unpublishCalendar(@PathVariable Long id) {
        return ResponseEntity.ok(calendarService.unpublishCalendar(id));
    }

    // -------------------------------------------------------------------------
    // GET /api/calendars/month/{month} — filter by month
    // -------------------------------------------------------------------------

    /**
     * Returns all calendars for a given month label.
     *
     * @param month URL-encoded month label, e.g. "June%202025"
     * @return 200 OK with the matching list
     */
    @GetMapping("/month/{month}")
    public ResponseEntity<List<CalendarDto>> getCalendarsByMonth(@PathVariable String month) {
        return ResponseEntity.ok(calendarService.getCalendarsByMonth(month));
    }

    // -------------------------------------------------------------------------
    // GET /api/calendars/published — all published
    // -------------------------------------------------------------------------

    /**
     * Returns all published calendars.
     *
     * @return 200 OK with the list of published CalendarDtos
     */
    @GetMapping("/published")
    public ResponseEntity<List<CalendarDto>> getPublishedCalendars() {
        return ResponseEntity.ok(calendarService.getPublishedCalendars());
    }

    // -------------------------------------------------------------------------
    // POST /api/calendars/{calendarId}/schedules — add schedule
    // -------------------------------------------------------------------------

    /**
     * Adds a new schedule to a calendar.
     *
     * @param calendarId  the parent calendar ID
     * @param scheduleDto the schedule data from the request body
     * @return 201 Created with the new ScheduleDto
     */
    @PostMapping("/{calendarId}/schedules")
    public ResponseEntity<ScheduleDto> addSchedule(@PathVariable Long calendarId,
                                                   @RequestBody ScheduleDto scheduleDto) {
        ScheduleDto created = calendarService.addSchedule(calendarId, scheduleDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // -------------------------------------------------------------------------
    // DELETE /api/calendars/{calendarId}/schedules/{scheduleId} — remove schedule
    // -------------------------------------------------------------------------

    /**
     * Removes and deletes a schedule from a calendar.
     *
     * @param calendarId the parent calendar ID
     * @param scheduleId the schedule ID to remove
     * @return 200 OK with a confirmation message
     */
    @DeleteMapping("/{calendarId}/schedules/{scheduleId}")
    public ResponseEntity<String> removeSchedule(@PathVariable Long calendarId,
                                                 @PathVariable Long scheduleId) {
        calendarService.removeSchedule(calendarId, scheduleId);
        return ResponseEntity.ok("Schedule " + scheduleId + " removed from calendar " + calendarId + ".");
    }

    // -------------------------------------------------------------------------
    // POST /api/calendars/generate — auto-generate a draft calendar
    // -------------------------------------------------------------------------

    /**
     * Auto-generates a draft calendar for the given office and month.
     * Creates one schedule per weekday (Monday-Friday) in the date range,
     * and automatically assigns employees to teams based on role:
     * each team gets 1 Doctor + 1 TC, with Assistants distributed
     * as evenly as possible across the teams created.
     *
     * <p>Throws a 409 Conflict if a calendar already exists for the
     * given office and month.</p>
     *
     * @param calendarDto the office, month, date range, and creator info
     * @return 201 Created with the fully populated CalendarDto
     */
    @PostMapping("/generate")
    public ResponseEntity<CalendarDto> generateCalendar(@RequestBody CalendarDto calendarDto) {
        CalendarDto generated = calendarService.generateCalendar(calendarDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(generated);
    }

    // -------------------------------------------------------------------------
    // POST /api/calendars/{calendarId}/employees/{employeeId}/schedule-all
    // -------------------------------------------------------------------------

    /**
     * Schedules an employee across every day in the given calendar.
     * For each day's schedule, the employee is added to whichever
     * team currently has the fewest members, balancing team sizes.
     * Used by Manager "New Employee" tab — "Schedule" action.
     *
     * @param calendarId the calendar to schedule the employee into
     * @param employeeId the employee to schedule
     * @return 200 OK with the updated CalendarDto
     */
    @PostMapping("/{calendarId}/employees/{employeeId}/schedule-all")
    public ResponseEntity<CalendarDto> scheduleEmployeeAcrossCalendar(
            @PathVariable Long calendarId,
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(
                calendarService.scheduleEmployeeAcrossCalendar(calendarId, employeeId));
    }

    // -------------------------------------------------------------------------
    // DELETE /api/calendars/{calendarId}/employees/{employeeId}/schedule-all
    // -------------------------------------------------------------------------

    /**
     * Removes an employee from every team across every day in the
     * given calendar. Used by Manager "Removed Employee" tab —
     * "Remove from Schedule" action.
     *
     * @param calendarId the calendar to remove the employee from
     * @param employeeId the employee to remove
     * @return 200 OK with the updated CalendarDto
     */
    @DeleteMapping("/{calendarId}/employees/{employeeId}/schedule-all")
    public ResponseEntity<CalendarDto> removeEmployeeFromCalendar(
            @PathVariable Long calendarId,
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(
                calendarService.removeEmployeeFromCalendar(calendarId, employeeId));
    }
}