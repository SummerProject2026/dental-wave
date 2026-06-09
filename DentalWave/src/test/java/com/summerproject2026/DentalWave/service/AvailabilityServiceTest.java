package com.summerproject2026.DentalWave.service.impl;

import com.summerproject2026.DentalWave.dto.AvailabilityDto;
import com.summerproject2026.DentalWave.exception.ResourceNotFoundException;
import com.summerproject2026.DentalWave.mapper.AvailabilityMapper;
import com.summerproject2026.DentalWave.entity.Availability;
import com.summerproject2026.DentalWave.entity.Employee;
import com.summerproject2026.DentalWave.repository.AvailabilityRepository;
import com.summerproject2026.DentalWave.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AvailabilityServiceImpl.
 *
 * All dependencies are mocked with Mockito — no Spring context, no database.
 * Each test exercises a single method in isolation.
 */
@ExtendWith(MockitoExtension.class)
class AvailabilityServiceImplTest {

    @Mock
    private AvailabilityRepository availabilityRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AvailabilityMapper availabilityMapper;

    @InjectMocks
    private AvailabilityServiceImpl availabilityService;

    // ── Shared fixtures ───────────────────────────────────────────────────────

    private Employee employee;
    private Availability availability;
    private AvailabilityDto availabilityDto;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setPosition("Dental Hygienist");

        availability = new Availability();
        availability.setId(10L);
        availability.setEmployee(employee);
        availability.setDayOfWeek(DayOfWeek.MONDAY);
        availability.setStartTime(LocalTime.of(8, 0));
        availability.setEndTime(LocalTime.of(16, 0));
        availability.setAvailable(true);

