package com.summerproject2026.DentalWave.service.impl;

import com.summerproject2026.DentalWave.dto.TimeOffRequestDto;
import com.summerproject2026.DentalWave.entity.Employee;
import com.summerproject2026.DentalWave.entity.TimeOffRequest;
import com.summerproject2026.DentalWave.entity.User;
import com.summerproject2026.DentalWave.enums.RequestStatus;
import com.summerproject2026.DentalWave.exception.ResourceNotFoundException;
import com.summerproject2026.DentalWave.mapper.TimeOffRequestMapper;
import com.summerproject2026.DentalWave.repository.EmployeeRepository;
import com.summerproject2026.DentalWave.repository.TimeOffRequestRepository;
import com.summerproject2026.DentalWave.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pure unit tests for {@link TimeOffRequestServiceImpl}.
 *
 * <p>All collaborators are replaced with Mockito mocks — no Spring context,
 * no database. Tests are fast and cover every code path including all
 * exception branches and the shared {@code reviewRequest} helper.</p>
 */
@ExtendWith(MockitoExtension.class)
class TimeOffRequestServiceImplTest {

    // -------------------------------------------------------------------------
    // Mocks & subject under test
    // -------------------------------------------------------------------------

    @Mock
    private TimeOffRequestRepository timeOffRequestRepository;

    @Mock
    private TimeOffRequestMapper timeOffRequestMapper;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TimeOffRequestServiceImpl timeOffRequestService;

    // -------------------------------------------------------------------------
    // Shared test data
    // -------------------------------------------------------------------------

    private Employee employee;
    private User reviewer;
    private TimeOffRequest pendingEntity;
    private TimeOffRequestDto requestDto;
    private TimeOffRequestDto responseDto;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(10L);

        reviewer = new User();
        reviewer.setId(20L);

        pendingEntity = new TimeOffRequest();
        pendingEntity.setId(1L);
        pendingEntity.setEmployee(employee);
        pendingEntity.setStatus(RequestStatus.PENDING);
        pendingEntity.setSubmittedAt(LocalDateTime.now());

        requestDto = new TimeOffRequestDto();
        requestDto.setEmployeeId(10L);
        requestDto.setStartDate(LocalDate.now().plusDays(5));
        requestDto.setEndDate(LocalDate.now().plusDays(10));

