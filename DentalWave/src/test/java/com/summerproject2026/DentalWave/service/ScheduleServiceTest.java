package com.dentalwave.service;

import com.dentalwave.dto.ScheduleDto;
import com.dentalwave.exception.ResourceNotFoundException;
import com.dentalwave.mapper.ScheduleMapper;
import com.dentalwave.model.Employee;
import com.dentalwave.model.Schedule;
import com.dentalwave.model.User;
import com.dentalwave.repository.EmployeeRepository;
import com.dentalwave.repository.ScheduleRepository;
import com.dentalwave.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ScheduleServiceImpl (pure service-layer, no Spring context, no DB).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ScheduleService")
class ScheduleServiceImplTest {

    // -------------------------------------------------------------------------
    // Mocks — inject whichever repositories / utilities your impl needs.
    // Remove any that your ScheduleServiceImpl doesn't actually inject.
    // -------------------------------------------------------------------------

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ScheduleMapper scheduleMapper;   // remove if you map manually in the service

    @InjectMocks
    private ScheduleServiceImpl scheduleService;

    // -------------------------------------------------------------------------
    // Shared fixtures
    // -------------------------------------------------------------------------

    private Schedule schedule;
    private ScheduleDto scheduleDto;
    private static final LocalDate TEST_DATE = LocalDate.of(2025, 6, 15);

    @BeforeEach
    void setUp() {
        schedule = new Schedule();
        schedule.setId(1L);
        schedule.setDate(TEST_DATE);
        schedule.setPublished(false);
        schedule.setTeams(new HashMap<>());

        scheduleDto = new ScheduleDto();
        scheduleDto.setId(1L);
        scheduleDto.setDate(TEST_DATE);
        scheduleDto.setPublished(false);
    }

    // =========================================================================
    // createSchedule
    // =========================================================================

    @Nested
    @DisplayName("createSchedule")
    class CreateSchedule {

        @Test
        @DisplayName("maps, saves, and returns the persisted ScheduleDto")
        void createSchedule_savesAndReturnsDto() {
            when(scheduleMapper.toEntity(scheduleDto)).thenReturn(schedule);
            when(scheduleRepository.save(schedule)).thenReturn(schedule);
            when(scheduleMapper.toDto(schedule)).thenReturn(scheduleDto);

            ScheduleDto result = scheduleService.createSchedule(scheduleDto);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            verify(scheduleRepository).save(schedule);
        }

        @Test
        @DisplayName("passes the correct entity to the repository")
        void createSchedule_passesCorrectEntityToRepo() {
            when(scheduleMapper.toEntity(any(ScheduleDto.class))).thenReturn(schedule);
            when(scheduleRepository.save(any(Schedule.class))).thenReturn(schedule);
            when(scheduleMapper.toDto(any(Schedule.class))).thenReturn(scheduleDto);

            scheduleService.createSchedule(scheduleDto);

            ArgumentCaptor<Schedule> captor = ArgumentCaptor.forClass(Schedule.class);
            verify(scheduleRepository).save(captor.capture());
            assertThat(captor.getValue().getDate()).isEqualTo(TEST_DATE);
        }
    }

    // =========================================================================
    // getScheduleById
    // =========================================================================

    @Nested
    @DisplayName("getScheduleById")
    class GetScheduleById {

