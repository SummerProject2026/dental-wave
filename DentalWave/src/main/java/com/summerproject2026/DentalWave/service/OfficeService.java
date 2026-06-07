package com.summerproject2026.DentalWave.service;

import com.summerproject2026.DentalWave.dto.OfficeDto;

import java.util.List;

/**
 * Service interface defining the business operations
 * available for managing {@link com.yourpackage.entity.Office} entities.
 *
 * <p>The service layer sits between the controller and repository,
 * handling business logic, validation, and entity-to-DTO mapping.
 * The concrete implementation is {@link OfficeServiceImpl}.</p>
 */
public interface OfficeService {

    /**
     * Creates a new office and persists it to the database.
     *
     * @param officeDto the data for the new office; must not be null
     * @return the created office as a {@link OfficeDto} with its generated ID
     */
    OfficeDto createOffice(OfficeDto officeDto);

    /**
     * Retrieves a single office by its unique ID.
     *
     * @param id the ID of the office to retrieve; must not be null
     * @return the matching office as an {@link OfficeDto}
     * @throws com.yourpackage.exception.ResourceNotFoundException
     *         if no office exists with the given ID
     */
    OfficeDto getOfficeById(Long id);

    /**
     * Retrieves all offices stored in the database.
     *
     * @return a {@link List} of all offices as {@link OfficeDto} objects;
     *         returns an empty list if none exist
     */
    List<OfficeDto> getAllOffices();

    /**
     * Updates an existing office identified by the given ID
     * with the data provided in the DTO.
     *
     * @param id        the ID of the office to update; must not be null
     * @param officeDto the updated office data; must not be null
     * @return the updated office as an {@link OfficeDto}
     * @throws com.yourpackage.exception.ResourceNotFoundException
     *         if no office exists with the given ID
     */
    OfficeDto updateOffice(Long id, OfficeDto officeDto);

    /**
     * Deletes the office with the given ID from the database.
     *
     * @param id the ID of the office to delete; must not be null
     * @throws com.yourpackage.exception.ResourceNotFoundException
     *         if no office exists with the given ID
     */
    void deleteOffice(Long id);
}