        availabilityDto = new AvailabilityDto();
        availabilityDto.setEmployeeId(1L);
        availabilityDto.setDayOfWeek(DayOfWeek.MONDAY);
        availabilityDto.setStartTime(LocalTime.of(8, 0));
        availabilityDto.setEndTime(LocalTime.of(16, 0));
        availabilityDto.setAvailable(true);
    }

    // -------------------------------------------------------------------------
    // createAvailability
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("createAvailability — persists record and returns mapped DTO")
    void createAvailability_success() {
        when(availabilityMapper.mapToAvailability(availabilityDto)).thenReturn(availability);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(availabilityRepository.save(availability)).thenReturn(availability);
        when(availabilityMapper.mapToAvailabilityDto(availability)).thenReturn(availabilityDto);

        AvailabilityDto result = availabilityService.createAvailability(availabilityDto);

        assertThat(result).isEqualTo(availabilityDto);
        verify(availabilityRepository).save(availability);
        verify(availability::setEmployee);  // employee was hydrated
    }

    @Test
    @DisplayName("createAvailability — throws ResourceNotFoundException when employee not found")
    void createAvailability_employeeNotFound_throws() {
        when(availabilityMapper.mapToAvailability(availabilityDto)).thenReturn(availability);
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> availabilityService.createAvailability(availabilityDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Employee not found with id: 1");

        verify(availabilityRepository, never()).save(any());
    }

    @Test
    @DisplayName("createAvailability — skips employee lookup when employeeId is null")
    void createAvailability_nullEmployeeId_skipsLookup() {
        availabilityDto.setEmployeeId(null);
        when(availabilityMapper.mapToAvailability(availabilityDto)).thenReturn(availability);
        when(availabilityRepository.save(availability)).thenReturn(availability);
        when(availabilityMapper.mapToAvailabilityDto(availability)).thenReturn(availabilityDto);

        availabilityService.createAvailability(availabilityDto);

        verify(employeeRepository, never()).findById(any());
        verify(availabilityRepository).save(availability);
    }

    // -------------------------------------------------------------------------
    // getAvailabilityByEmployee
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getAvailabilityByEmployee — returns mapped list for existing employee")
    void getAvailabilityByEmployee_success() {
        Availability avail2 = new Availability();
        avail2.setId(11L);
        avail2.setEmployee(employee);
        avail2.setDayOfWeek(DayOfWeek.TUESDAY);

        AvailabilityDto dto2 = new AvailabilityDto();
        dto2.setDayOfWeek(DayOfWeek.TUESDAY);

        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(availabilityRepository.findByEmployeeId(1L)).thenReturn(List.of(availability, avail2));
        when(availabilityMapper.mapToAvailabilityDto(availability)).thenReturn(availabilityDto);
        when(availabilityMapper.mapToAvailabilityDto(avail2)).thenReturn(dto2);

        List<AvailabilityDto> result = availabilityService.getAvailabilityByEmployee(1L);

        assertThat(result).hasSize(2);
        verify(availabilityRepository).findByEmployeeId(1L);
    }

    @Test
    @DisplayName("getAvailabilityByEmployee — throws ResourceNotFoundException when employee not found")
    void getAvailabilityByEmployee_employeeNotFound_throws() {
        when(employeeRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> availabilityService.getAvailabilityByEmployee(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Employee not found with id: 99");

        verify(availabilityRepository, never()).findByEmployeeId(any());
    }

    @Test
    @DisplayName("getAvailabilityByEmployee — returns empty list when employee has no records")
    void getAvailabilityByEmployee_noRecords_returnsEmpty() {
        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(availabilityRepository.findByEmployeeId(1L)).thenReturn(List.of());

        List<AvailabilityDto> result = availabilityService.getAvailabilityByEmployee(1L);

        assertThat(result).isEmpty();
    }

    // -------------------------------------------------------------------------
    // updateAvailability
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("updateAvailability — updates fields and returns mapped DTO")
    void updateAvailability_success() {
        AvailabilityDto updateDto = new AvailabilityDto();
        updateDto.setEmployeeId(1L);
        updateDto.setDayOfWeek(DayOfWeek.WEDNESDAY);
        updateDto.setStartTime(LocalTime.of(9, 0));
        updateDto.setEndTime(LocalTime.of(17, 0));
        updateDto.setAvailable(false);

        AvailabilityDto updatedResult = new AvailabilityDto();
        updatedResult.setDayOfWeek(DayOfWeek.WEDNESDAY);

        when(availabilityRepository.findById(10L)).thenReturn(Optional.of(availability));
        when(availabilityRepository.save(availability)).thenReturn(availability);
        when(availabilityMapper.mapToAvailabilityDto(availability)).thenReturn(updatedResult);

        AvailabilityDto result = availabilityService.updateAvailability(10L, updateDto);

        assertThat(result.getDayOfWeek()).isEqualTo(DayOfWeek.WEDNESDAY);
        verify(availabilityRepository).save(availability);
    }

    @Test
    @DisplayName("updateAvailability — re-assigns employee when a different employeeId is provided")
    void updateAvailability_reassignsEmployee() {
        Employee newEmployee = new Employee();
        newEmployee.setId(2L);

        AvailabilityDto updateDto = new AvailabilityDto();
        updateDto.setEmployeeId(2L); // different from current employee (id=1)
        updateDto.setDayOfWeek(DayOfWeek.FRIDAY);
        updateDto.setStartTime(LocalTime.of(8, 0));
        updateDto.setEndTime(LocalTime.of(12, 0));
        updateDto.setAvailable(true);

        when(availabilityRepository.findById(10L)).thenReturn(Optional.of(availability));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(newEmployee));
        when(availabilityRepository.save(availability)).thenReturn(availability);
        when(availabilityMapper.mapToAvailabilityDto(availability)).thenReturn(availabilityDto);

        availabilityService.updateAvailability(10L, updateDto);

        verify(employeeRepository).findById(2L);
        assertThat(availability.getEmployee().getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("updateAvailability — throws ResourceNotFoundException when record not found")
    void updateAvailability_recordNotFound_throws() {
        when(availabilityRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> availabilityService.updateAvailability(99L, availabilityDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Availability not found with id: 99");

        verify(availabilityRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateAvailability — throws when new employeeId does not exist")
    void updateAvailability_newEmployeeNotFound_throws() {
        AvailabilityDto updateDto = new AvailabilityDto();
        updateDto.setEmployeeId(99L);
        updateDto.setDayOfWeek(DayOfWeek.MONDAY);
        updateDto.setStartTime(LocalTime.of(8, 0));
        updateDto.setEndTime(LocalTime.of(16, 0));
        updateDto.setAvailable(true);

        when(availabilityRepository.findById(10L)).thenReturn(Optional.of(availability));
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> availabilityService.updateAvailability(10L, updateDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Employee not found with id: 99");
    }

    // -------------------------------------------------------------------------
    // deleteAvailability
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("deleteAvailability — deletes record when it exists")
    void deleteAvailability_success() {
        when(availabilityRepository.findById(10L)).thenReturn(Optional.of(availability));

        availabilityService.deleteAvailability(10L);

        verify(availabilityRepository).delete(availability);
    }

    @Test
    @DisplayName("deleteAvailability — throws ResourceNotFoundException when record not found")
    void deleteAvailability_recordNotFound_throws() {
        when(availabilityRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> availabilityService.deleteAvailability(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Availability not found with id: 99");

        verify(availabilityRepository, never()).delete(any());
    }
}