        @Test
        @DisplayName("returns the ScheduleDto when the id exists")
        void getScheduleById_found_returnsDto() {
            when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
            when(scheduleMapper.toDto(schedule)).thenReturn(scheduleDto);

            ScheduleDto result = scheduleService.getScheduleById(1L);

            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when the id does not exist")
        void getScheduleById_notFound_throwsException() {
            when(scheduleRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> scheduleService.getScheduleById(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // =========================================================================
    // getAllSchedules
    // =========================================================================

    @Nested
    @DisplayName("getAllSchedules")
    class GetAllSchedules {

        @Test
        @DisplayName("returns all schedules as DTOs")
        void getAllSchedules_returnsAll() {
            Schedule s2 = new Schedule();
            s2.setId(2L);
            ScheduleDto dto2 = new ScheduleDto();
            dto2.setId(2L);

            when(scheduleRepository.findAll()).thenReturn(List.of(schedule, s2));
            when(scheduleMapper.toDto(schedule)).thenReturn(scheduleDto);
            when(scheduleMapper.toDto(s2)).thenReturn(dto2);

            List<ScheduleDto> results = scheduleService.getAllSchedules();

            assertThat(results).hasSize(2);
            assertThat(results).extracting(ScheduleDto::getId).containsExactly(1L, 2L);
        }

        @Test
        @DisplayName("returns an empty list when no schedules exist")
        void getAllSchedules_noData_returnsEmptyList() {
            when(scheduleRepository.findAll()).thenReturn(List.of());

            assertThat(scheduleService.getAllSchedules()).isEmpty();
        }
    }

    // =========================================================================
    // updateSchedule
    // =========================================================================

    @Nested
    @DisplayName("updateSchedule")
    class UpdateSchedule {

        @Test
        @DisplayName("updates existing schedule and returns updated DTO")
        void updateSchedule_success() {
            ScheduleDto updatedDto = new ScheduleDto();
            updatedDto.setId(1L);
            updatedDto.setDate(LocalDate.of(2025, 7, 1));

            Schedule updatedEntity = new Schedule();
            updatedEntity.setId(1L);
            updatedEntity.setDate(LocalDate.of(2025, 7, 1));

            when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
            when(scheduleRepository.save(any(Schedule.class))).thenReturn(updatedEntity);
            when(scheduleMapper.toDto(updatedEntity)).thenReturn(updatedDto);

            ScheduleDto result = scheduleService.updateSchedule(1L, updatedDto);

            assertThat(result.getDate()).isEqualTo(LocalDate.of(2025, 7, 1));
            verify(scheduleRepository).save(any(Schedule.class));
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when schedule not found")
        void updateSchedule_notFound_throwsException() {
            when(scheduleRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> scheduleService.updateSchedule(99L, scheduleDto))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // =========================================================================
    // deleteSchedule
    // =========================================================================

    @Nested
    @DisplayName("deleteSchedule")
    class DeleteSchedule {

        @Test
        @DisplayName("calls deleteById on the repository")
        void deleteSchedule_callsRepository() {
            when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
            doNothing().when(scheduleRepository).deleteById(1L);

            scheduleService.deleteSchedule(1L);

            verify(scheduleRepository).deleteById(1L);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when schedule not found")
        void deleteSchedule_notFound_throwsException() {
            when(scheduleRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> scheduleService.deleteSchedule(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // =========================================================================
    // getSchedulesByDate
    // =========================================================================

    @Nested
    @DisplayName("getSchedulesByDate")
    class GetSchedulesByDate {

        @Test
        @DisplayName("returns all schedules on the given date")
        void getSchedulesByDate_returnsMatching() {
            when(scheduleRepository.findByDate(TEST_DATE)).thenReturn(List.of(schedule));
            when(scheduleMapper.toDto(schedule)).thenReturn(scheduleDto);

            List<ScheduleDto> results = scheduleService.getSchedulesByDate(TEST_DATE);

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getDate()).isEqualTo(TEST_DATE);
        }

        @Test
        @DisplayName("returns empty list when no schedules exist on that date")
        void getSchedulesByDate_noMatch_returnsEmpty() {
            when(scheduleRepository.findByDate(LocalDate.of(2000, 1, 1))).thenReturn(List.of());

            assertThat(scheduleService.getSchedulesByDate(LocalDate.of(2000, 1, 1))).isEmpty();
        }
    }

    // =========================================================================
    // getSchedulesByCalendar
    // =========================================================================

    @Nested
    @DisplayName("getSchedulesByCalendar")
    class GetSchedulesByCalendar {

        @Test
        @DisplayName("returns all schedules for the given calendar id")
        void getSchedulesByCalendar_returnsMatching() {
            when(scheduleRepository.findByCalendarId(5L)).thenReturn(List.of(schedule));
            when(scheduleMapper.toDto(schedule)).thenReturn(scheduleDto);

            List<ScheduleDto> results = scheduleService.getSchedulesByCalendar(5L);

            assertThat(results).hasSize(1);
        }
    }

    // =========================================================================
    // assignEmployeeToTeam
    // =========================================================================

    @Nested
    @DisplayName("assignEmployeeToTeam")
    class AssignEmployeeToTeam {

        @Test
        @DisplayName("adds employee to an existing team lead's list")
        void assignEmployee_existingTeam_addsEmployee() {
            User teamLead = new User();
            teamLead.setId(2L);

            Employee employee = new Employee();
            employee.setId(3L);

            // Pre-seed team so the key already exists
            List<Employee> existingTeam = new ArrayList<>();
            schedule.getTeams().put(teamLead, existingTeam);

            when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
            when(userRepository.findById(2L)).thenReturn(Optional.of(teamLead));
            when(employeeRepository.findById(3L)).thenReturn(Optional.of(employee));
            when(scheduleRepository.save(any(Schedule.class))).thenReturn(schedule);
            when(scheduleMapper.toDto(schedule)).thenReturn(scheduleDto);

            scheduleService.assignEmployeeToTeam(1L, 2L, 3L);

            assertThat(existingTeam).contains(employee);
            verify(scheduleRepository).save(schedule);
        }

        @Test
        @DisplayName("creates a new team key when team lead has no existing team")
        void assignEmployee_newTeamKey_createsTeam() {
            User teamLead = new User();
            teamLead.setId(2L);

            Employee employee = new Employee();
            employee.setId(3L);

            // No pre-seeded team — map is empty
            when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
            when(userRepository.findById(2L)).thenReturn(Optional.of(teamLead));
            when(employeeRepository.findById(3L)).thenReturn(Optional.of(employee));
            when(scheduleRepository.save(any(Schedule.class))).thenReturn(schedule);
            when(scheduleMapper.toDto(schedule)).thenReturn(scheduleDto);

            scheduleService.assignEmployeeToTeam(1L, 2L, 3L);

            assertThat(schedule.getTeams()).containsKey(teamLead);
            assertThat(schedule.getTeams().get(teamLead)).contains(employee);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when schedule is missing")
        void assignEmployee_scheduleNotFound_throws() {
            when(scheduleRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> scheduleService.assignEmployeeToTeam(99L, 2L, 3L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when user (team lead) is missing")
        void assignEmployee_userNotFound_throws() {
            when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> scheduleService.assignEmployeeToTeam(1L, 99L, 3L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when employee is missing")
        void assignEmployee_employeeNotFound_throws() {
            User teamLead = new User();
            teamLead.setId(2L);

            when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
            when(userRepository.findById(2L)).thenReturn(Optional.of(teamLead));
            when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> scheduleService.assignEmployeeToTeam(1L, 2L, 99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // =========================================================================
    // removeEmployeeFromTeam
    // =========================================================================

    @Nested
    @DisplayName("removeEmployeeFromTeam")
    class RemoveEmployeeFromTeam {

        @Test
        @DisplayName("removes the employee from the team lead's list")
        void removeEmployee_success() {
            User teamLead = new User();
            teamLead.setId(2L);

            Employee employee = new Employee();
            employee.setId(3L);

            List<Employee> team = new ArrayList<>(List.of(employee));
            schedule.getTeams().put(teamLead, team);

            when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
            when(userRepository.findById(2L)).thenReturn(Optional.of(teamLead));
            when(employeeRepository.findById(3L)).thenReturn(Optional.of(employee));
            when(scheduleRepository.save(any(Schedule.class))).thenReturn(schedule);
            when(scheduleMapper.toDto(schedule)).thenReturn(scheduleDto);

            scheduleService.removeEmployeeFromTeam(1L, 2L, 3L);

            assertThat(team).doesNotContain(employee);
            verify(scheduleRepository).save(schedule);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when schedule is missing")
        void removeEmployee_scheduleNotFound_throws() {
            when(scheduleRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> scheduleService.removeEmployeeFromTeam(99L, 2L, 3L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // =========================================================================
    // publishSchedule
    // =========================================================================

    @Nested
    @DisplayName("publishSchedule")
    class PublishSchedule {

        @Test
        @DisplayName("sets published=true and persists the schedule")
        void publishSchedule_setsPublishedTrue() {
            schedule.setPublished(false);
            scheduleDto.setPublished(true);

            when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
            when(scheduleRepository.save(any(Schedule.class))).thenReturn(schedule);
            when(scheduleMapper.toDto(schedule)).thenReturn(scheduleDto);

            ScheduleDto result = scheduleService.publishSchedule(1L);

            ArgumentCaptor<Schedule> captor = ArgumentCaptor.forClass(Schedule.class);
            verify(scheduleRepository).save(captor.capture());
            assertThat(captor.getValue().isPublished()).isTrue();
            assertThat(result.isPublished()).isTrue();
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when schedule not found")
        void publishSchedule_notFound_throws() {
            when(scheduleRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> scheduleService.publishSchedule(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}