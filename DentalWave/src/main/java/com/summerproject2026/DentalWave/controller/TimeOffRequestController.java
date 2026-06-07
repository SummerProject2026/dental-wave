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
     * Constructor injection is preferred over field injection
     * for testability and immutability.
     *
     * @param timeOffRequestService the service handling TimeOffRequest business logic
     */
    public TimeOffRequestController(TimeOffRequestService timeOffRequestService) {
        this.timeOffRequestService = timeOffRequestService;
    }

    // -------------------------
    // POST /api/time-off-requests
    // -------------------------

    /**
     * Creates a new time-off request.
     *
     * <p>Accepts a {@link TimeOffRequestDto} in the request body.
     * The status is automatically set to PENDING and the submission
     * timestamp is set server-side regardless of what the client sends.</p>
     *
     * @param timeOffRequestDto the request data from the request body; must not be null
     * @return {@code 201 Created} with the created {@link TimeOffRequestDto} in the body
     */
    @PostMapping
    public ResponseEntity<TimeOffRequestDto> createTimeOffRequest(
            @RequestBody TimeOffRequestDto timeOffRequestDto) {
        TimeOffRequestDto createdRequest = timeOffRequestService
                .createTimeOffRequest(timeOffRequestDto);
        return new ResponseEntity<>(createdRequest, HttpStatus.CREATED);
    }

    // -------------------------
    // GET /api/time-off-requests/{id}
    // -------------------------

    /**
     * Retrieves a single time-off request by its unique ID.
     *
     * @param id the ID of the request to retrieve, extracted from the path
     * @return {@code 200 OK} with the matching {@link TimeOffRequestDto} in the body,
     *         or {@code 404 Not Found} if no request exists with the given ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<TimeOffRequestDto> getTimeOffRequestById(@PathVariable Long id) {
        TimeOffRequestDto request = timeOffRequestService.getTimeOffRequestById(id);
        return ResponseEntity.ok(request);
    }

    // -------------------------
    // GET /api/time-off-requests
    // -------------------------

    /**
     * Retrieves all time-off requests.
     *
     * @return {@code 200 OK} with a {@link List} of all {@link TimeOffRequestDto}
     *         objects in the body; returns an empty list if none exist
     */
    @GetMapping
    public ResponseEntity<List<TimeOffRequestDto>> getAllRequests() {
        List<TimeOffRequestDto> requests = timeOffRequestService.getAllRequests();
        return ResponseEntity.ok(requests);
    }

    // -------------------------
    // GET /api/time-off-requests/employee/{employeeId}
    // -------------------------

    /**
     * Retrieves all time-off requests submitted by a specific employee.
     *
     * @param employeeId the ID of the employee, extracted from the path
     * @return {@code 200 OK} with a {@link List} of matching {@link TimeOffRequestDto}
     *         objects in the body; returns an empty list if none found,
     *         or {@code 404 Not Found} if no employee exists with the given ID
     */
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<TimeOffRequestDto>> getRequestsByEmployee(
            @PathVariable Long employeeId) {
        List<TimeOffRequestDto> requests = timeOffRequestService
                .getRequestsByEmployee(employeeId);
        return ResponseEntity.ok(requests);
    }

    // -------------------------
    // GET /api/time-off-requests/status/{status}
    // -------------------------

    /**
     * Retrieves all time-off requests filtered by status.
     *
     * <p>Example: {@code GET /api/time-off-requests/status/PENDING}
     * returns all pending requests for a manager approval queue.</p>
     *
     * @param status the {@link RequestStatus} to filter by, extracted from the path
     * @return {@code 200 OK} with a {@link List} of matching {@link TimeOffRequestDto}
     *         objects in the body; returns an empty list if none found
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TimeOffRequestDto>> getRequestsByStatus(
            @PathVariable RequestStatus status) {
        List<TimeOffRequestDto> requests = timeOffRequestService
                .getRequestsByStatus(status);
        return ResponseEntity.ok(requests);
    }

    // -------------------------
    // PATCH /api/time-off-requests/{id}/approve
    // -------------------------

    /**
     * Approves a pending time-off request.
     *
     * <p>Uses {@code PATCH} rather than {@code PUT} since only the
     * status, reviewer, timestamp, and comment fields are being
     * changed — not the full resource.</p>
     *
     * <p>Example: {@code PATCH /api/time-off-requests/5/approve?reviewedById=2
     * &reviewComment=Approved}</p>
     *
     * @param id             the ID of the request to approve, extracted from the path
     * @param reviewedById   the ID of the approving user, from the query parameter
     * @param reviewComment  an optional comment from the reviewer, from the query parameter
     * @return {@code 200 OK} with the updated {@link TimeOffRequestDto} in the body,
     *         or {@code 404 Not Found} if the request or reviewer does not exist,
     *         or {@code 409 Conflict} if the request is not in PENDING status
     */
    @PatchMapping("/{id}/approve")
    public ResponseEntity<TimeOffRequestDto> approveRequest(
            @PathVariable Long id,
            @RequestParam Long reviewedById,
            @RequestParam(required = false) String reviewComment) {
        TimeOffRequestDto updatedRequest = timeOffRequestService
                .approveRequest(id, reviewedById, reviewComment);
        return ResponseEntity.ok(updatedRequest);
    }

    // -------------------------
    // PATCH /api/time-off-requests/{id}/deny
    // -------------------------

    /**
     * Denies a pending time-off request.
     *
     * <p>Uses {@code PATCH} rather than {@code PUT} since only the
     * status, reviewer, timestamp, and comment fields are being
     * changed — not the full resource.</p>
     *
     * <p>Example: {@code PATCH /api/time-off-requests/5/deny?reviewedById=2
     * &reviewComment=Insufficient+coverage}</p>
     *
     * @param id             the ID of the request to deny, extracted from the path
     * @param reviewedById   the ID of the denying user, from the query parameter
     * @param reviewComment  an optional comment from the reviewer, from the query parameter
     * @return {@code 200 OK} with the updated {@link TimeOffRequestDto} in the body,
     *         or {@code 404 Not Found} if the request or reviewer does not exist,
     *         or {@code 409 Conflict} if the request is not in PENDING status
     */
    @PatchMapping("/{id}/deny")
    public ResponseEntity<TimeOffRequestDto> denyRequest(
            @PathVariable Long id,
            @RequestParam Long reviewedById,
            @RequestParam(required = false) String reviewComment) {
        TimeOffRequestDto updatedRequest = timeOffRequestService
                .denyRequest(id, reviewedById, reviewComment);
        return ResponseEntity.ok(updatedRequest);
    }

    // -------------------------
    // DELETE /api/time-off-requests/{id}
    // -------------------------

    /**
     * Deletes the time-off request with the given ID.
     *
     * @param id the ID of the request to delete, extracted from the path
     * @return {@code 200 OK} with a confirmation message in the body,
     *         or {@code 404 Not Found} if no request exists with the given ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRequest(@PathVariable Long id) {
        timeOffRequestService.deleteRequest(id);
        return ResponseEntity.ok("TimeOffRequest with id " + id + " deleted successfully.");
    }
}