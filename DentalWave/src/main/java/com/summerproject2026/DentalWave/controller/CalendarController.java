package com.summerproject2026.DentalWave.controller;

import com.summerproject2026.DentalWave.dto.CalendarDto;
import com.summerproject2026.DentalWave.dto.ScheduleDto;
import com.summerproject2026.DentalWave.service.CalendarService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
 *  - Removing an employee from schedules within a specific date range
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

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<CalendarDto> createCalendar(@RequestBody CalendarDto calendarDto) {
        CalendarDto created = calendarService.createCalendar(calendarDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // -------------------------------------------------------------------------
    // GET /api/calendars/{id} — read one
    // -------------------------------------------------------------------------

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'ADMIN')")
    public ResponseEntity<CalendarDto> getCalendarById(@PathVariable Long id) {
        return ResponseEntity.ok(calendarService.getCalendarById(id));
    }

    // -------------------------------------------------------------------------
    // GET /api/calendars — read all
    // -------------------------------------------------------------------------

    @GetMapping
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<CalendarDto>> getAllCalendars() {
        return ResponseEntity.ok(calendarService.getAllCalendars());
    }

    // -------------------------------------------------------------------------
    // PUT /api/calendars/{id} — update
    // -------------------------------------------------------------------------

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<CalendarDto> updateCalendar(@PathVariable Long id,
                                                      @RequestBody CalendarDto calendarDto) {
        return ResponseEntity.ok(calendarService.updateCalendar(id, calendarDto));
    }

    // -------------------------------------------------------------------------
    // DELETE /api/calendars/{id} — delete
    // -------------------------------------------------------------------------

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<String> deleteCalendar(@PathVariable Long id) {
        calendarService.deleteCalendar(id);
        return ResponseEntity.ok("Calendar with id " + id + " deleted successfully.");
    }

    // -------------------------------------------------------------------------
    // PATCH /api/calendars/{id}/publish — publish
    // -------------------------------------------------------------------------

    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<CalendarDto> publishCalendar(@PathVariable Long id) {
        return ResponseEntity.ok(calendarService.publishCalendar(id));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<CalendarDto> publishCalendarPost(@PathVariable Long id) {
        return ResponseEntity.ok(calendarService.publishCalendar(id));
    }

    // -------------------------------------------------------------------------
    // PATCH /api/calendars/{id}/unpublish — unpublish
    // -------------------------------------------------------------------------

    @PatchMapping("/{id}/unpublish")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<CalendarDto> unpublishCalendar(@PathVariable Long id) {
        return ResponseEntity.ok(calendarService.unpublishCalendar(id));
    }

    @PostMapping("/{id}/unpublish")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<CalendarDto> unpublishCalendarPost(@PathVariable Long id) {
        return ResponseEntity.ok(calendarService.unpublishCalendar(id));
    }

    // -------------------------------------------------------------------------
    // GET /api/calendars/month/{month} — filter by month
    // -------------------------------------------------------------------------

    @GetMapping("/month/{month}")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<CalendarDto>> getCalendarsByMonth(@PathVariable String month) {
        return ResponseEntity.ok(calendarService.getCalendarsByMonth(month));
    }

    // -------------------------------------------------------------------------
    // GET /api/calendars/published — all published
    // -------------------------------------------------------------------------

    @GetMapping("/published")
    @PreAuthorize("hasAnyRole('ASSISTANT', 'HR', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<CalendarDto>> getPublishedCalendars() {
        return ResponseEntity.ok(calendarService.getPublishedCalendars());
    }

    // -------------------------------------------------------------------------
    // POST /api/calendars/{calendarId}/schedules — add schedule
    // -------------------------------------------------------------------------

    @PostMapping("/{calendarId}/schedules")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ScheduleDto> addSchedule(@PathVariable Long calendarId,
                                                   @RequestBody ScheduleDto scheduleDto) {
        ScheduleDto created = calendarService.addSchedule(calendarId, scheduleDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // -------------------------------------------------------------------------
    // DELETE /api/calendars/{calendarId}/schedules/{scheduleId} — remove schedule
    // -------------------------------------------------------------------------

    @DeleteMapping("/{calendarId}/schedules/{scheduleId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<String> removeSchedule(@PathVariable Long calendarId,
                                                 @PathVariable Long scheduleId) {
        calendarService.removeSchedule(calendarId, scheduleId);
        return ResponseEntity.ok("Schedule " + scheduleId
                + " removed from calendar " + calendarId + ".");
    }

    // -------------------------------------------------------------------------
    // POST /api/calendars/generate — auto-generate a draft calendar
    // -------------------------------------------------------------------------

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<CalendarDto> generateCalendar(@RequestBody CalendarDto calendarDto) {
        CalendarDto generated = calendarService.generateCalendar(calendarDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(generated);
    }

    // -------------------------------------------------------------------------
    // POST /api/calendars/{calendarId}/employees/{employeeId}/schedule-all
    // -------------------------------------------------------------------------

    @PostMapping("/{calendarId}/employees/{employeeId}/schedule-all")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<CalendarDto> scheduleEmployeeAcrossCalendar(
            @PathVariable Long calendarId,
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(
                calendarService.scheduleEmployeeAcrossCalendar(calendarId, employeeId));
    }

    // -------------------------------------------------------------------------
    // DELETE /api/calendars/{calendarId}/employees/{employeeId}/schedule-all
    // -------------------------------------------------------------------------

    @DeleteMapping("/{calendarId}/employees/{employeeId}/schedule-all")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<CalendarDto> removeEmployeeFromCalendar(
            @PathVariable Long calendarId,
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(
                calendarService.removeEmployeeFromCalendar(calendarId, employeeId));
    }

    // -------------------------------------------------------------------------
    // DELETE /api/calendars/office/{officeId}/employees/{employeeId}/dates
    // -------------------------------------------------------------------------

    /**
     * Removes an employee from every team on schedules that fall within
     * the given date range for the specified office.
     * Used by Manager "Approved Request" page — "Remove from Schedule" action.
     *
     * @param officeId   the office whose calendars to search
     * @param employeeId the employee to remove
     * @param startDate  start of the time-off range (ISO date e.g. 2026-06-15)
     * @param endDate    end of the time-off range (ISO date e.g. 2026-06-20)
     * @return 200 OK with confirmation message
     */
    @DeleteMapping("/office/{officeId}/employees/{employeeId}/dates")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<String> removeEmployeeFromScheduleOnDates(
            @PathVariable Long officeId,
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        calendarService.removeEmployeeFromScheduleOnDates(
                officeId, employeeId, startDate, endDate);
        return ResponseEntity.ok("Employee " + employeeId
                + " removed from schedules between " + startDate + " and " + endDate + ".");
    }
}
