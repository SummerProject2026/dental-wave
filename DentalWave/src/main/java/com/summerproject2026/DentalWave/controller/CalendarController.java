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
 */
@RestController
@RequestMapping("/api/calendars")
public class CalendarController {

    private final CalendarService calendarService;

    @Autowired
    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
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
}
