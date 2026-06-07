package com.summerproject2026.DentalWave.service.impl;

import com.summerproject2026.DentalWave.dto.AvailabilityDto;
import com.summerproject2026.DentalWave.dto.EmployeeDto;
import com.summerproject2026.DentalWave.entity.Availability;
import com.summerproject2026.DentalWave.entity.Employee;
import com.summerproject2026.DentalWave.entity.Office;
import com.summerproject2026.DentalWave.entity.User;
import com.summerproject2026.DentalWave.enums.WorkStatus;
import com.summerproject2026.DentalWave.exception.ResourceNotFoundException;
import com.summerproject2026.DentalWave.mapper.AvailabilityMapper;
import com.summerproject2026.DentalWave.mapper.EmployeeMapper;
import com.summerproject2026.DentalWave.repository.AvailabilityRepository;
import com.summerproject2026.DentalWave.repository.EmployeeRepository;
import com.summerproject2026.DentalWave.repository.OfficeRepository;
import com.summerproject2026.DentalWave.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EmployeeServiceImpl.
 *
 * All collaborators are mocked — no Spring context or database.
 */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock private EmployeeRepository     employeeRepository;
    @Mock private UserRepository         userRepository;
    @Mock private OfficeRepository       officeRepository;
    @Mock private AvailabilityRepository availabilityRepository;
    @Mock private EmployeeMapper         employeeMapper;
    @Mock private AvailabilityMapper     availabilityMapper;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    // ── Fixtures ──────────────────────────────────────────────────────────────

    private User user;
    private Office office;
    private Employee employee;
    private EmployeeDto employeeDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setFirstName("Alice");
        user.setLastName("Smith");
        user.setEmail("alice@dentalwave.com");

        office = new Office();
        office.setId(10L);
        office.setName("Downtown Clinic");

        employee = new Employee();
        employee.setId(100L);
        employee.setUser(user);
        employee.setPosition("Dental Hygienist");
        employee.setStatus(WorkStatus.ACTIVE);
        employee.setHireDate(LocalDate.of(2020, 1, 15));
        employee.setOffices(new ArrayList<>(List.of(office)));

        Office officeDto = new Office();
        officeDto.setId(10L);

        employeeDto = new EmployeeDto();
        employeeDto.setId(100L);
        employeeDto.setUserId(1L);
        employeeDto.setPosition("Dental Hygienist");
        employeeDto.setStatus(WorkStatus.ACTIVE);
        employeeDto.setHireDate(LocalDate.of(2020, 1, 15));
        employeeDto.setOffices(List.of(officeDto));
    }

    // -------------------------------------------------------------------------
    // createEmployee
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("createEmployee — persists employee with resolved user and offices")
    void createEmployee_success() {
        when(employeeMapper.mapToEmployee(employeeDto)).thenReturn(employee);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(officeRepository.findById(10L)).thenReturn(Optional.of(office));
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeMapper.mapToEmployeeDto(employee)).thenReturn(employeeDto);

        EmployeeDto result = employeeService.createEmployee(employeeDto);

        assertThat(result).isEqualTo(employeeDto);
        verify(employeeRepository).save(employee);
        verify(userRepository).findById(1L);
        verify(officeRepository).findById(10L);
    }

    @Test
    @DisplayName("createEmployee — throws ResourceNotFoundException when user not found")
    void createEmployee_userNotFound_throws() {
        when(employeeMapper.mapToEmployee(employeeDto)).thenReturn(employee);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.createEmployee(employeeDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id: 1");

        verify(employeeRepository, never()).save(any());
    }

    @Test
    @DisplayName("createEmployee — throws ResourceNotFoundException when office not found")
    void createEmployee_officeNotFound_throws() {
        when(employeeMapper.mapToEmployee(employeeDto)).thenReturn(employee);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(officeRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.createEmployee(employeeDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Office not found with id: 10");

        verify(employeeRepository, never()).save(any());
    }

    @Test
    @DisplayName("createEmployee — succeeds with empty offices list")
    void createEmployee_emptyOffices_success() {
        employeeDto.setOffices(List.of());
        when(employeeMapper.mapToEmployee(employeeDto)).thenReturn(employee);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeMapper.mapToEmployeeDto(employee)).thenReturn(employeeDto);

        EmployeeDto result = employeeService.createEmployee(employeeDto);

        assertThat(result).isNotNull();
        verify(officeRepository, never()).findById(any());
    }

    // -------------------------------------------------------------------------
    // getEmployeeById
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getEmployeeById — returns mapped DTO for existing employee")
    void getEmployeeById_success() {
        when(employeeRepository.findById(100L)).thenReturn(Optional.of(employee));
        when(employeeMapper.mapToEmployeeDto(employee)).thenReturn(employeeDto);

        EmployeeDto result = employeeService.getEmployeeById(100L);

        assertThat(result).isEqualTo(employeeDto);
    }

    @Test
    @DisplayName("getEmployeeById — throws ResourceNotFoundException when not found")
    void getEmployeeById_notFound_throws() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getEmployeeById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Employee not found with id: 99");
    }

    // -------------------------------------------------------------------------
    // getAllEmployees
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getAllEmployees — returns mapped list of all employees")
    void getAllEmployees_success() {
        Employee emp2 = new Employee();
        emp2.setId(101L);
        EmployeeDto dto2 = new EmployeeDto();
        dto2.setId(101L);

        when(employeeRepository.findAll()).thenReturn(List.of(employee, emp2));
        when(employeeMapper.mapToEmployeeDto(employee)).thenReturn(employeeDto);
        when(employeeMapper.mapToEmployeeDto(emp2)).thenReturn(dto2);

        List<EmployeeDto> result = employeeService.getAllEmployees();

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("getAllEmployees — returns empty list when no employees exist")
    void getAllEmployees_empty() {
        when(employeeRepository.findAll()).thenReturn(List.of());

        List<EmployeeDto> result = employeeService.getAllEmployees();

        assertThat(result).isEmpty();
    }

    // -------------------------------------------------------------------------
    // getEmployeesByOffice
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getEmployeesByOffice — returns employees for the given office")
    void getEmployeesByOffice_success() {
        when(employeeRepository.findByOfficeId(10L)).thenReturn(List.of(employee));
        when(employeeMapper.mapToEmployeeDto(employee)).thenReturn(employeeDto);

        List<EmployeeDto> result = employeeService.getEmployeesByOffice(10L);

        assertThat(result).hasSize(1);
        verify(employeeRepository).findByOfficeId(10L);
    }

    // -------------------------------------------------------------------------
    // getEmployeesByStatus
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getEmployeesByStatus — returns employees matching the given status")
    void getEmployeesByStatus_success() {
        when(employeeRepository.findByStatus(WorkStatus.ACTIVE)).thenReturn(List.of(employee));
        when(employeeMapper.mapToEmployeeDto(employee)).thenReturn(employeeDto);

        List<EmployeeDto> result = employeeService.getEmployeesByStatus(WorkStatus.ACTIVE);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(WorkStatus.ACTIVE);
    }

    @Test
    @DisplayName("getEmployeesByStatus — returns empty list when no employees match")
    void getEmployeesByStatus_noMatch_returnsEmpty() {
        when(employeeRepository.findByStatus(WorkStatus.ON_LEAVE)).thenReturn(List.of());

        List<EmployeeDto> result = employeeService.getEmployeesByStatus(WorkStatus.ON_LEAVE);

        assertThat(result).isEmpty();
    }

    // -------------------------------------------------------------------------
    // updateEmployee
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("updateEmployee — updates fields and returns mapped DTO")
    void updateEmployee_success() {
        EmployeeDto updateDto = new EmployeeDto();
        updateDto.setPosition("Senior Hygienist");
        updateDto.setHireDate(LocalDate.of(2020, 1, 15));
        updateDto.setStatus(WorkStatus.ACTIVE);
        updateDto.setOffices(List.of());

        EmployeeDto updatedResult = new EmployeeDto();
        updatedResult.setPosition("Senior Hygienist");

        when(employeeRepository.findById(100L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeMapper.mapToEmployeeDto(employee)).thenReturn(updatedResult);

        EmployeeDto result = employeeService.updateEmployee(100L, updateDto);

        assertThat(result.getPosition()).isEqualTo("Senior Hygienist");
        verify(employeeRepository).save(employee);
    }

    @Test
    @DisplayName("updateEmployee — throws ResourceNotFoundException when employee not found")
    void updateEmployee_notFound_throws() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.updateEmployee(99L, employeeDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Employee not found with id: 99");
    }

    // -------------------------------------------------------------------------
    // deleteEmployee
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("deleteEmployee — deletes employee when found")
    void deleteEmployee_success() {
        when(employeeRepository.findById(100L)).thenReturn(Optional.of(employee));

        employeeService.deleteEmployee(100L);

        verify(employeeRepository).delete(employee);
    }

    @Test
    @DisplayName("deleteEmployee — throws ResourceNotFoundException when not found")
    void deleteEmployee_notFound_throws() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.deleteEmployee(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Employee not found with id: 99");

        verify(employeeRepository, never()).delete(any());
    }

    // -------------------------------------------------------------------------
    // addAvailability
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("addAvailability — wires back-reference and cascades save through employee")
    void addAvailability_success() {
        Availability availability = new Availability();
        availability.setDayOfWeek(DayOfWeek.MONDAY);
        availability.setStartTime(LocalTime.of(8, 0));
        availability.setEndTime(LocalTime.of(16, 0));
        availability.setAvailable(true);

        AvailabilityDto availabilityDto = new AvailabilityDto();
        availabilityDto.setDayOfWeek(DayOfWeek.MONDAY);

        when(employeeRepository.findById(100L)).thenReturn(Optional.of(employee));
        when(availabilityMapper.mapToAvailability(availabilityDto)).thenReturn(availability);
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(availabilityMapper.mapToAvailabilityDto(availability)).thenReturn(availabilityDto);

        AvailabilityDto result = employeeService.addAvailability(100L, availabilityDto);

        assertThat(result).isEqualTo(availabilityDto);
        verify(employeeRepository).save(employee);
        // back-reference wired via addAvailability()
        assertThat(employee.getAvailabilities()).contains(availability);
    }

    @Test
    @DisplayName("addAvailability — throws ResourceNotFoundException when employee not found")
    void addAvailability_employeeNotFound_throws() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.addAvailability(99L, new AvailabilityDto()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Employee not found with id: 99");
    }

    // -------------------------------------------------------------------------
    // updateAvailability (nested)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("updateAvailability — updates fields when availability belongs to employee")
    void updateAvailability_success() {
        Availability availability = new Availability();
        availability.setId(200L);
        availability.setEmployee(employee);
        availability.setDayOfWeek(DayOfWeek.MONDAY);
        availability.setStartTime(LocalTime.of(8, 0));
        availability.setEndTime(LocalTime.of(16, 0));
        availability.setAvailable(true);

        AvailabilityDto updateDto = new AvailabilityDto();
        updateDto.setDayOfWeek(DayOfWeek.WEDNESDAY);
        updateDto.setStartTime(LocalTime.of(9, 0));
        updateDto.setEndTime(LocalTime.of(17, 0));
        updateDto.setAvailable(false);

        AvailabilityDto updatedResult = new AvailabilityDto();
        updatedResult.setDayOfWeek(DayOfWeek.WEDNESDAY);

        when(employeeRepository.findById(100L)).thenReturn(Optional.of(employee));
        when(availabilityRepository.findById(200L)).thenReturn(Optional.of(availability));
        when(availabilityRepository.save(availability)).thenReturn(availability);
        when(availabilityMapper.mapToAvailabilityDto(availability)).thenReturn(updatedResult);

        AvailabilityDto result = employeeService.updateAvailability(100L, 200L, updateDto);

        assertThat(result.getDayOfWeek()).isEqualTo(DayOfWeek.WEDNESDAY);
        verify(availabilityRepository).save(availability);
    }

    @Test
    @DisplayName("updateAvailability — throws IllegalArgumentException when availability belongs to different employee")
    void updateAvailability_wrongEmployee_throws() {
        Employee otherEmployee = new Employee();
        otherEmployee.setId(999L);

        Availability availability = new Availability();
        availability.setId(200L);
        availability.setEmployee(otherEmployee); // belongs to a DIFFERENT employee

        when(employeeRepository.findById(100L)).thenReturn(Optional.of(employee));
        when(availabilityRepository.findById(200L)).thenReturn(Optional.of(availability));

        assertThatThrownBy(() -> employeeService.updateAvailability(100L, 200L, new AvailabilityDto()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Availability 200 does not belong to employee 100");
    }

    @Test
    @DisplayName("updateAvailability — throws ResourceNotFoundException when availability not found")
    void updateAvailability_availabilityNotFound_throws() {
        when(employeeRepository.findById(100L)).thenReturn(Optional.of(employee));
        when(availabilityRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.updateAvailability(100L, 99L, new AvailabilityDto()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Availability not found with id: 99");
    }

    // -------------------------------------------------------------------------
    // deleteAvailability (nested)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("deleteAvailability — removes availability via orphanRemoval")
    void deleteAvailability_success() {
        Availability availability = new Availability();
        availability.setId(200L);
        availability.setEmployee(employee);
        employee.addAvailability(availability);

        when(employeeRepository.findById(100L)).thenReturn(Optional.of(employee));
        when(availabilityRepository.findById(200L)).thenReturn(Optional.of(availability));
        when(employeeRepository.save(employee)).thenReturn(employee);

        employeeService.deleteAvailability(100L, 200L);

        verify(employeeRepository).save(employee);
        assertThat(employee.getAvailabilities()).doesNotContain(availability);
    }

    @Test
    @DisplayName("deleteAvailability — throws IllegalArgumentException when availability belongs to different employee")
    void deleteAvailability_wrongEmployee_throws() {
        Employee otherEmployee = new Employee();
        otherEmployee.setId(999L);

        Availability availability = new Availability();
        availability.setId(200L);
        availability.setEmployee(otherEmployee);

        when(employeeRepository.findById(100L)).thenReturn(Optional.of(employee));
        when(availabilityRepository.findById(200L)).thenReturn(Optional.of(availability));

        assertThatThrownBy(() -> employeeService.deleteAvailability(100L, 200L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Availability 200 does not belong to employee 100");

        verify(employeeRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteAvailability — throws ResourceNotFoundException when employee not found")
    void deleteAvailability_employeeNotFound_throws() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.deleteAvailability(99L, 200L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Employee not found with id: 99");
    }
}