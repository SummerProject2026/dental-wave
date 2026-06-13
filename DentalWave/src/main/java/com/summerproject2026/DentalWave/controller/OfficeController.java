package com.summerproject2026.DentalWave.controller;

import com.summerproject2026.DentalWave.dto.OfficeDto;
import com.summerproject2026.DentalWave.service.OfficeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller exposing CRUD endpoints for managing offices.
 *
 * <p>Base path: {@code /api/offices}</p>
 *
 * <p>Delegates all business logic to {@link OfficeService}
 * and returns appropriate HTTP status codes with each response.</p>
 */
@RestController
@RequestMapping("/api/offices")
public class OfficeController {

    private final OfficeService officeService;

    /**
     * Constructs the controller with its required dependency.
     * Constructor injection is preferred over field injection
     * for testability and immutability.
     *
     * @param officeService the service handling Office business logic
     */
    public OfficeController(OfficeService officeService) {
        this.officeService = officeService;
    }

    // -------------------------
    // POST /api/offices
    // -------------------------

    /**
     * Creates a new office.
     *
     * <p>Accepts an {@link OfficeDto} in the request body,
     * delegates creation to the service, and returns the
     * persisted office with its generated ID.</p>
     *
     * @param officeDto the office data from the request body; must not be null
     * @return {@code 201 Created} with the created {@link OfficeDto} in the body
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('HR', 'ADMIN', 'MANAGER')")
    public ResponseEntity<OfficeDto> createOffice(@RequestBody OfficeDto officeDto) {
        OfficeDto createdOffice = officeService.createOffice(officeDto);
        return new ResponseEntity<>(createdOffice, HttpStatus.CREATED);
    }

    // -------------------------
    // GET /api/offices/{id}
    // -------------------------

    /**
     * Retrieves a single office by its unique ID.
     *
     * @param id the ID of the office to retrieve, extracted from the path
     * @return {@code 200 OK} with the matching {@link OfficeDto} in the body,
     *         or {@code 404 Not Found} if no office exists with the given ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN', 'MANAGER')")
    public ResponseEntity<OfficeDto> getOfficeById(@PathVariable Long id) {
        OfficeDto office = officeService.getOfficeById(id);
        return ResponseEntity.ok(office);
    }

    // -------------------------
    // GET /api/offices
    // -------------------------

    /**
     * Retrieves all offices.
     *
     * @return {@code 200 OK} with a {@link List} of all {@link OfficeDto}
     *         objects in the body; returns an empty list if none exist
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('HR', 'ADMIN', 'MANAGER')")
    public ResponseEntity<List<OfficeDto>> getAllOffices() {
        List<OfficeDto> offices = officeService.getAllOffices();
        return ResponseEntity.ok(offices);
    }

    // -------------------------
    // PUT /api/offices/{id}
    // -------------------------

    /**
     * Updates an existing office identified by the given ID.
     *
     * <p>Accepts updated office data in the request body and applies
     * it to the existing record. The ID in the path takes precedence
     * over any ID present in the request body.</p>
     *
     * @param id        the ID of the office to update, extracted from the path
     * @param officeDto the updated office data from the request body
     * @return {@code 200 OK} with the updated {@link OfficeDto} in the body,
     *         or {@code 404 Not Found} if no office exists with the given ID
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN', 'MANAGER')")
    public ResponseEntity<OfficeDto> updateOffice(@PathVariable Long id,
                                                  @RequestBody OfficeDto officeDto) {
        OfficeDto updatedOffice = officeService.updateOffice(id, officeDto);
        return ResponseEntity.ok(updatedOffice);
    }

    // -------------------------
    // DELETE /api/offices/{id}
    // -------------------------

    /**
     * Deletes the office with the given ID.
     *
     * @param id the ID of the office to delete, extracted from the path
     * @return {@code 200 OK} with a confirmation message in the body,
     *         or {@code 404 Not Found} if no office exists with the given ID
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN', 'MANAGER')")
    public ResponseEntity<String> deleteOffice(@PathVariable Long id) {
        officeService.deleteOffice(id);
        return ResponseEntity.ok("Office with id " + id + " deleted successfully.");
    }
}