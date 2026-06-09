package com.summerproject2026.DentalWave.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.summerproject2026.DentalWave.dto.AvailabilityDto;
import com.summerproject2026.DentalWave.dto.EmployeeDto;
import com.summerproject2026.DentalWave.enums.WorkStatus;
import com.summerproject2026.DentalWave.entity.Employee;
import com.summerproject2026.DentalWave.repository.EmployeeRepository;
import com.summerproject2026.DentalWave.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
/**
 * Unit tests for EmployeeController.
 *
 * Both EmployeeService and EmployeeRepository are mocked because the
 * controller injects both (the search endpoint reaches the repository directly).
 *
 * If SecurityConfig is active in tests, add the appropriate exclusions or a
 * permissive test-security config so that MockMvc requests are not rejected.
 */
@WebMvcTest(EmployeeController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("EmployeeController")
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;


    // Controller injects EmployeeRepository directly for search — must be mocked.
    @MockBean
    private EmployeeRepository employeeRepository;

    @MockBean
    private com.summerproject2026.DentalWave.security.JwtTokenProvider jwtTokenProvider;

    @MockBean
    private com.summerproject2026.DentalWave.security.JwtAuthenticationFilter jwtAuthenticationFilter;
    @Autowired
    private ObjectMapper objectMapper;

    // -------------------------------------------------------------------------
    // Shared fixtures
    // -------------------------------------------------------------------------

    private EmployeeDto employeeDto;
    private AvailabilityDto availabilityDto;

    @BeforeEach
    void setUp() {
        employeeDto = new EmployeeDto();
        employeeDto.setId(1L);
        employeeDto.setFirstName("Jane");
        employeeDto.setLastName("Doe");
        employeeDto.setEmail("jane.doe@clinic.com");
        employeeDto.setPosition("Dental Hygienist");
        employeeDto.setStatus(WorkStatus.ACTIVE);

        availabilityDto = new AvailabilityDto();
        availabilityDto.setId(20L);
        availabilityDto.setEmployeeId(1L);
    }

    // =========================================================================
    // POST /api/employees
    // =========================================================================

    @Nested
    @DisplayName("POST /api/employees")
    class CreateEmployee {

        @Test
        @DisplayName("returns 201 and the persisted EmployeeDto")
        void createEmployee_returns201() throws Exception {
            when(employeeService.createEmployee(any(EmployeeDto.class))).thenReturn(employeeDto);

            mockMvc.perform(post("/api/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(employeeDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.firstName").value("Jane"))
                    .andExpect(jsonPath("$.lastName").value("Doe"));

            verify(employeeService, times(1)).createEmployee(any(EmployeeDto.class));
        }
    }

    // =========================================================================
    // GET /api/employees/{id}
    // =========================================================================

    @Nested
    @DisplayName("GET /api/employees/{id}")
    class GetEmployeeById {

        @Test
        @DisplayName("returns 200 and the EmployeeDto for a valid id")
        void getEmployeeById_returns200() throws Exception {
            when(employeeService.getEmployeeById(1L)).thenReturn(employeeDto);

            mockMvc.perform(get("/api/employees/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.email").value("jane.doe@clinic.com"));
        }

        @Test
        @DisplayName("propagates ResourceNotFoundException (results in 404)")
        void getEmployeeById_notFound() throws Exception {
            when(employeeService.getEmployeeById(99L))
                    .thenThrow(new com.summerproject2026.DentalWave.exception.ResourceNotFoundException("Employee not found"));

            mockMvc.perform(get("/api/employees/99"))
                    .andExpect(status().isNotFound());
        }
    }

    // =========================================================================
    // GET /api/employees
    // =========================================================================

    @Nested
    @DisplayName("GET /api/employees")
    class GetAllEmployees {

        @Test
        @DisplayName("returns 200 and the full employee list")
        void getAllEmployees_returns200() throws Exception {
            EmployeeDto second = new EmployeeDto();
            second.setId(2L);
            second.setFirstName("John");

            when(employeeService.getAllEmployees()).thenReturn(List.of(employeeDto, second));

            mockMvc.perform(get("/api/employees"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].firstName").value("Jane"))
                    .andExpect(jsonPath("$[1].firstName").value("John"));
        }

        @Test
        @DisplayName("returns 200 and empty list when no employees exist")
        void getAllEmployees_emptyList() throws Exception {
            when(employeeService.getAllEmployees()).thenReturn(List.of());

            mockMvc.perform(get("/api/employees"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    // =========================================================================
    // PUT /api/employees/{id}
    // =========================================================================

    @Nested
    @DisplayName("PUT /api/employees/{id}")
    class UpdateEmployee {

        @Test
        @DisplayName("returns 200 with the updated EmployeeDto")
        void updateEmployee_returns200() throws Exception {
            employeeDto.setPosition("Orthodontist");
            when(employeeService.updateEmployee(eq(1L), any(EmployeeDto.class))).thenReturn(employeeDto);

            mockMvc.perform(put("/api/employees/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(employeeDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.position").value("Orthodontist"));

            verify(employeeService).updateEmployee(eq(1L), any(EmployeeDto.class));
        }
    }

    // =========================================================================
    // DELETE /api/employees/{id}
    // =========================================================================

    @Nested
    @DisplayName("DELETE /api/employees/{id}")
    class DeleteEmployee {

        @Test
        @DisplayName("returns 200 with a confirmation message")
        void deleteEmployee_returns200() throws Exception {
            doNothing().when(employeeService).deleteEmployee(1L);

            mockMvc.perform(delete("/api/employees/1"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Employee with id 1 deleted successfully."));

            verify(employeeService).deleteEmployee(1L);
        }
    }

    // =========================================================================
    // GET /api/employees/office/{officeId}
    // =========================================================================

    @Nested
    @DisplayName("GET /api/employees/office/{officeId}")
    class GetEmployeesByOffice {

        @Test
        @DisplayName("returns 200 with employees for the given office")
        void getEmployeesByOffice_returns200() throws Exception {
            when(employeeService.getEmployeesByOffice(5L)).thenReturn(List.of(employeeDto));

            mockMvc.perform(get("/api/employees/office/5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1L));
        }

        @Test
        @DisplayName("returns 200 and empty list when no employees are in that office")
        void getEmployeesByOffice_noResults() throws Exception {
            when(employeeService.getEmployeesByOffice(99L)).thenReturn(List.of());

            mockMvc.perform(get("/api/employees/office/99"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    // =========================================================================
    // GET /api/employees/status/{status}
    // =========================================================================

    @Nested
    @DisplayName("GET /api/employees/status/{status}")
    class GetEmployeesByStatus {

        @Test
        @DisplayName("returns 200 with ACTIVE employees")
        void getByStatus_active_returns200() throws Exception {
            when(employeeService.getEmployeesByStatus(WorkStatus.ACTIVE))
                    .thenReturn(List.of(employeeDto));

            mockMvc.perform(get("/api/employees/status/ACTIVE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].status").value("ACTIVE"));
        }

        @Test
        @DisplayName("returns 200 with INACTIVE employees")
        void getByStatus_inactive_returns200() throws Exception {
            EmployeeDto inactive = new EmployeeDto();
            inactive.setId(3L);
            inactive.setStatus(WorkStatus.INACTIVE);

            when(employeeService.getEmployeesByStatus(WorkStatus.INACTIVE))
                    .thenReturn(List.of(inactive));

            mockMvc.perform(get("/api/employees/status/INACTIVE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].status").value("INACTIVE"));
        }
    }

    // =========================================================================
    // GET /api/employees/search?keyword=
    // =========================================================================

    @Nested
    @DisplayName("GET /api/employees/search")
    class SearchEmployees {

        @Test
        @DisplayName("returns 200 with employees matching the keyword")
        void searchEmployees_matchFound_returns200() throws Exception {
            // The controller calls employeeRepository.searchByKeyword then maps
            // each result through employeeService.getEmployeeById.
            Employee entity = new Employee();
            entity.setId(1L);

            when(employeeRepository.searchByKeyword("jane")).thenReturn(List.of(entity));
            when(employeeService.getEmployeeById(1L)).thenReturn(employeeDto);

            mockMvc.perform(get("/api/employees/search").param("keyword", "jane"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].firstName").value("Jane"));
        }

        @Test
        @DisplayName("returns 200 and empty list when no employees match")
        void searchEmployees_noMatch_emptyList() throws Exception {
            when(employeeRepository.searchByKeyword("xyz")).thenReturn(List.of());

            mockMvc.perform(get("/api/employees/search").param("keyword", "xyz"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("requires the keyword request parameter")
        void searchEmployees_missingKeyword_returns400() throws Exception {
            mockMvc.perform(get("/api/employees/search"))
                    .andExpect(status().isBadRequest());
        }
    }

    // =========================================================================
    // POST /api/employees/{employeeId}/availability
    // =========================================================================

    @Nested
    @DisplayName("POST /api/employees/{employeeId}/availability")
    class AddAvailability {

        @Test
        @DisplayName("returns 201 with the new AvailabilityDto")
        void addAvailability_returns201() throws Exception {
            when(employeeService.addAvailability(eq(1L), any(AvailabilityDto.class)))
                    .thenReturn(availabilityDto);

            mockMvc.perform(post("/api/employees/1/availability")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(availabilityDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(20L))
                    .andExpect(jsonPath("$.employeeId").value(1L));

            verify(employeeService).addAvailability(eq(1L), any(AvailabilityDto.class));
        }
    }

    // =========================================================================
    // PUT /api/employees/{employeeId}/availability/{availabilityId}
    // =========================================================================

    @Nested
    @DisplayName("PUT /api/employees/{employeeId}/availability/{availabilityId}")
    class UpdateAvailability {

        @Test
        @DisplayName("returns 200 with the updated AvailabilityDto")
        void updateAvailability_returns200() throws Exception {
            when(employeeService.updateAvailability(eq(1L), eq(20L), any(AvailabilityDto.class)))
                    .thenReturn(availabilityDto);

            mockMvc.perform(put("/api/employees/1/availability/20")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(availabilityDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(20L));

            verify(employeeService).updateAvailability(eq(1L), eq(20L), any(AvailabilityDto.class));
        }
    }

    // =========================================================================
    // DELETE /api/employees/{employeeId}/availability/{availabilityId}
    // =========================================================================

    @Nested
    @DisplayName("DELETE /api/employees/{employeeId}/availability/{availabilityId}")
    class DeleteAvailability {

        @Test
        @DisplayName("returns 200 with a confirmation message")
        void deleteAvailability_returns200() throws Exception {
            doNothing().when(employeeService).deleteAvailability(1L, 20L);

            mockMvc.perform(delete("/api/employees/1/availability/20"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Availability 20 removed from employee 1."));

            verify(employeeService).deleteAvailability(1L, 20L);
        }
    }
}