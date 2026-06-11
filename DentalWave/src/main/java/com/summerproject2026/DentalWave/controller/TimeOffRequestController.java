package com.summerproject2026.DentalWave.controller;

import com.summerproject2026.DentalWave.dto.TimeOffRequestDto;
import com.summerproject2026.DentalWave.enums.RequestStatus;
import com.summerproject2026.DentalWave.service.TimeOffRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller exposing endpoints for managing time-off requests.
 *
 * <p>Base path: {@code /api/time-off-requests}</p>
 *
 * <p>Delegates all business logic to {@link TimeOffRequestService}
 * and returns appropriate HTTP status codes with each response.</p>
 */
@RestController
@RequestMapping("/api/time-off-requests")
public class TimeOffRequestController {

    private final TimeOffRequestService timeOffRequestService;

    /**
     * Constructs the controller with its required dependency.
     *
     * @param timeOffRequestService the service handling TimeOffRequest business logic
     */
    public TimeOffRequestController(TimeOffRequestService timeOffRequestService) {
        this.timeOffRequestService = timeOffRequestService;
    }

    /**
     * Handles IllegalStateException — returned when a request is not in PENDING status.
     *
     * @param ex the exception thrown
     * @return 409 Conflict with the exception message
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalStateException(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    // POST /api/time-off-requests
    @PostMapping
    public ResponseEntity<TimeOffRequestDto> createTimeOffRequest(
            @RequestBody TimeOffRequestDto timeOffRequestDto) {
        TimeOffRequestDto createdRequest = timeOffRequestService
                .createTimeOffRequest(timeOffRequestDto);
        return new ResponseEntity<>(createdRequest, HttpStatus.CREATED);
    }

    // GET /api/time-off-requests/{id}
    @GetMapping("/{id}")
    public ResponseEntity<TimeOffRequestDto> getTimeOffRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(timeOffRequestService.getTimeOffRequestById(id));
    }

    // GET /api/time-off-requests
    @GetMapping
    public ResponseEntity<List<TimeOffRequestDto>> getAllRequests() {
        return ResponseEntity.ok(timeOffRequestService.getAllRequests());
    }

    // GET /api/time-off-requests/employee/{employeeId}
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<TimeOffRequestDto>> getRequestsByEmployee(
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(timeOffRequestService.getRequestsByEmployee(employeeId));
    }

    // GET /api/time-off-requests/status/{status}
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TimeOffRequestDto>> getRequestsByStatus(
            @PathVariable RequestStatus status) {
        return ResponseEntity.ok(timeOffRequestService.getRequestsByStatus(status));
    }

    // PATCH /api/time-off-requests/{id}/approve
    @PatchMapping("/{id}/approve")
    public ResponseEntity<TimeOffRequestDto> approveRequest(
            @PathVariable Long id,
            @RequestParam Long reviewedById,
            @RequestParam(required = false) String reviewComment) {
        return ResponseEntity.ok(timeOffRequestService.approveRequest(id, reviewedById, reviewComment));
    }

    // PATCH /api/time-off-requests/{id}/deny
    @PatchMapping("/{id}/deny")
    public ResponseEntity<TimeOffRequestDto> denyRequest(
            @PathVariable Long id,
            @RequestParam Long reviewedById,
            @RequestParam(required = false) String reviewComment) {
        return ResponseEntity.ok(timeOffRequestService.denyRequest(id, reviewedById, reviewComment));
    }

    // DELETE /api/time-off-requests/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRequest(@PathVariable Long id) {
        timeOffRequestService.deleteRequest(id);
        return ResponseEntity.ok("TimeOffRequest with id " + id + " deleted successfully.");
    }
}