        responseDto = new TimeOffRequestDto();
        responseDto.setId(1L);
        responseDto.setEmployeeId(10L);
        responseDto.setStatus(RequestStatus.PENDING);
    }

    // =========================================================================
    // createTimeOffRequest()
    // =========================================================================

    @Test
    @DisplayName("createTimeOffRequest() resolves employee, forces PENDING status and server timestamp, saves and returns DTO")
    void createTimeOffRequest_savesWithPendingStatusAndServerTimestamp() {
        when(employeeRepository.findById(10L)).thenReturn(Optional.of(employee));
        when(timeOffRequestMapper.mapToTimeOffRequest(requestDto)).thenReturn(pendingEntity);
        when(timeOffRequestRepository.save(pendingEntity)).thenReturn(pendingEntity);
        when(timeOffRequestMapper.mapToTimeOffRequestDto(pendingEntity)).thenReturn(responseDto);

        TimeOffRequestDto result = timeOffRequestService.createTimeOffRequest(requestDto);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(RequestStatus.PENDING);

        // Verify the entity had its server-side fields set before saving
        ArgumentCaptor<TimeOffRequest> captor = ArgumentCaptor.forClass(TimeOffRequest.class);
        verify(timeOffRequestRepository).save(captor.capture());
        TimeOffRequest captured = captor.getValue();

        assertThat(captured.getStatus()).isEqualTo(RequestStatus.PENDING);
        assertThat(captured.getSubmittedAt()).isNotNull();
        assertThat(captured.getEmployee()).isEqualTo(employee);
    }

    @Test
    @DisplayName("createTimeOffRequest() overrides any client-supplied status with PENDING")
    void createTimeOffRequest_overridesClientStatus_withPending() {
        // Simulate a client trying to create a request already marked APPROVED
        pendingEntity.setStatus(RequestStatus.APPROVED);

        when(employeeRepository.findById(10L)).thenReturn(Optional.of(employee));
        when(timeOffRequestMapper.mapToTimeOffRequest(requestDto)).thenReturn(pendingEntity);
        when(timeOffRequestRepository.save(any())).thenReturn(pendingEntity);
        when(timeOffRequestMapper.mapToTimeOffRequestDto(any())).thenReturn(responseDto);

        timeOffRequestService.createTimeOffRequest(requestDto);

        ArgumentCaptor<TimeOffRequest> captor = ArgumentCaptor.forClass(TimeOffRequest.class);
        verify(timeOffRequestRepository).save(captor.capture());

        // Must have been reset to PENDING by the service
        assertThat(captor.getValue().getStatus()).isEqualTo(RequestStatus.PENDING);
    }

    @Test
    @DisplayName("createTimeOffRequest() throws ResourceNotFoundException when employee does not exist")
    void createTimeOffRequest_throwsResourceNotFoundException_whenEmployeeNotFound() {
        when(employeeRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> timeOffRequestService.createTimeOffRequest(requestDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("10");

        verify(timeOffRequestRepository, never()).save(any());
    }

    // =========================================================================
    // getTimeOffRequestById()
    // =========================================================================

    @Test
    @DisplayName("getTimeOffRequestById() returns DTO when the request exists")
    void getTimeOffRequestById_returnsDto_whenExists() {
        when(timeOffRequestRepository.findById(1L)).thenReturn(Optional.of(pendingEntity));
        when(timeOffRequestMapper.mapToTimeOffRequestDto(pendingEntity)).thenReturn(responseDto);

        TimeOffRequestDto result = timeOffRequestService.getTimeOffRequestById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getTimeOffRequestById() throws ResourceNotFoundException when request does not exist")
    void getTimeOffRequestById_throwsResourceNotFoundException_whenNotFound() {
        when(timeOffRequestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> timeOffRequestService.getTimeOffRequestById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(timeOffRequestMapper, never()).mapToTimeOffRequestDto(any());
    }

    // =========================================================================
    // getAllRequests()
    // =========================================================================

    @Test
    @DisplayName("getAllRequests() returns a list of DTOs for all existing requests")
    void getAllRequests_returnsListOfDtos() {
        TimeOffRequest second = new TimeOffRequest();
        second.setId(2L);
        second.setStatus(RequestStatus.APPROVED);

        TimeOffRequestDto secondDto = new TimeOffRequestDto();
        secondDto.setId(2L);
        secondDto.setStatus(RequestStatus.APPROVED);

        when(timeOffRequestRepository.findAll()).thenReturn(List.of(pendingEntity, second));
        when(timeOffRequestMapper.mapToTimeOffRequestDto(pendingEntity)).thenReturn(responseDto);
        when(timeOffRequestMapper.mapToTimeOffRequestDto(second)).thenReturn(secondDto);

        List<TimeOffRequestDto> results = timeOffRequestService.getAllRequests();

        assertThat(results).hasSize(2);
        assertThat(results)
                .extracting(TimeOffRequestDto::getId)
                .containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    @DisplayName("getAllRequests() returns an empty list when no requests exist")
    void getAllRequests_returnsEmptyList_whenNoneExist() {
        when(timeOffRequestRepository.findAll()).thenReturn(List.of());

        List<TimeOffRequestDto> results = timeOffRequestService.getAllRequests();

        assertThat(results).isEmpty();
        verify(timeOffRequestMapper, never()).mapToTimeOffRequestDto(any());
    }

    // =========================================================================
    // getRequestsByEmployee()
    // =========================================================================

    @Test
    @DisplayName("getRequestsByEmployee() returns matching DTOs when employee exists")
    void getRequestsByEmployee_returnsDtos_whenEmployeeExists() {
        when(employeeRepository.existsById(10L)).thenReturn(true);
        when(timeOffRequestRepository.findByEmployeeId(10L)).thenReturn(List.of(pendingEntity));
        when(timeOffRequestMapper.mapToTimeOffRequestDto(pendingEntity)).thenReturn(responseDto);

        List<TimeOffRequestDto> results = timeOffRequestService.getRequestsByEmployee(10L);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getEmployeeId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("getRequestsByEmployee() returns empty list when employee has no requests")
    void getRequestsByEmployee_returnsEmptyList_whenNoRequestsFound() {
        when(employeeRepository.existsById(10L)).thenReturn(true);
        when(timeOffRequestRepository.findByEmployeeId(10L)).thenReturn(List.of());

        List<TimeOffRequestDto> results = timeOffRequestService.getRequestsByEmployee(10L);

        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("getRequestsByEmployee() throws ResourceNotFoundException when employee does not exist")
    void getRequestsByEmployee_throwsResourceNotFoundException_whenEmployeeNotFound() {
        when(employeeRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> timeOffRequestService.getRequestsByEmployee(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(timeOffRequestRepository, never()).findByEmployeeId(any());
    }

    // =========================================================================
    // getRequestsByStatus()
    // =========================================================================

    @Test
    @DisplayName("getRequestsByStatus() returns only requests matching the given status")
    void getRequestsByStatus_returnsMatchingDtos() {
        when(timeOffRequestRepository.findByStatus(RequestStatus.PENDING))
                .thenReturn(List.of(pendingEntity));
        when(timeOffRequestMapper.mapToTimeOffRequestDto(pendingEntity)).thenReturn(responseDto);

        List<TimeOffRequestDto> results = timeOffRequestService
                .getRequestsByStatus(RequestStatus.PENDING);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getStatus()).isEqualTo(RequestStatus.PENDING);
    }

    @Test
    @DisplayName("getRequestsByStatus() returns empty list when no requests match the status")
    void getRequestsByStatus_returnsEmptyList_whenNoMatch() {
        when(timeOffRequestRepository.findByStatus(RequestStatus.APPROVED))
                .thenReturn(List.of());

        List<TimeOffRequestDto> results = timeOffRequestService
                .getRequestsByStatus(RequestStatus.APPROVED);

        assertThat(results).isEmpty();
    }

    // =========================================================================
    // approveRequest()
    // =========================================================================

    @Test
    @DisplayName("approveRequest() sets status to APPROVED, assigns reviewer, sets reviewedAt, and saves")
    void approveRequest_setsApprovedStatusAndReviewerAndTimestamp() {
        TimeOffRequestDto approvedDto = new TimeOffRequestDto();
        approvedDto.setId(1L);
        approvedDto.setStatus(RequestStatus.APPROVED);

        when(timeOffRequestRepository.findById(1L)).thenReturn(Optional.of(pendingEntity));
        when(userRepository.findById(20L)).thenReturn(Optional.of(reviewer));
        when(timeOffRequestRepository.save(pendingEntity)).thenReturn(pendingEntity);
        when(timeOffRequestMapper.mapToTimeOffRequestDto(pendingEntity)).thenReturn(approvedDto);

        TimeOffRequestDto result = timeOffRequestService.approveRequest(1L, 20L, "Looks good");

        assertThat(result.getStatus()).isEqualTo(RequestStatus.APPROVED);

        // Verify all review fields were applied to the entity
        assertThat(pendingEntity.getStatus()).isEqualTo(RequestStatus.APPROVED);
        assertThat(pendingEntity.getReviewedBy()).isEqualTo(reviewer);
        assertThat(pendingEntity.getReviewedAt()).isNotNull();
        assertThat(pendingEntity.getReviewComment()).isEqualTo("Looks good");

        verify(timeOffRequestRepository, times(1)).save(pendingEntity);
    }

    @Test
    @DisplayName("approveRequest() works with a null reviewComment")
    void approveRequest_worksWithNullReviewComment() {
        when(timeOffRequestRepository.findById(1L)).thenReturn(Optional.of(pendingEntity));
        when(userRepository.findById(20L)).thenReturn(Optional.of(reviewer));
        when(timeOffRequestRepository.save(pendingEntity)).thenReturn(pendingEntity);
        when(timeOffRequestMapper.mapToTimeOffRequestDto(pendingEntity)).thenReturn(responseDto);

        timeOffRequestService.approveRequest(1L, 20L, null);

        assertThat(pendingEntity.getReviewComment()).isNull();
    }

    @Test
    @DisplayName("approveRequest() throws ResourceNotFoundException when the request does not exist")
    void approveRequest_throwsResourceNotFoundException_whenRequestNotFound() {
        when(timeOffRequestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> timeOffRequestService.approveRequest(99L, 20L, null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(userRepository, never()).findById(any());
        verify(timeOffRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("approveRequest() throws ResourceNotFoundException when the reviewer does not exist")
    void approveRequest_throwsResourceNotFoundException_whenReviewerNotFound() {
        when(timeOffRequestRepository.findById(1L)).thenReturn(Optional.of(pendingEntity));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> timeOffRequestService.approveRequest(1L, 99L, null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(timeOffRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("approveRequest() throws IllegalStateException when request is not PENDING")
    void approveRequest_throwsIllegalStateException_whenRequestNotPending() {
        pendingEntity.setStatus(RequestStatus.APPROVED); // already reviewed

        when(timeOffRequestRepository.findById(1L)).thenReturn(Optional.of(pendingEntity));

        assertThatThrownBy(() -> timeOffRequestService.approveRequest(1L, 20L, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PENDING")
                .hasMessageContaining("APPROVED");

        verify(userRepository, never()).findById(any());
        verify(timeOffRequestRepository, never()).save(any());
    }

    // =========================================================================
    // denyRequest()
    // =========================================================================

    @Test
    @DisplayName("denyRequest() sets status to DENIED, assigns reviewer, sets reviewedAt, and saves")
    void denyRequest_setsDeniedStatusAndReviewerAndTimestamp() {
        TimeOffRequestDto deniedDto = new TimeOffRequestDto();
        deniedDto.setId(1L);
        deniedDto.setStatus(RequestStatus.DENIED);

        when(timeOffRequestRepository.findById(1L)).thenReturn(Optional.of(pendingEntity));
        when(userRepository.findById(20L)).thenReturn(Optional.of(reviewer));
        when(timeOffRequestRepository.save(pendingEntity)).thenReturn(pendingEntity);
        when(timeOffRequestMapper.mapToTimeOffRequestDto(pendingEntity)).thenReturn(deniedDto);

        TimeOffRequestDto result = timeOffRequestService
                .denyRequest(1L, 20L, "Insufficient coverage");

        assertThat(result.getStatus()).isEqualTo(RequestStatus.DENIED);

        assertThat(pendingEntity.getStatus()).isEqualTo(RequestStatus.DENIED);
        assertThat(pendingEntity.getReviewedBy()).isEqualTo(reviewer);
        assertThat(pendingEntity.getReviewedAt()).isNotNull();
        assertThat(pendingEntity.getReviewComment()).isEqualTo("Insufficient coverage");
    }

    @Test
    @DisplayName("denyRequest() throws ResourceNotFoundException when the request does not exist")
    void denyRequest_throwsResourceNotFoundException_whenRequestNotFound() {
        when(timeOffRequestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> timeOffRequestService.denyRequest(99L, 20L, null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(userRepository, never()).findById(any());
        verify(timeOffRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("denyRequest() throws ResourceNotFoundException when the reviewer does not exist")
    void denyRequest_throwsResourceNotFoundException_whenReviewerNotFound() {
        when(timeOffRequestRepository.findById(1L)).thenReturn(Optional.of(pendingEntity));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> timeOffRequestService.denyRequest(1L, 99L, null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(timeOffRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("denyRequest() throws IllegalStateException when request is not PENDING")
    void denyRequest_throwsIllegalStateException_whenRequestNotPending() {
        pendingEntity.setStatus(RequestStatus.DENIED); // already reviewed

        when(timeOffRequestRepository.findById(1L)).thenReturn(Optional.of(pendingEntity));

        assertThatThrownBy(() -> timeOffRequestService.denyRequest(1L, 20L, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PENDING")
                .hasMessageContaining("DENIED");

        verify(userRepository, never()).findById(any());
        verify(timeOffRequestRepository, never()).save(any());
    }

    // =========================================================================
    // deleteRequest()
    // =========================================================================

    @Test
    @DisplayName("deleteRequest() fetches the request then deletes it")
    void deleteRequest_fetchesAndDeletes() {
        when(timeOffRequestRepository.findById(1L)).thenReturn(Optional.of(pendingEntity));

        timeOffRequestService.deleteRequest(1L);

        verify(timeOffRequestRepository, times(1)).findById(1L);
        verify(timeOffRequestRepository, times(1)).delete(pendingEntity);
    }

    @Test
    @DisplayName("deleteRequest() throws ResourceNotFoundException when request does not exist")
    void deleteRequest_throwsResourceNotFoundException_whenNotFound() {
        when(timeOffRequestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> timeOffRequestService.deleteRequest(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(timeOffRequestRepository, never()).delete(any());
    }
}