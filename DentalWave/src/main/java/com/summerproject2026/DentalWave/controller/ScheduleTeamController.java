package com.summerproject2026.DentalWave.controller;

import com.summerproject2026.DentalWave.dto.ScheduleTeamDto;
import com.summerproject2026.DentalWave.service.ScheduleTeamService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing schedule teams.
 * Handles creating teams, adding/removing employees, and retrieving teams.
 */
@RestController
@RequestMapping("/api/schedule-teams")
@AllArgsConstructor
public class ScheduleTeamController {

    // Service that contains the schedule team business logic
    private final ScheduleTeamService scheduleTeamService;

    // POST /api/schedule-teams - creates a new team
    @PostMapping
    public ResponseEntity<ScheduleTeamDto> createTeam(
            @RequestBody ScheduleTeamDto scheduleTeamDto) {
        return new ResponseEntity<>(
                scheduleTeamService.createTeam(scheduleTeamDto),
                HttpStatus.CREATED);
    }

    // GET /api/schedule-teams/{id} - gets a team by id
    @GetMapping("/{id}")
    public ResponseEntity<ScheduleTeamDto> getTeamById(@PathVariable Long id) {
        return ResponseEntity.ok(scheduleTeamService.getTeamById(id));
    }

    // GET /api/schedule-teams/schedule/{scheduleId} - gets all teams for a schedule
    @GetMapping("/schedule/{scheduleId}")
    public ResponseEntity<List<ScheduleTeamDto>> getTeamsBySchedule(
            @PathVariable Long scheduleId) {
        return ResponseEntity.ok(
                scheduleTeamService.getTeamsBySchedule(scheduleId));
    }

    // POST /api/schedule-teams/{teamId}/employees/{employeeId} - adds employee to team
    @PostMapping("/{teamId}/employees/{employeeId}")
    public ResponseEntity<ScheduleTeamDto> addEmployeeToTeam(
            @PathVariable Long teamId,
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(
                scheduleTeamService.addEmployeeToTeam(teamId, employeeId));
    }

    // DELETE /api/schedule-teams/{teamId}/employees/{employeeId} - removes employee from team
    @DeleteMapping("/{teamId}/employees/{employeeId}")
    public ResponseEntity<ScheduleTeamDto> removeEmployeeFromTeam(
            @PathVariable Long teamId,
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(
                scheduleTeamService.removeEmployeeFromTeam(teamId, employeeId));
    }

    // PUT /api/schedule-teams/{id}/name - updates team name
    @PutMapping("/{id}/name")
    public ResponseEntity<ScheduleTeamDto> updateTeamName(
            @PathVariable Long id,
            @RequestParam String name) {
        return ResponseEntity.ok(
                scheduleTeamService.updateTeamName(id, name));
    }

    // DELETE /api/schedule-teams/{id} - deletes a team
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeam(@PathVariable Long id) {
        scheduleTeamService.deleteTeam(id);
        return ResponseEntity.noContent().build();
    }
}
