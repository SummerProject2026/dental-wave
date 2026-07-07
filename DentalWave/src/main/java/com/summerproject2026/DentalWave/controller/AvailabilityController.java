package com.summerproject2026.DentalWave.controller;

import com.summerproject2026.DentalWave.dto.AvailabilityDto;
import com.summerproject2026.DentalWave.dto.EmployeeDto;
import com.summerproject2026.DentalWave.service.AvailabilityService;
import com.summerproject2026.DentalWave.service.EmployeeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller exposing availability management endpoints.
 * Base path: /api/availability
 *
 * Responsibilities:
 *  - Create / update / delete individual availability records
 *  - Query all records for a specific employee
 */
@RestController
@RequestMapping("/api/availability")
public class AvailabilityController {

    private final AvailabilityService availabilityService;
    private final EmployeeService employeeService;

    @Autowired
    public AvailabilityController(AvailabilityService availabilityService,
                                  EmployeeService employeeService) {
        this.availabilityService = availabilityService;
        this.employeeService = employeeService;
    }

    // -------------------------------------------------------------------------
    // POST /api/availability — create
    // -------------------------------------------------------------------------

    /**
     * Creates a new availability record for an employee.
     *
     * @param availabilityDto the availability data from the request body
     * @return 201 Created with the persisted AvailabilityDto
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'ADMIN')")
    public ResponseEntity<AvailabilityDto> createAvailability(
            @RequestBody AvailabilityDto availabilityDto) {
        AvailabilityDto created = availabilityService.createAvailability(availabilityDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // -------------------------------------------------------------------------
    // GET /api/availability/{id} — read one by availability ID
    // -------------------------------------------------------------------------

    /**
     * Returns a single availability record by its own ID.
     * Delegates to getAvailabilityByEmployee for single-record look-up isn't
     * in the service interface; this endpoint can be added to the service if needed.
     * For now we expose a simple read-by-id via a dedicated mapping.
     *
     * @param id the availability record ID
     * @return 200 OK with the AvailabilityDto
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'ADMIN')")
    public ResponseEntity<AvailabilityDto> getAvailabilityById(@PathVariable Long id) {
        return ResponseEntity.ok(availabilityService.getAvailabilityById(id));
    }

    // -------------------------------------------------------------------------
    // GET /api/availability/employee/{employeeId} — read by employee
    // -------------------------------------------------------------------------

    /**
     * Returns all availability records for a specific employee.
     *
     * @param employeeId the employee whose records to retrieve
     * @return 200 OK with the list of AvailabilityDtos
     */
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ASSISTANT', 'HR', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<AvailabilityDto>> getAvailabilityByEmployee(
            @PathVariable Long employeeId,
            Authentication authentication) {
        assertCanAccessEmployeeAvailability(employeeId, authentication);
        return ResponseEntity.ok(availabilityService.getAvailabilityByEmployee(employeeId));
    }

    // -------------------------------------------------------------------------
    // PUT /api/availability/{id} — update
    // -------------------------------------------------------------------------

    /**
     * Updates an existing availability record.
     *
     * @param id              the ID of the record to update
     * @param availabilityDto the new values from the request body
     * @return 200 OK with the updated AvailabilityDto
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'ADMIN')")
    public ResponseEntity<AvailabilityDto> updateAvailability(
            @PathVariable Long id,
            @RequestBody AvailabilityDto availabilityDto) {
        return ResponseEntity.ok(availabilityService.updateAvailability(id, availabilityDto));
    }

    // -------------------------------------------------------------------------
    // DELETE /api/availability/{id} — delete
    // -------------------------------------------------------------------------

    /**
     * Deletes an availability record by ID.
     *
     * @param id the record ID to delete
     * @return 200 OK with a confirmation message
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'ADMIN')")
    public ResponseEntity<String> deleteAvailability(@PathVariable Long id) {
        availabilityService.deleteAvailability(id);
        return ResponseEntity.ok("Availability record with id " + id + " deleted successfully.");
    }

    private void assertCanAccessEmployeeAvailability(Long employeeId, Authentication authentication) {
        if (hasAnyAuthority(authentication, "ROLE_HR", "ROLE_MANAGER", "ROLE_ADMIN")) {
            return;
        }

        EmployeeDto employee = employeeService.getEmployeeById(employeeId);
        String principal = authentication != null ? authentication.getName() : null;
        boolean ownsAvailability = principal != null
                && (principal.equals(employee.getUsername()) || principal.equals(employee.getEmail()));

        if (!ownsAvailability) {
            throw new AccessDeniedException("Employees may only view their own availability.");
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
