package com.summerproject2026.DentalWave.controller;

import com.summerproject2026.DentalWave.dto.AvailabilityDto;
import com.summerproject2026.DentalWave.dto.EmployeeDto;
import com.summerproject2026.DentalWave.dto.CreateEmployeeDto;
import com.summerproject2026.DentalWave.enums.WorkStatus;
import com.summerproject2026.DentalWave.repository.EmployeeRepository;
import com.summerproject2026.DentalWave.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller exposing all employee management endpoints.
 * Base path: /api/employees
 */
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeController(EmployeeService employeeService,
                               EmployeeRepository employeeRepository) {
        this.employeeService    = employeeService;
        this.employeeRepository = employeeRepository;
    }

    // ------------------------------------------------------------------ //
    // POST /api/employees — create
    // ------------------------------------------------------------------ //

    /**
     * Creates a new employee.
     * Request body must include a valid userId referencing an existing User.
     *
     * @return 201 Created with the persisted EmployeeDto
     */
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @PostMapping
    public ResponseEntity<EmployeeDto> createEmployee(@RequestBody CreateEmployeeDto createEmployeeDto) {
        System.out.println("EMPLOYEE CONTROLLER HIT");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(employeeService.createEmployee(createEmployeeDto));
    }

    // ------------------------------------------------------------------ //
    // GET /api/employees/{id} — read one
    // ------------------------------------------------------------------ //

    /** Returns a single employee by primary key. */
    @PreAuthorize("hasAnyRole('HR', 'ADMIN', 'MANAGER')")
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDto> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    // ------------------------------------------------------------------ //
    // GET /api/employees — read all
    // ------------------------------------------------------------------ //

    /** Returns all employees. */
    @PreAuthorize("hasAnyRole('HR', 'ADMIN', 'MANAGER')")
    @GetMapping
    public ResponseEntity<List<EmployeeDto>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    // ------------------------------------------------------------------ //
    // PUT /api/employees/{id} — update
    // ------------------------------------------------------------------ //

    /**
     * Updates an existing employee's HR fields.
     * The linked User account is NOT changeable through this endpoint.
     * Anyone can update the certain parts of an employee, such as email, and password.
     */
    @PreAuthorize("hasAnyRole('ASSISTANT', 'MANAGER', 'HR', 'ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDto> updateEmployee(@PathVariable Long id,
                                                       @RequestBody EmployeeDto employeeDto) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, employeeDto));
    }

    // ------------------------------------------------------------------ //
    // DELETE /api/employees/{id} — delete
    // ------------------------------------------------------------------ //

    /**
     * Deletes an employee and all cascaded records.
     * This should only be ablt to be used by HR or Admin.
     *
     */
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok("Employee with id " + id + " deleted successfully.");
    }

    // ------------------------------------------------------------------ //
    // GET /api/employees/office/{officeId} — filter by office
    // ------------------------------------------------------------------ //

    /** Returns all employees assigned to a specific office. */
    @PreAuthorize("hasAnyRole('HR', 'ADMIN', 'MANAGER')")
    @GetMapping("/office/{officeId}")
    public ResponseEntity<List<EmployeeDto>> getEmployeesByOffice(@PathVariable Long officeId) {
        return ResponseEntity.ok(employeeService.getEmployeesByOffice(officeId));
    }

    // ------------------------------------------------------------------ //
    // GET /api/employees/status/{status} — filter by work status
    // ------------------------------------------------------------------ //

    /**
     * Returns all employees with the given WorkStatus.
     * Example: GET /api/employees/status/ACTIVE
     */
    @PreAuthorize("hasAnyRole('HR', 'ADMIN', 'MANAGER')")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<EmployeeDto>> getEmployeesByStatus(
            @PathVariable WorkStatus status) {
        return ResponseEntity.ok(employeeService.getEmployeesByStatus(status));
    }

    // ------------------------------------------------------------------ //
    // GET /api/employees/search?keyword= — keyword search
    // ------------------------------------------------------------------ //

    /**
     * Keyword search across first name, last name, email, and position.
     * Case-insensitive LIKE match.
     * Example: GET /api/employees/search?keyword=jane
     *
     * Note: the searchByKeyword JPQL query lives in EmployeeRepository.
     * The controller delegates to it directly and maps the results via
     * the service's getById to avoid duplicating mapper logic.
     */
    @PreAuthorize("hasAnyRole('HR', 'ADMIN', 'MANAGER')")
    @GetMapping("/search")
    public ResponseEntity<List<EmployeeDto>> searchEmployees(@RequestParam String keyword) {
        List<EmployeeDto> results = employeeRepository.searchByKeyword(keyword).stream()
                .map(employee -> employeeService.getEmployeeById(employee.getId()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(results);
    }

    // ------------------------------------------------------------------ //
    // POST /api/employees/{employeeId}/availability — add availability
    // ------------------------------------------------------------------ //

    /**
     * Adds a new availability window to an employee's weekly schedule.
     *
     * @return 201 Created with the new AvailabilityDto
     */
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @PostMapping("/{employeeId}/availability")
    public ResponseEntity<AvailabilityDto> addAvailability(
            @PathVariable Long employeeId,
            @RequestBody AvailabilityDto availabilityDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(employeeService.addAvailability(employeeId, availabilityDto));
    }

    // ------------------------------------------------------------------ //
    // PUT /api/employees/{employeeId}/availability/{availabilityId} — update
    // ------------------------------------------------------------------ //

    /**
     * Updates an existing availability record belonging to the employee.
     * Returns 404 if the availability doesn't belong to this employee.
     */
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @PutMapping("/{employeeId}/availability/{availabilityId}")
    public ResponseEntity<AvailabilityDto> updateAvailability(
            @PathVariable Long employeeId,
            @PathVariable Long availabilityId,
            @RequestBody AvailabilityDto availabilityDto) {
        return ResponseEntity.ok(
                employeeService.updateAvailability(employeeId, availabilityId, availabilityDto));
    }

    // ------------------------------------------------------------------ //
    // DELETE /api/employees/{employeeId}/availability/{availabilityId} — delete
    // ------------------------------------------------------------------ //

    /** Removes an availability record from an employee. */
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @DeleteMapping("/{employeeId}/availability/{availabilityId}")
    public ResponseEntity<String> deleteAvailability(
            @PathVariable Long employeeId,
            @PathVariable Long availabilityId) {
        employeeService.deleteAvailability(employeeId, availabilityId);
        return ResponseEntity.ok(
                "Availability " + availabilityId + " removed from employee " + employeeId + ".");
    }
}
