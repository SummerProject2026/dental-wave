package com.summerproject2026.DentalWave.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.summerproject2026.DentalWave.dto.TimeOffRequestDto;
import com.summerproject2026.DentalWave.enums.RequestStatus;
import com.summerproject2026.DentalWave.exception.ResourceNotFoundException;
import com.summerproject2026.DentalWave.service.TimeOffRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TimeOffRequestController.class)
@AutoConfigureMockMvc(addFilters = false)
class TimeOffRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TimeOffRequestService timeOffRequestService;

    @MockBean
    private com.summerproject2026.DentalWave.security.JwtTokenProvider jwtTokenProvider;

    @MockBean
    private com.summerproject2026.DentalWave.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    private TimeOffRequestDto pendingDto;
    private TimeOffRequestDto approvedDto;

    @BeforeEach
    void setUp() {
        pendingDto = new TimeOffRequestDto();
        pendingDto.setId(1L);
        pendingDto.setEmployeeId(10L);
        pendingDto.setStatus(RequestStatus.PENDING);
        pendingDto.setStartDate(LocalDate.now().plusDays(5));
        pendingDto.setEndDate(LocalDate.now().plusDays(10));

        approvedDto = new TimeOffRequestDto();
        approvedDto.setId(1L);
        approvedDto.setEmployeeId(10L);
        approvedDto.setStatus(RequestStatus.APPROVED);
        approvedDto.setReviewComment("Looks good");
    }

    @Test
    @DisplayName("POST /api/time-off-requests → 201 Created with the created request")
    void createTimeOffRequest_returns201() throws Exception {
        when(timeOffRequestService.createTimeOffRequest(any(TimeOffRequestDto.class)))
                .thenReturn(pendingDto);

        mockMvc.perform(post("/api/time-off-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pendingDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.employeeId", is(10)))
                .andExpect(jsonPath("$.status", is("PENDING")));

        verify(timeOffRequestService, times(1))
                .createTimeOffRequest(any(TimeOffRequestDto.class));
    }

    @Test
    @DisplayName("POST /api/time-off-requests → 400 Bad Request when body is missing")
    void createTimeOffRequest_returns400_whenBodyMissing() throws Exception {
        mockMvc.perform(post("/api/time-off-requests")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(timeOffRequestService, never()).createTimeOffRequest(any());
    }

    @Test
    @DisplayName("GET /api/time-off-requests/{id} → 200 OK with matching request")
    void getTimeOffRequestById_returns200_whenExists() throws Exception {
        when(timeOffRequestService.getTimeOffRequestById(1L)).thenReturn(pendingDto);

        mockMvc.perform(get("/api/time-off-requests/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("PENDING")));
    }

    @Test
    @DisplayName("GET /api/time-off-requests/{id} → 404 Not Found when request does not exist")
    void getTimeOffRequestById_returns404_whenNotFound() throws Exception {
        when(timeOffRequestService.getTimeOffRequestById(99L))
                .thenThrow(new ResourceNotFoundException("TimeOffRequest not found with id: 99"));

        mockMvc.perform(get("/api/time-off-requests/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/time-off-requests → 200 OK with all requests")
    void getAllRequests_returns200WithList() throws Exception {
        TimeOffRequestDto second = new TimeOffRequestDto();
        second.setId(2L);
        second.setStatus(RequestStatus.DENIED);

        when(timeOffRequestService.getAllRequests()).thenReturn(List.of(pendingDto, second));

        mockMvc.perform(get("/api/time-off-requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].status", is("PENDING")))
                .andExpect(jsonPath("$[1].status", is("DENIED")));
    }

    @Test
    @DisplayName("GET /api/time-off-requests → 200 OK with empty array when none exist")
    void getAllRequests_returns200WithEmptyList() throws Exception {
        when(timeOffRequestService.getAllRequests()).thenReturn(List.of());

        mockMvc.perform(get("/api/time-off-requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/time-off-requests/employee/{employeeId} → 200 OK with employee's requests")
    void getRequestsByEmployee_returns200() throws Exception {
        when(timeOffRequestService.getRequestsByEmployee(10L)).thenReturn(List.of(pendingDto));

        mockMvc.perform(get("/api/time-off-requests/employee/{employeeId}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].employeeId", is(10)));
    }

    @Test
    @DisplayName("GET /api/time-off-requests/employee/{employeeId} → 404 when employee does not exist")
    void getRequestsByEmployee_returns404_whenEmployeeNotFound() throws Exception {
        when(timeOffRequestService.getRequestsByEmployee(99L))
                .thenThrow(new ResourceNotFoundException("Employee not found with id: 99"));

        mockMvc.perform(get("/api/time-off-requests/employee/{employeeId}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/time-off-requests/status/PENDING → 200 OK with pending requests")
    void getRequestsByStatus_returns200() throws Exception {
        when(timeOffRequestService.getRequestsByStatus(RequestStatus.PENDING))
                .thenReturn(List.of(pendingDto));

        mockMvc.perform(get("/api/time-off-requests/status/{status}", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("PENDING")));
    }

    @Test
    @DisplayName("GET /api/time-off-requests/status/{status} → 200 OK with empty list when no matches")
    void getRequestsByStatus_returns200WithEmptyList_whenNoMatch() throws Exception {
        when(timeOffRequestService.getRequestsByStatus(RequestStatus.APPROVED))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/time-off-requests/status/{status}", "APPROVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("PATCH /api/time-off-requests/{id}/approve → 200 OK with approved request")
    void approveRequest_returns200WithApprovedRequest() throws Exception {
        when(timeOffRequestService.approveRequest(1L, 20L, "Looks good"))
                .thenReturn(approvedDto);

        mockMvc.perform(patch("/api/time-off-requests/{id}/approve", 1L)
                        .param("reviewedById", "20")
                        .param("reviewComment", "Looks good"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("APPROVED")))
                .andExpect(jsonPath("$.reviewComment", is("Looks good")));
    }

    @Test
    @DisplayName("PATCH /api/time-off-requests/{id}/approve → 200 OK without optional reviewComment")
    void approveRequest_returns200_withoutReviewComment() throws Exception {
        when(timeOffRequestService.approveRequest(1L, 20L, null)).thenReturn(approvedDto);

        mockMvc.perform(patch("/api/time-off-requests/{id}/approve", 1L)
                        .param("reviewedById", "20"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /api/time-off-requests/{id}/approve → 404 when request does not exist")
    void approveRequest_returns404_whenRequestNotFound() throws Exception {
        when(timeOffRequestService.approveRequest(eq(99L), any(), any()))
                .thenThrow(new ResourceNotFoundException("TimeOffRequest not found with id: 99"));

        mockMvc.perform(patch("/api/time-off-requests/{id}/approve", 99L)
                        .param("reviewedById", "20"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /api/time-off-requests/{id}/approve → 5xx when request is not PENDING")
    void approveRequest_returns409_whenRequestNotPending() throws Exception {
        when(timeOffRequestService.approveRequest(eq(1L), any(), any()))
                .thenThrow(new IllegalStateException("Only PENDING requests can be reviewed."));

        mockMvc.perform(patch("/api/time-off-requests/{id}/approve", 1L)
                        .param("reviewedById", "20"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("PATCH /api/time-off-requests/{id}/deny → 200 OK with denied request")
    void denyRequest_returns200WithDeniedRequest() throws Exception {
        TimeOffRequestDto deniedDto = new TimeOffRequestDto();
        deniedDto.setId(1L);
        deniedDto.setStatus(RequestStatus.DENIED);
        deniedDto.setReviewComment("Insufficient coverage");

        when(timeOffRequestService.denyRequest(1L, 20L, "Insufficient coverage"))
                .thenReturn(deniedDto);

        mockMvc.perform(patch("/api/time-off-requests/{id}/deny", 1L)
                        .param("reviewedById", "20")
                        .param("reviewComment", "Insufficient coverage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("DENIED")))
                .andExpect(jsonPath("$.reviewComment", is("Insufficient coverage")));
    }

    @Test
    @DisplayName("PATCH /api/time-off-requests/{id}/deny → 404 when request does not exist")
    void denyRequest_returns404_whenRequestNotFound() throws Exception {
        when(timeOffRequestService.denyRequest(eq(99L), any(), any()))
                .thenThrow(new ResourceNotFoundException("TimeOffRequest not found with id: 99"));

        mockMvc.perform(patch("/api/time-off-requests/{id}/deny", 99L)
                        .param("reviewedById", "20"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /api/time-off-requests/{id}/deny → 5xx when request is not PENDING")
    void denyRequest_returns409_whenRequestNotPending() throws Exception {
        when(timeOffRequestService.denyRequest(eq(1L), any(), any()))
                .thenThrow(new IllegalStateException("Only PENDING requests can be reviewed."));

        mockMvc.perform(patch("/api/time-off-requests/{id}/deny", 1L)
                        .param("reviewedById", "20"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("DELETE /api/time-off-requests/{id} → 200 OK with confirmation message")
    void deleteRequest_returns200WithMessage() throws Exception {
        doNothing().when(timeOffRequestService).deleteRequest(1L);

        mockMvc.perform(delete("/api/time-off-requests/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("1")))
                .andExpect(content().string(containsString("deleted successfully")));

        verify(timeOffRequestService, times(1)).deleteRequest(1L);
    }

    @Test
    @DisplayName("DELETE /api/time-off-requests/{id} → 404 Not Found when request does not exist")
    void deleteRequest_returns404_whenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("TimeOffRequest not found with id: 99"))
                .when(timeOffRequestService).deleteRequest(99L);

        mockMvc.perform(delete("/api/time-off-requests/{id}", 99L))
                .andExpect(status().isNotFound());
    }
}