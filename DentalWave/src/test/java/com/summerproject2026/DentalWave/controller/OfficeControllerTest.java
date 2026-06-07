package com.summerproject2026.DentalWave.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.summerproject2026.DentalWave.dto.OfficeDto;
import com.summerproject2026.DentalWave.exception.ResourceNotFoundException;
import com.summerproject2026.DentalWave.service.OfficeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web-layer (slice) tests for {@link OfficeController}.
 *
 * <p>{@code @WebMvcTest} loads only the controller layer: the servlet
 * infrastructure, Jackson, and Spring MVC configuration are wired up, but
 * no {@code @Service} or {@code @Repository} beans are created.
 * {@link OfficeService} is replaced by a Mockito mock via
 * {@code @MockBean} so the test is completely isolated from the
 * business and persistence layers.</p>
 *
 * <p>If your application uses Spring Security, you will need to either
 * disable it for these tests or configure test-specific security rules
 * (e.g. {@code @WithMockUser} or a custom {@code SecurityFilterChain}
 * bean scoped to tests). The simplest approach is to add
 * {@code @AutoConfigureMockMvc(addFilters = false)} to the class.</p>
 *
 * <p><strong>Required dependencies (usually transitively present via
 * {@code spring-boot-starter-test}):</strong>
 * MockMvc, Jackson, Mockito, Hamcrest.</p>
 */
@WebMvcTest(OfficeController.class)
// Uncomment the line below if Spring Security is on the classpath and
// causes 401/403 responses that break these tests:
// @AutoConfigureMockMvc(addFilters = false)
class OfficeControllerTest {

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OfficeService officeService;

    // -------------------------------------------------------------------------
    // Shared test data
    // -------------------------------------------------------------------------

    private OfficeDto officeDto;
    private OfficeDto officeDto2;

    @BeforeEach
    void setUp() {
        officeDto = new OfficeDto();
        officeDto.setId(1L);
        officeDto.setName("Downtown Dental");
        officeDto.setAddress("123 Main St, Charlotte, NC 28201");
        officeDto.setPhoneNumber("704-555-0101");

        officeDto2 = new OfficeDto();
        officeDto2.setId(2L);
        officeDto2.setName("Uptown Smiles");
        officeDto2.setAddress("456 Trade St, Charlotte, NC 28202");
        officeDto2.setPhoneNumber("704-555-0202");
    }

    // =========================================================================
    // POST /api/offices
    // =========================================================================

    @Test
    @DisplayName("POST /api/offices → 201 Created with the created office in the body")
    void createOffice_returns201WithCreatedOffice() throws Exception {
        when(officeService.createOffice(any(OfficeDto.class))).thenReturn(officeDto);

        mockMvc.perform(post("/api/offices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(officeDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Downtown Dental")))
                .andExpect(jsonPath("$.address", is("123 Main St, Charlotte, NC 28201")))
                .andExpect(jsonPath("$.phoneNumber", is("704-555-0101")));

        verify(officeService, times(1)).createOffice(any(OfficeDto.class));
    }

    @Test
    @DisplayName("POST /api/offices → 400 Bad Request when body is missing")
    void createOffice_returns400_whenBodyMissing() throws Exception {
        mockMvc.perform(post("/api/offices")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(officeService, never()).createOffice(any());
    }

    // =========================================================================
    // GET /api/offices/{id}
    // =========================================================================

    @Test
    @DisplayName("GET /api/offices/{id} → 200 OK with the matching office")
    void getOfficeById_returns200WithOffice_whenExists() throws Exception {
        when(officeService.getOfficeById(1L)).thenReturn(officeDto);

        mockMvc.perform(get("/api/offices/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Downtown Dental")));
    }

    @Test
    @DisplayName("GET /api/offices/{id} → 404 Not Found when office does not exist")
    void getOfficeById_returns404_whenNotExists() throws Exception {
        when(officeService.getOfficeById(99L))
                .thenThrow(new ResourceNotFoundException("Office not found with id: 99"));

        mockMvc.perform(get("/api/offices/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    // =========================================================================
    // GET /api/offices
    // =========================================================================

    @Test
    @DisplayName("GET /api/offices → 200 OK with a list of all offices")
    void getAllOffices_returns200WithAllOffices() throws Exception {
        when(officeService.getAllOffices()).thenReturn(List.of(officeDto, officeDto2));

        mockMvc.perform(get("/api/offices"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Downtown Dental")))
                .andExpect(jsonPath("$[1].name", is("Uptown Smiles")));
    }

    @Test
    @DisplayName("GET /api/offices → 200 OK with an empty array when no offices exist")
    void getAllOffices_returns200WithEmptyList_whenNoneExist() throws Exception {
        when(officeService.getAllOffices()).thenReturn(List.of());

        mockMvc.perform(get("/api/offices"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // =========================================================================
    // PUT /api/offices/{id}
    // =========================================================================

    @Test
    @DisplayName("PUT /api/offices/{id} → 200 OK with the updated office")
    void updateOffice_returns200WithUpdatedOffice_whenExists() throws Exception {
        OfficeDto updateRequest = new OfficeDto();
        updateRequest.setName("Downtown Dental – Relocated");
        updateRequest.setAddress("789 New St, Charlotte, NC 28203");
        updateRequest.setPhoneNumber("704-555-0303");

        OfficeDto updatedResponse = new OfficeDto();
        updatedResponse.setId(1L);
        updatedResponse.setName("Downtown Dental – Relocated");
        updatedResponse.setAddress("789 New St, Charlotte, NC 28203");
        updatedResponse.setPhoneNumber("704-555-0303");

        when(officeService.updateOffice(eq(1L), any(OfficeDto.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/api/offices/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Downtown Dental – Relocated")))
                .andExpect(jsonPath("$.address", is("789 New St, Charlotte, NC 28203")))
                .andExpect(jsonPath("$.phoneNumber", is("704-555-0303")));
    }

    @Test
    @DisplayName("PUT /api/offices/{id} → 404 Not Found when office does not exist")
    void updateOffice_returns404_whenNotExists() throws Exception {
        when(officeService.updateOffice(eq(99L), any(OfficeDto.class)))
                .thenThrow(new ResourceNotFoundException("Office not found with id: 99"));

        mockMvc.perform(put("/api/offices/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(officeDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/offices/{id} → 400 Bad Request when body is missing")
    void updateOffice_returns400_whenBodyMissing() throws Exception {
        mockMvc.perform(put("/api/offices/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(officeService, never()).updateOffice(any(), any());
    }

    // =========================================================================
    // DELETE /api/offices/{id}
    // =========================================================================

    @Test
    @DisplayName("DELETE /api/offices/{id} → 200 OK with confirmation message")
    void deleteOffice_returns200WithMessage_whenExists() throws Exception {
        doNothing().when(officeService).deleteOffice(1L);

        mockMvc.perform(delete("/api/offices/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("1")))
                .andExpect(content().string(containsString("deleted successfully")));

        verify(officeService, times(1)).deleteOffice(1L);
    }

    @Test
    @DisplayName("DELETE /api/offices/{id} → 404 Not Found when office does not exist")
    void deleteOffice_returns404_whenNotExists() throws Exception {
        doThrow(new ResourceNotFoundException("Office not found with id: 99"))
                .when(officeService).deleteOffice(99L);

        mockMvc.perform(delete("/api/offices/{id}", 99L))
                .andExpect(status().isNotFound());
    }
}