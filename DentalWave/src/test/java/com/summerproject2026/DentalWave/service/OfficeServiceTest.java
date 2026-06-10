package com.summerproject2026.DentalWave.service.impl;

import com.summerproject2026.DentalWave.dto.OfficeDto;
import com.summerproject2026.DentalWave.entity.Office;
import com.summerproject2026.DentalWave.exception.ResourceNotFoundException;
import com.summerproject2026.DentalWave.mapper.OfficeMapper;
import com.summerproject2026.DentalWave.repository.OfficeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pure unit tests for {@link OfficeServiceImpl}.
 *
 * <p>All collaborators ({@link OfficeRepository} and {@link OfficeMapper})
 * are replaced with Mockito mocks so this test class is completely
 * isolated from the database and the Spring context.
 * Tests run in milliseconds and are ideal for covering every code path
 * including error branches.</p>
 *
 * <p>{@code @ExtendWith(MockitoExtension.class)} initialises mocks before
 * each test and verifies no unexpected interactions occurred afterwards.</p>
 */
@ExtendWith(MockitoExtension.class)
class OfficeServiceImplTest {

    // -------------------------------------------------------------------------
    // Mocks & subject under test
    // -------------------------------------------------------------------------

    @Mock
    private OfficeRepository officeRepository;

    @Mock
    private OfficeMapper officeMapper;

    @InjectMocks
    private OfficeServiceImpl officeService;

    // -------------------------------------------------------------------------
    // Shared test data (rebuilt before each test to avoid mutation side-effects)
    // -------------------------------------------------------------------------

    private Office officeEntity;
    private OfficeDto officeDto;

    /**
     * Creates fresh shared Office entity and OfficeDto fixtures before each test.
     * This prevents one test's mutations from affecting another test.
     */
    @BeforeEach
    void setUp() {
        officeEntity = new Office();
        officeEntity.setId(1L);
        officeEntity.setName("Downtown Dental");
        officeEntity.setAddress("123 Main St, Charlotte, NC 28201");
        officeEntity.setPhoneNumber("704-555-0101");

        officeDto = new OfficeDto();
        officeDto.setId(1L);
        officeDto.setName("Downtown Dental");
        officeDto.setAddress("123 Main St, Charlotte, NC 28201");
        officeDto.setPhoneNumber("704-555-0101");
    }

    // =========================================================================
    // createOffice()
    // =========================================================================

