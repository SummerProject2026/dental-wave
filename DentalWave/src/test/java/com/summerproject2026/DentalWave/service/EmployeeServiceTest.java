package com.summerproject2026.DentalWave.service.impl;

import com.summerproject2026.DentalWave.dto.AvailabilityDto;
import com.summerproject2026.DentalWave.dto.EmployeeDto;
import com.summerproject2026.DentalWave.dto.OfficeDto;
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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EmployeeServiceImpl.
 *
 * Uses Mockito to isolate the service from all repository and mapper dependencies.
 * Each nested class groups tests for one logical operation (create, read, update, etc.)
 * so it is easy to find tests for a specific behaviour.
 *
 * Test naming convention: methodName_condition_expectedResult
 */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    // ── Mocked dependencies ──────────────────────────────────────────────────
    @Mock private EmployeeRepository     employeeRepository;
    @Mock private UserRepository         userRepository;
    @Mock private OfficeRepository       officeRepository;
    @Mock private AvailabilityRepository availabilityRepository;
    @Mock private EmployeeMapper         employeeMapper;
    @Mock private AvailabilityMapper     availabilityMapper;

    // ── System under test ────────────────────────────────────────────────────
    @InjectMocks
    private EmployeeServiceImpl employeeService;

    // ── Shared test fixtures ─────────────────────────────────────────────────
    private User        user;
    private Employee    employee;
    private EmployeeDto employeeDto;
    private Office      office;
    private OfficeDto   officeDto;

    @BeforeEach
    void setUp() {
        // Build a minimal User used as the linked account
        user = new User();
        user.setId(1L);
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setEmail("jane.doe@dental.com");

        // Build a minimal Office
        office = new Office();
        office.setId(10L);
        office.setName("Main Branch");

        officeDto = new OfficeDto(10L, "Main Branch", "123 Main St");

        // Build the entity that the repository would return
        employee = new Employee();
        employee.setId(1L);
        employee.setUser(user);
        employee.setPosition("Dental Hygienist");
        employee.setHireDate(LocalDate.of(2023, 1, 15));
        employee.setTimeOff(10.0);
        employee.setStatus(WorkStatus.ACTIVE);
        employee.setOffices(List.of(office));

        // Build the DTO that comes in from the controller
        employeeDto = new EmployeeDto();
        employeeDto.setId(1L);
        employeeDto.setUserId(1L);
        employeeDto.setFirstName("Jane");
        employeeDto.setLastName("Doe");
        employeeDto.setEmail("jane.doe@dental.com");
        employeeDto.setPosition("Dental Hygienist");
        employeeDto.setHireDate(LocalDate.of(2023, 1, 15));
        employeeDto.setTimeOff(10.0);
        employeeDto.setStatus(WorkStatus.ACTIVE);
        employeeDto.setOffices(List.of(officeDto));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CREATE
    // ══════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("createEmployee")
    class CreateEmployee {

        @Test
        @DisplayName("Happy path – valid DTO with existing user saves and returns DTO")
        void createEmployee_validDto_returnsSavedDto() {
            // Arrange
            // The mapper converts the DTO to a stub entity (user not yet hydrated)
            when(employeeMapper.mapToEmployee(employeeDto)).thenReturn(employee);
            // The service then fetches the real User from the DB
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            // Office resolution
            when(officeRepository.findById(10L)).thenReturn(Optional.of(office));
            // Repository persist
            when(employeeRepository.save(employee)).thenReturn(employee);
            // Mapper produces the final DTO from the persisted entity
            when(employeeMapper.mapToEmployeeDto(employee)).thenReturn(employeeDto);

            // Act
            EmployeeDto result = employeeService.createEmployee(employeeDto);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getPosition()).isEqualTo("Dental Hygienist");
            // Verify the service wired in the managed User before saving
            verify(userRepository).findById(1L);
            verify(employeeRepository).save(employee);
        }

        @Test
        @DisplayName("User not found – throws ResourceNotFoundException and nothing is saved")
        void createEmployee_userNotFound_throwsResourceNotFoundException() {
            // Arrange
            when(employeeMapper.mapToEmployee(employeeDto)).thenReturn(employee);
            // Simulate missing User
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> employeeService.createEmployee(employeeDto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found with id: 1");

            // The repository should never be called when User resolution fails
            verify(employeeRepository, never()).save(any());
        }

        @Test
        @DisplayName("Office not found – throws ResourceNotFoundException and nothing is saved")
        void createEmployee_officeNotFound_throwsResourceNotFoundException() {
            // Arrange
            when(employeeMapper.mapToEmployee(employeeDto)).thenReturn(employee);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            // Simulate missing Office
            when(officeRepository.findById(10L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> employeeService.createEmployee(employeeDto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Office not found with id: 10");

            verify(employeeRepository, never()).save(any());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // READ
    // ══════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("getEmployeeById")
    class GetEmployeeById {

        @Test
        @DisplayName("Existing ID – returns mapped DTO")
        void getEmployeeById_existingId_returnsDto() {
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
            when(employeeMapper.mapToEmployeeDto(employee)).thenReturn(employeeDto);

            EmployeeDto result = employeeService.getEmployeeById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Non-existent ID – throws ResourceNotFoundException")
        void getEmployeeById_notFound_throwsResourceNotFoundException() {
            when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.getEmployeeById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Employee not found with id: 99");
        }
    }

    @Nested
    @DisplayName("getAllEmployees")
    class GetAllEmployees {

        @Test
        @DisplayName("Multiple employees exist – returns full list")
        void getAllEmployees_multipleExist_returnsList() {
            when(employeeRepository.findAll()).thenReturn(List.of(employee, employee));
            when(employeeMapper.mapToEmployeeDto(employee)).thenReturn(employeeDto);

            List<EmployeeDto> result = employeeService.getAllEmployees();

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("No employees in DB – returns empty list (not null)")
        void getAllEmployees_noneExist_returnsEmptyList() {
            when(employeeRepository.findAll()).thenReturn(Collections.emptyList());

            List<EmployeeDto> result = employeeService.getAllEmployees();

            assertThat(result).isNotNull().isEmpty();
        }
    }

    @Nested
    @DisplayName("getEmployeesByStatus")
    class GetEmployeesByStatus {

        @Test
        @DisplayName("ACTIVE status – returns only active employees")
        void getEmployeesByStatus_active_returnsFilteredList() {
            when(employeeRepository.findByStatus(WorkStatus.ACTIVE)).thenReturn(List.of(employee));
            when(employeeMapper.mapToEmployeeDto(employee)).thenReturn(employeeDto);

            List<EmployeeDto> result = employeeService.getEmployeesByStatus(WorkStatus.ACTIVE);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo(WorkStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("getEmployeesByOffice")
    class GetEmployeesByOffice {

        @Test
        @DisplayName("Valid officeId – returns employees assigned to that office")
        void getEmployeesByOffice_validId_returnsEmployees() {
            when(employeeRepository.findByOfficeId(10L)).thenReturn(List.of(employee));
            when(employeeMapper.mapToEmployeeDto(employee)).thenReturn(employeeDto);

            List<EmployeeDto> result = employeeService.getEmployeesByOffice(10L);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("No employees in office – returns empty list")
        void getEmployeesByOffice_noMatches_returnsEmptyList() {
            when(employeeRepository.findByOfficeId(99L)).thenReturn(Collections.emptyList());

            List<EmployeeDto> result = employeeService.getEmployeesByOffice(99L);

            assertThat(result).isEmpty();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // UPDATE
    // ══════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("updateEmployee")
    class UpdateEmployee {

        @Test
        @DisplayName("Valid update – scalar fields are applied and saved")
        void updateEmployee_validData_updatesAndReturnsDto() {
            // Arrange – existing record is found
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
            when(officeRepository.findById(10L)).thenReturn(Optional.of(office));
            when(employeeRepository.save(employee)).thenReturn(employee);

            EmployeeDto updatedDto = new EmployeeDto();
            updatedDto.setPosition("Orthodontist");
            updatedDto.setHireDate(LocalDate.of(2024, 3, 1));
            updatedDto.setTimeOff(15.0);
            updatedDto.setStatus(WorkStatus.ACTIVE);
            updatedDto.setOffices(List.of(officeDto));
            updatedDto.setResponsibilities(List.of("Patient care"));

            when(employeeMapper.mapToEmployeeDto(employee)).thenReturn(updatedDto);

            // Act
            EmployeeDto result = employeeService.updateEmployee(1L, updatedDto);

            // Assert – the entity's position was overwritten before save
            verify(employeeRepository).save(employee);
            assertThat(employee.getPosition()).isEqualTo("Orthodontist");
        }

        @Test
        @DisplayName("Employee not found – throws ResourceNotFoundException")
        void updateEmployee_notFound_throwsException() {
            when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.updateEmployee(99L, employeeDto))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // DELETE
    // ══════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("deleteEmployee")
    class DeleteEmployee {

        @Test
        @DisplayName("Existing employee – delete is called once")
        void deleteEmployee_exists_deletesSuccessfully() {
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

            employeeService.deleteEmployee(1L);

            verify(employeeRepository).delete(employee);
        }

        @Test
        @DisplayName("Non-existent ID – throws ResourceNotFoundException before delete")
        void deleteEmployee_notFound_throwsException() {
            when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.deleteEmployee(99L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(employeeRepository, never()).delete(any());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // NESTED AVAILABILITY
    // ══════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("addAvailability")
    class AddAvailability {

        @Test
        @DisplayName("Valid inputs – availability is added via aggregate helper and saved")
        void addAvailability_validInputs_addsAndReturnsDto() {
            // Arrange
            AvailabilityDto availDto = new AvailabilityDto(null, 1L,
                    DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(17, 0), true);
            Availability availability = new Availability();
            availability.setDayOfWeek(DayOfWeek.MONDAY);

            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
            when(availabilityMapper.mapToAvailability(availDto)).thenReturn(availability);
            when(employeeRepository.save(employee)).thenReturn(employee);
            when(availabilityMapper.mapToAvailabilityDto(availability)).thenReturn(availDto);

            // Act
            AvailabilityDto result = employeeService.addAvailability(1L, availDto);

            // Assert – the availability was wired to the employee before saving
            assertThat(result).isNotNull();
            assertThat(availability.getEmployee()).isEqualTo(employee);
            verify(employeeRepository).save(employee);
        }

        @Test
        @DisplayName("Employee not found – throws ResourceNotFoundException")
        void addAvailability_employeeNotFound_throwsException() {
            when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.addAvailability(99L, new AvailabilityDto()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateAvailability")
    class UpdateAvailability {

        @Test
        @DisplayName("Correct owner – updates fields and returns updated DTO")
        void updateAvailability_correctOwner_updatesSuccessfully() {
            // Arrange
            Availability existing = new Availability();
            existing.setId(5L);
            existing.setEmployee(employee); // belongs to employee id=1
            existing.setDayOfWeek(DayOfWeek.MONDAY);
            existing.setStartTime(LocalTime.of(8, 0));
            existing.setEndTime(LocalTime.of(16, 0));
            existing.setAvailable(true);

            AvailabilityDto updateDto = new AvailabilityDto(5L, 1L,
                    DayOfWeek.TUESDAY, LocalTime.of(9, 0), LocalTime.of(17, 0), false);

            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
            when(availabilityRepository.findById(5L)).thenReturn(Optional.of(existing));
            when(availabilityRepository.save(existing)).thenReturn(existing);
            when(availabilityMapper.mapToAvailabilityDto(existing)).thenReturn(updateDto);

            // Act
            AvailabilityDto result = employeeService.updateAvailability(1L, 5L, updateDto);

            // Assert – day and times were overwritten
            assertThat(existing.getDayOfWeek()).isEqualTo(DayOfWeek.TUESDAY);
            assertThat(existing.isAvailable()).isFalse();
        }

        @Test
        @DisplayName("Wrong owner – throws IllegalArgumentException")
        void updateAvailability_wrongOwner_throwsIllegalArgumentException() {
            // Build a different employee who owns the availability
            Employee otherEmployee = new Employee();
            otherEmployee.setId(99L);

            Availability existing = new Availability();
            existing.setId(5L);
            existing.setEmployee(otherEmployee); // owned by employee 99, not employee 1

            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
            when(availabilityRepository.findById(5L)).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> employeeService.updateAvailability(1L, 5L, new AvailabilityDto()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("does not belong to employee 1");
        }
    }

    @Nested
    @DisplayName("deleteAvailability")
    class DeleteAvailability {

        @Test
        @DisplayName("Correct owner – removes via aggregate and saves")
        void deleteAvailability_correctOwner_removesSuccessfully() {
            Availability availability = new Availability();
            availability.setId(5L);
            availability.setEmployee(employee);
            employee.getAvailabilities().add(availability);

            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
            when(availabilityRepository.findById(5L)).thenReturn(Optional.of(availability));
            when(employeeRepository.save(employee)).thenReturn(employee);

            employeeService.deleteAvailability(1L, 5L);

            // The availability was removed from the employee's list
            assertThat(employee.getAvailabilities()).doesNotContain(availability);
            verify(employeeRepository).save(employee);
        }

        @Test
        @DisplayName("Availability not found – throws ResourceNotFoundException")
        void deleteAvailability_availabilityNotFound_throwsException() {
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
            when(availabilityRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.deleteAvailability(1L, 99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Availability not found with id: 99");
        }
    }
}