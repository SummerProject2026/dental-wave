package com.summerproject2026.DentalWave.repository;

import com.summerproject2026.DentalWave.entity.Calendar;
import com.summerproject2026.DentalWave.entity.Schedule;
import com.summerproject2026.DentalWave.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for ScheduleRepository.
 *
 * The tests cover:
 *  - findByDate
 *  - findByCalendarId
 *  - Standard JpaRepository operations (save, findById, findAll, delete, count)
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ScheduleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ScheduleRepository scheduleRepository;

    private Calendar calendarJune;
    private Calendar calendarJuly;

    // -------------------------------------------------------------------------
    // Test fixtures
    // -------------------------------------------------------------------------

    @BeforeEach
    void setUp() {
        User adminUser = new User();
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setEmail("admin@dentalwave.com");
        entityManager.persist(adminUser);

        calendarJune = new Calendar();
        calendarJune.setMonth("June 2025");
        calendarJune.setStartCalendarDate(LocalDate.of(2025, 6, 1));
        calendarJune.setEndCalendarDate(LocalDate.of(2025, 6, 30));
        calendarJune.setPublished(true);
        calendarJune.setCreatedBy(adminUser);
        entityManager.persist(calendarJune);

        calendarJuly = new Calendar();
        calendarJuly.setMonth("July 2025");
        calendarJuly.setStartCalendarDate(LocalDate.of(2025, 7, 1));
        calendarJuly.setEndCalendarDate(LocalDate.of(2025, 7, 31));
        calendarJuly.setPublished(false);
        calendarJuly.setCreatedBy(adminUser);
        entityManager.persist(calendarJuly);

        entityManager.flush();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Schedule buildSchedule(LocalDate date, LocalTime start, LocalTime end,
                                   Calendar calendar, String notes) {
        Schedule s = new Schedule();
        s.setDate(date);
        s.setStartTime(start);
        s.setEndTime(end);
        s.setCalendar(calendar);
        s.setNotes(notes);
        return s;
    }

    // -------------------------------------------------------------------------
    // findByDate — happy paths
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findByDate returns all schedules on the given date")
    void findByDate_returnsMatchingSchedules() {
        LocalDate targetDate = LocalDate.of(2025, 6, 15);
        entityManager.persist(buildSchedule(
                targetDate,
                LocalTime.of(8, 0), LocalTime.of(12, 0),
                calendarJune, "Morning shift"));
        entityManager.persist(buildSchedule(
                targetDate,
                LocalTime.of(13, 0), LocalTime.of(17, 0),
                calendarJune, "Afternoon shift"));
        entityManager.persist(buildSchedule(
                LocalDate.of(2025, 6, 16),
                LocalTime.of(8, 0), LocalTime.of(16, 0),
                calendarJune, "Different day"));
        entityManager.flush();

        List<Schedule> result = scheduleRepository.findByDate(targetDate);

        assertThat(result).hasSize(2)
                .allMatch(s -> s.getDate().equals(targetDate));
    }

    @Test
    @DisplayName("findByDate excludes schedules on different dates")
    void findByDate_excludesDifferentDates() {
        entityManager.persist(buildSchedule(
                LocalDate.of(2025, 6, 10),
                LocalTime.of(8, 0), LocalTime.of(16, 0),
                calendarJune, "June 10th"));
        entityManager.persist(buildSchedule(
                LocalDate.of(2025, 6, 11),
                LocalTime.of(8, 0), LocalTime.of(16, 0),
                calendarJune, "June 11th"));
        entityManager.flush();

        List<Schedule> result = scheduleRepository.findByDate(LocalDate.of(2025, 6, 10));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNotes()).isEqualTo("June 10th");
    }

    @Test
    @DisplayName("findByDate returns empty list when no schedules exist for that date")
    void findByDate_returnsEmpty_whenNoMatch() {
        entityManager.persist(buildSchedule(
                LocalDate.of(2025, 6, 15),
                LocalTime.of(8, 0), LocalTime.of(16, 0),
                calendarJune, "Existing shift"));
        entityManager.flush();

        List<Schedule> result = scheduleRepository.findByDate(LocalDate.of(2025, 7, 1));

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByDate returns empty list when no schedules exist at all")
    void findByDate_returnsEmpty_whenNoSchedules() {
        List<Schedule> result = scheduleRepository.findByDate(LocalDate.of(2025, 6, 15));

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByDate returns schedules across different calendars for the same date")
    void findByDate_returnsSchedulesAcrossCalendars() {
        LocalDate sharedDate = LocalDate.of(2025, 7, 1);
        entityManager.persist(buildSchedule(
                sharedDate,
                LocalTime.of(8, 0), LocalTime.of(12, 0),
                calendarJune, "June calendar entry"));
        entityManager.persist(buildSchedule(
                sharedDate,
                LocalTime.of(13, 0), LocalTime.of(17, 0),
                calendarJuly, "July calendar entry"));
        entityManager.flush();

        List<Schedule> result = scheduleRepository.findByDate(sharedDate);

        assertThat(result).hasSize(2)
                .allMatch(s -> s.getDate().equals(sharedDate));
    }

    // -------------------------------------------------------------------------
    // findByCalendarId — happy paths
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findByCalendarId returns all schedules belonging to the given calendar")
    void findByCalendarId_returnsMatchingSchedules() {
        entityManager.persist(buildSchedule(
                LocalDate.of(2025, 6, 5),
                LocalTime.of(8, 0), LocalTime.of(16, 0),
                calendarJune, "June shift A"));
        entityManager.persist(buildSchedule(
                LocalDate.of(2025, 6, 12),
                LocalTime.of(8, 0), LocalTime.of(16, 0),
                calendarJune, "June shift B"));
        entityManager.persist(buildSchedule(
                LocalDate.of(2025, 7, 5),
                LocalTime.of(8, 0), LocalTime.of(16, 0),
                calendarJuly, "July shift"));
        entityManager.flush();

        List<Schedule> result = scheduleRepository.findByCalendarId(calendarJune.getId());

        assertThat(result).hasSize(2)
                .allMatch(s -> s.getCalendar().getId().equals(calendarJune.getId()));
    }

    @Test
    @DisplayName("findByCalendarId excludes schedules from other calendars")
    void findByCalendarId_excludesOtherCalendars() {
        entityManager.persist(buildSchedule(
                LocalDate.of(2025, 6, 5),
                LocalTime.of(8, 0), LocalTime.of(16, 0),
                calendarJune, "June shift"));
        entityManager.persist(buildSchedule(
                LocalDate.of(2025, 7, 5),
                LocalTime.of(8, 0), LocalTime.of(16, 0),
                calendarJuly, "July shift"));
        entityManager.flush();

        List<Schedule> result = scheduleRepository.findByCalendarId(calendarJuly.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNotes()).isEqualTo("July shift");
    }

    @Test
    @DisplayName("findByCalendarId returns empty list when calendar has no schedules")
    void findByCalendarId_returnsEmpty_whenCalendarHasNoSchedules() {
        List<Schedule> result = scheduleRepository.findByCalendarId(calendarJune.getId());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByCalendarId returns empty list for a non-existent calendar ID")
    void findByCalendarId_returnsEmpty_forNonExistentCalendar() {
        List<Schedule> result = scheduleRepository.findByCalendarId(9999L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByCalendarId correctly handles a calendar with many schedules")
    void findByCalendarId_returnsManySchedules() {
        for (int day = 1; day <= 5; day++) {
            entityManager.persist(buildSchedule(
                    LocalDate.of(2025, 6, day),
                    LocalTime.of(8, 0), LocalTime.of(16, 0),
                    calendarJune, "Shift day " + day));
        }
        entityManager.flush();

        List<Schedule> result = scheduleRepository.findByCalendarId(calendarJune.getId());

        assertThat(result).hasSize(5);
    }

    // -------------------------------------------------------------------------
    // Standard JpaRepository operations
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("save persists a new schedule and assigns a generated ID")
    void save_persistsNewSchedule() {
        Schedule schedule = buildSchedule(
                LocalDate.of(2025, 6, 20),
                LocalTime.of(8, 0), LocalTime.of(16, 0),
                calendarJune, "New shift");

        Schedule saved = scheduleRepository.save(schedule);

        assertThat(saved.getId()).isNotNull();
        assertThat(entityManager.find(Schedule.class, saved.getId())).isNotNull();
    }

    @Test
    @DisplayName("findById returns the correct schedule")
    void findById_returnsSchedule() {
        Schedule schedule = buildSchedule(
                LocalDate.of(2025, 6, 25),
                LocalTime.of(9, 0), LocalTime.of(17, 0),
                calendarJune, "Afternoon clinic");
        entityManager.persist(schedule);
        entityManager.flush();

        Optional<Schedule> found = scheduleRepository.findById(schedule.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getNotes()).isEqualTo("Afternoon clinic");
    }

    @Test
    @DisplayName("findById returns empty Optional for a non-existent ID")
    void findById_returnsEmpty_whenNotFound() {
        Optional<Schedule> found = scheduleRepository.findById(9999L);

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("findAll returns every persisted schedule")
    void findAll_returnsAllSchedules() {
        entityManager.persist(buildSchedule(
                LocalDate.of(2025, 6, 5),
                LocalTime.of(8, 0), LocalTime.of(16, 0),
                calendarJune, "Shift 1"));
        entityManager.persist(buildSchedule(
                LocalDate.of(2025, 7, 5),
                LocalTime.of(8, 0), LocalTime.of(16, 0),
                calendarJuly, "Shift 2"));
        entityManager.flush();

        List<Schedule> all = scheduleRepository.findAll();

        assertThat(all).hasSize(2);
    }

    @Test
    @DisplayName("save updates scalar fields on an existing schedule")
    void save_updatesExistingSchedule() {
        Schedule schedule = buildSchedule(
                LocalDate.of(2025, 6, 10),
                LocalTime.of(8, 0), LocalTime.of(16, 0),
                calendarJune, "Original notes");
        entityManager.persist(schedule);
        entityManager.flush();

        schedule.setNotes("Updated notes");
        schedule.setStartTime(LocalTime.of(9, 0));
        scheduleRepository.save(schedule);
        entityManager.flush();
        entityManager.clear();

        Schedule updated = entityManager.find(Schedule.class, schedule.getId());
        assertThat(updated.getNotes()).isEqualTo("Updated notes");
        assertThat(updated.getStartTime()).isEqualTo(LocalTime.of(9, 0));
    }

    @Test
    @DisplayName("delete removes the schedule from the database")
    void delete_removesSchedule() {
        Schedule schedule = buildSchedule(
                LocalDate.of(2025, 6, 15),
                LocalTime.of(8, 0), LocalTime.of(16, 0),
                calendarJune, "To be deleted");
        entityManager.persist(schedule);
        entityManager.flush();
        Long id = schedule.getId();

        scheduleRepository.delete(schedule);
        entityManager.flush();

        assertThat(entityManager.find(Schedule.class, id)).isNull();
    }

    @Test
    @DisplayName("existsById returns true for a persisted schedule")
    void existsById_returnsTrue() {
        Schedule schedule = buildSchedule(
                LocalDate.of(2025, 6, 18),
                LocalTime.of(8, 0), LocalTime.of(16, 0),
                calendarJune, "Exists");
        entityManager.persist(schedule);
        entityManager.flush();

        assertThat(scheduleRepository.existsById(schedule.getId())).isTrue();
    }

    @Test
    @DisplayName("existsById returns false for a non-existent ID")
    void existsById_returnsFalse() {
        assertThat(scheduleRepository.existsById(9999L)).isFalse();
    }

    @Test
    @DisplayName("count returns the correct number of persisted schedules")
    void count_returnsCorrectTotal() {
        entityManager.persist(buildSchedule(
                LocalDate.of(2025, 6, 1),
                LocalTime.of(8, 0), LocalTime.of(16, 0),
                calendarJune, "Shift A"));
        entityManager.persist(buildSchedule(
                LocalDate.of(2025, 6, 2),
                LocalTime.of(8, 0), LocalTime.of(16, 0),
                calendarJune, "Shift B"));
        entityManager.persist(buildSchedule(
                LocalDate.of(2025, 7, 1),
                LocalTime.of(8, 0), LocalTime.of(16, 0),
                calendarJuly, "Shift C"));
        entityManager.flush();

        assertThat(scheduleRepository.count()).isEqualTo(3);
    }
}