    /**
     * Verifies that createOffice maps the incoming DTO to an entity,
     * saves the entity, and returns the saved office as a DTO.
     */
    @Test
    @DisplayName("createOffice() maps DTO to entity, saves it, and returns mapped DTO")
    void createOffice_mapsAndSavesAndReturnsDto() {
        when(officeMapper.mapToOffice(officeDto)).thenReturn(officeEntity);
        when(officeRepository.save(officeEntity)).thenReturn(officeEntity);
        when(officeMapper.mapToOfficeDto(officeEntity)).thenReturn(officeDto);

        OfficeDto result = officeService.createOffice(officeDto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Downtown Dental");

        // Verify collaboration order
        verify(officeMapper, times(1)).mapToOffice(officeDto);
        verify(officeRepository, times(1)).save(officeEntity);
        verify(officeMapper, times(1)).mapToOfficeDto(officeEntity);
    }

    /**
     * Verifies that createOffice does not swallow repository exceptions.
     * Any save failure should propagate to the caller.
     */
    @Test
    @DisplayName("createOffice() propagates any exception thrown by the repository")
    void createOffice_propagatesRepositoryException() {
        when(officeMapper.mapToOffice(officeDto)).thenReturn(officeEntity);
        when(officeRepository.save(any(Office.class)))
                .thenThrow(new RuntimeException("DB constraint violation"));

        assertThatThrownBy(() -> officeService.createOffice(officeDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB constraint violation");
    }

    // =========================================================================
    // getOfficeById()
    // =========================================================================

    /**
     * Verifies that getOfficeById returns a mapped DTO when the office exists.
     */
    @Test
    @DisplayName("getOfficeById() returns the DTO when the office exists")
    void getOfficeById_returnsDto_whenExists() {
        when(officeRepository.findById(1L)).thenReturn(Optional.of(officeEntity));
        when(officeMapper.mapToOfficeDto(officeEntity)).thenReturn(officeDto);

        OfficeDto result = officeService.getOfficeById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Downtown Dental");
    }

    /**
     * Verifies that getOfficeById throws ResourceNotFoundException when
     * no office exists for the requested ID.
     */
    @Test
    @DisplayName("getOfficeById() throws ResourceNotFoundException when the office does not exist")
    void getOfficeById_throwsResourceNotFoundException_whenNotExists() {
        when(officeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> officeService.getOfficeById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        // Mapper must never be called if the entity was not found
        verify(officeMapper, never()).mapToOfficeDto(any());
    }

    // =========================================================================
    // getAllOffices()
    // =========================================================================

    /**
     * Verifies that getAllOffices returns all persisted offices mapped to DTOs.
     */
    @Test
    @DisplayName("getAllOffices() returns a list of DTOs for all persisted offices")
    void getAllOffices_returnsListOfDtos() {
        Office office2 = new Office();
        office2.setId(2L);
        office2.setName("Uptown Smiles");

        OfficeDto officeDto2 = new OfficeDto();
        officeDto2.setId(2L);
        officeDto2.setName("Uptown Smiles");

        when(officeRepository.findAll()).thenReturn(List.of(officeEntity, office2));
        when(officeMapper.mapToOfficeDto(officeEntity)).thenReturn(officeDto);
        when(officeMapper.mapToOfficeDto(office2)).thenReturn(officeDto2);

        List<OfficeDto> results = officeService.getAllOffices();

        assertThat(results).hasSize(2);
        assertThat(results)
                .extracting(OfficeDto::getName)
                .containsExactlyInAnyOrder("Downtown Dental", "Uptown Smiles");
    }

    /**
     * Verifies that getAllOffices returns an empty list when the repository
     * contains no offices.
     */
    @Test
    @DisplayName("getAllOffices() returns an empty list when no offices exist")
    void getAllOffices_returnsEmptyList_whenNoneExist() {
        when(officeRepository.findAll()).thenReturn(List.of());

        List<OfficeDto> results = officeService.getAllOffices();

        assertThat(results).isEmpty();
        verify(officeMapper, never()).mapToOfficeDto(any());
    }

    // =========================================================================
    // updateOffice()
    // =========================================================================

    /**
     * Verifies that updateOffice copies all editable DTO fields onto the existing
     * entity, saves it, and returns the updated DTO.
     */
    @Test
    @DisplayName("updateOffice() applies all DTO fields onto the existing entity and returns updated DTO")
    void updateOffice_appliesChangesAndReturnsUpdatedDto() {
        OfficeDto updateRequest = new OfficeDto();
        updateRequest.setName("Downtown Dental – Relocated");
        updateRequest.setAddress("789 New St, Charlotte, NC 28203");
        updateRequest.setPhoneNumber("704-555-0303");

        OfficeDto updatedDto = new OfficeDto();
        updatedDto.setId(1L);
        updatedDto.setName("Downtown Dental – Relocated");
        updatedDto.setAddress("789 New St, Charlotte, NC 28203");
        updatedDto.setPhoneNumber("704-555-0303");

        when(officeRepository.findById(1L)).thenReturn(Optional.of(officeEntity));
        when(officeRepository.save(officeEntity)).thenReturn(officeEntity);
        when(officeMapper.mapToOfficeDto(officeEntity)).thenReturn(updatedDto);

        OfficeDto result = officeService.updateOffice(1L, updateRequest);

        // Verify the entity was mutated before saving
        assertThat(officeEntity.getName()).isEqualTo("Downtown Dental – Relocated");
        assertThat(officeEntity.getAddress()).isEqualTo("789 New St, Charlotte, NC 28203");
        assertThat(officeEntity.getPhoneNumber()).isEqualTo("704-555-0303");

        assertThat(result.getName()).isEqualTo("Downtown Dental – Relocated");
        verify(officeRepository, times(1)).save(officeEntity);
    }

    /**
     * Verifies that updateOffice throws ResourceNotFoundException when
     * the office to update does not exist.
     */
    @Test
    @DisplayName("updateOffice() throws ResourceNotFoundException when the office does not exist")
    void updateOffice_throwsResourceNotFoundException_whenNotExists() {
        when(officeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> officeService.updateOffice(99L, officeDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        // The repository must never attempt a save for a missing record
        verify(officeRepository, never()).save(any());
    }

    // =========================================================================
    // deleteOffice()
    // =========================================================================

    /**
     * Verifies that deleteOffice looks up the office first and then deletes
     * the found entity.
     */
    @Test
    @DisplayName("deleteOffice() fetches the entity, then deletes it")
    void deleteOffice_fetchesAndDeletes() {
        when(officeRepository.findById(1L)).thenReturn(Optional.of(officeEntity));

        officeService.deleteOffice(1L);

        verify(officeRepository, times(1)).findById(1L);
        verify(officeRepository, times(1)).delete(officeEntity);
    }

    /**
     * Verifies that deleteOffice throws ResourceNotFoundException when
     * the requested office does not exist.
     */
    @Test
    @DisplayName("deleteOffice() throws ResourceNotFoundException when the office does not exist")
    void deleteOffice_throwsResourceNotFoundException_whenNotExists() {
        when(officeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> officeService.deleteOffice(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        // delete must never be called if the entity was not found
        verify(officeRepository, never()).delete(any());
    }
}