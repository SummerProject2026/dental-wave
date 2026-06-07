package com.summerproject2026.DentalWave.service.impl;

import com.summerproject2026.DentalWave.dto.OfficeDto;
import com.summerproject2026.DentalWave.entity.Office;
import com.summerproject2026.DentalWave.exception.ResourceNotFoundException;
import com.summerproject2026.DentalWave.mapper.OfficeMapper;
import com.summerproject2026.DentalWave.repository.OfficeRepository;
import com.summerproject2026.DentalWave.service.OfficeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of {@link OfficeService} containing the
 * business logic for managing office entities.
 *
 * <p>Delegates persistence operations to {@link OfficeRepository}
 * and uses {@link OfficeMapper} for entity-to-DTO conversion.</p>
 */
@Service
@Transactional
public class OfficeServiceImpl implements OfficeService {

    private final OfficeRepository officeRepository;
    private final OfficeMapper officeMapper;

    /**
     * Constructs the service with its required dependencies.
     * Constructor injection is preferred over field injection
     * for testability and immutability.
     *
     * @param officeRepository the repository for Office persistence
     * @param officeMapper     the mapper for Office entity-DTO conversion
     */
    public OfficeServiceImpl(OfficeRepository officeRepository,
                             OfficeMapper officeMapper) {
        this.officeRepository = officeRepository;
        this.officeMapper = officeMapper;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Maps the incoming DTO to an entity, persists it,
     * then maps the saved entity (with generated ID) back to a DTO.</p>
     */
    @Override
    public OfficeDto createOffice(OfficeDto officeDto) {
        Office office = officeMapper.mapToOffice(officeDto);
        Office savedOffice = officeRepository.save(office);
        return officeMapper.mapToOfficeDto(savedOffice);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Throws {@link ResourceNotFoundException} if the office
     * does not exist, so the controller can return a 404 response.</p>
     */
    @Override
    @Transactional(readOnly = true)
    public OfficeDto getOfficeById(Long id) {
        Office office = officeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Office not found with id: " + id));
        return officeMapper.mapToOfficeDto(office);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Marked as read-only to allow the JPA provider to
     * apply query optimizations (no dirty checking needed).</p>
     */
    @Override
    @Transactional(readOnly = true)
    public List<OfficeDto> getAllOffices() {
        return officeRepository.findAll()
                .stream()
                .map(officeMapper::mapToOfficeDto)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     *
     * <p>Fetches the existing entity first to confirm it exists,
     * then applies the updated fields from the DTO before saving.</p>
     */
    @Override
    public OfficeDto updateOffice(Long id, OfficeDto officeDto) {
        // Confirm the office exists before attempting an update
        Office existingOffice = officeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Office not found with id: " + id));

        // Apply updated fields from the DTO onto the existing entity
        existingOffice.setName(officeDto.getName());
        existingOffice.setAddress(officeDto.getAddress());
        existingOffice.setPhoneNumber(officeDto.getPhoneNumber());

        Office updatedOffice = officeRepository.save(existingOffice);
        return officeMapper.mapToOfficeDto(updatedOffice);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Confirms the office exists before deletion so the caller
     * receives a meaningful error rather than a silent no-op.</p>
     */
    @Override
    public void deleteOffice(Long id) {
        // Confirm the office exists before attempting deletion
        Office office = officeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Office not found with id: " + id));
        officeRepository.delete(office);
    }
}