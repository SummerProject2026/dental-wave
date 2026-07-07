package com.summerproject2026.DentalWave.controller;

import com.summerproject2026.DentalWave.dto.TimeOffRequestDto;
import com.summerproject2026.DentalWave.entity.Employee;
import com.summerproject2026.DentalWave.entity.User;
import com.summerproject2026.DentalWave.enums.RequestStatus;
import com.summerproject2026.DentalWave.exception.ResourceNotFoundException;
import com.summerproject2026.DentalWave.repository.EmployeeRepository;
import com.summerproject2026.DentalWave.repository.UserRepository;
import com.summerproject2026.DentalWave.service.TimeOffRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    /**
     * Constructs the controller with its required dependency.
     *
     * @param timeOffRequestService the service handling TimeOffRequest business logic
     */
    public TimeOffRequestController(TimeOffRequestService timeOffRequestService,
                                    EmployeeRepository employeeRepository,
                                    UserRepository userRepository) {
        this.timeOffRequestService = timeOffRequestService;
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
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

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "You are not authorized to perform this action."));
    }

    // POST /api/time-off-requests
    /**
     * Creates a new time-off request for an employee.
     *
     * Assistants may only create requests for themselves. HR, managers,
     * and admins may create or view requests for employees as part of
     * office workflow support.
     *
     * @param timeOffRequestDto request details from the frontend
     * @param authentication authenticated user making the request
     * @return the created request with PENDING status
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ASSISTANT', 'HR', 'MANAGER', 'ADMIN')")
    public ResponseEntity<TimeOffRequestDto> createTimeOffRequest(
            @RequestBody TimeOffRequestDto timeOffRequestDto,
            Authentication authentication) {
        assertCanAccessEmployeeRequests(authentication, timeOffRequestDto.getEmployeeId());
        TimeOffRequestDto createdRequest = timeOffRequestService
                .createTimeOffRequest(timeOffRequestDto);
        return new ResponseEntity<>(createdRequest, HttpStatus.CREATED);
    }

    // GET /api/time-off-requests/{id}
    /**
     * Gets one time-off request by id.
     *
     * Assistants may only view their own requests. HR, managers, and admins
     * may view employee requests for review and scheduling purposes.
     *
     * @param id request id
     * @param authentication authenticated user making the request
     * @return the matching time-off request
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ASSISTANT', 'HR', 'MANAGER', 'ADMIN')")
    public ResponseEntity<TimeOffRequestDto> getTimeOffRequestById(@PathVariable Long id,
                                                                    Authentication authentication) {
        TimeOffRequestDto request = timeOffRequestService.getTimeOffRequestById(id);
        assertCanAccessEmployeeRequests(authentication, request.getEmployeeId());
        return ResponseEntity.ok(request);
    }

    // GET /api/time-off-requests
    @GetMapping
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<TimeOffRequestDto>> getAllRequests() {
        return ResponseEntity.ok(timeOffRequestService.getAllRequests());
    }

    // GET /api/time-off-requests/employee/{employeeId}
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ASSISTANT', 'HR', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<TimeOffRequestDto>> getRequestsByEmployee(
            @PathVariable Long employeeId,
            Authentication authentication) {
        assertCanAccessEmployeeRequests(authentication, employeeId);
        return ResponseEntity.ok(timeOffRequestService.getRequestsByEmployee(employeeId));
    }

    // GET /api/time-off-requests/status/{status}
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<TimeOffRequestDto>> getRequestsByStatus(
            @PathVariable RequestStatus status) {
        return ResponseEntity.ok(timeOffRequestService.getRequestsByStatus(status));
    }

    // PATCH /api/time-off-requests/{id}/approve
    /**
     * Approves a pending time-off request.
     *
     * The reviewer is taken from the authenticated JWT user, not from the
     * frontend, so users cannot spoof who reviewed a request.
     *
     * @param id request id
     * @param reviewComment optional comment shown to the employee
     * @param authentication authenticated HR or admin reviewer
     * @return the approved request
     */
    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<TimeOffRequestDto> approveRequest(
            @PathVariable Long id,
            @RequestParam(required = false) String reviewComment,
            Authentication authentication) {
        Long reviewerId = getAuthenticatedReviewerId(authentication);
        return ResponseEntity.ok(timeOffRequestService.approveRequest(id, reviewerId, reviewComment));
    }

    // PATCH /api/time-off-requests/{id}/deny
    /**
     * Denies a pending time-off request.
     *
     * The reviewer is taken from the authenticated JWT user, not from the
     * frontend, so users cannot spoof who reviewed a request.
     *
     * @param id request id
     * @param reviewComment optional comment shown to the employee
     * @param authentication authenticated HR or admin reviewer
     * @return the denied request
     */
    @PatchMapping("/{id}/deny")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<TimeOffRequestDto> denyRequest(
            @PathVariable Long id,
            @RequestParam(required = false) String reviewComment,
            Authentication authentication) {
        Long reviewerId = getAuthenticatedReviewerId(authentication);
        return ResponseEntity.ok(timeOffRequestService.denyRequest(id, reviewerId, reviewComment));
    }

    // DELETE /api/time-off-requests/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ASSISTANT', 'HR', 'ADMIN')")
    public ResponseEntity<String> deleteRequest(@PathVariable Long id,
                                                Authentication authentication) {
        TimeOffRequestDto request = timeOffRequestService.getTimeOffRequestById(id);
        assertCanAccessEmployeeRequests(authentication, request.getEmployeeId());
        timeOffRequestService.deleteRequest(id);
        return ResponseEntity.ok("TimeOffRequest with id " + id + " deleted successfully.");
    }

    /**
     * Verifies that the current user can access requests for an employee.
     *
     * HR, managers, and admins can access all employee requests. Assistants
     * are limited to their own employee record.
     *
     * @param authentication current authenticated user
     * @param employeeId employee whose requests are being accessed
     */
    private void assertCanAccessEmployeeRequests(Authentication authentication, Long employeeId) {
        if (hasAnyAuthority(authentication, "ROLE_HR", "ROLE_MANAGER", "ROLE_ADMIN")) {
            return;
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found with id: " + employeeId));

        User employeeUser = employee.getUser();
        String principal = authentication != null ? authentication.getName() : null;
        boolean ownsRequest = employeeUser != null
                && (employeeUser.getUsername().equals(principal)
                || employeeUser.getEmail().equals(principal));

        if (!ownsRequest) {
            throw new AccessDeniedException("Employees may only access their own time-off requests.");
        }
    }

    /**
     * Finds the User id for the authenticated reviewer.
     *
     * @param authentication current authenticated HR or admin user
     * @return database id of the reviewer
     */
    private Long getAuthenticatedReviewerId(Authentication authentication) {
        String principal = authentication != null ? authentication.getName() : null;
        if (principal == null || principal.isBlank()) {
            throw new AccessDeniedException("No authenticated reviewer was found.");
        }

        return userRepository.findByUsername(principal)
                .or(() -> userRepository.findByEmail(principal))
                .map(User::getId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Authenticated reviewer was not found."));
    }

    /**
     * Checks whether the current user has any of the given authorities.
     *
     * @param authentication current authenticated user
     * @param authorities role authorities such as ROLE_HR
     * @return true when the user has at least one listed authority
     */
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
