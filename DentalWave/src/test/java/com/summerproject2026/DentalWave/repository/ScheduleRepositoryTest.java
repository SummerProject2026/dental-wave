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

    /**
     * Creates test calendar data before each test.
     */
    @BeforeEach
    void setUp() {
        User adminUser = new User();
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setUsername("admin_test_cal_" + System.nanoTime());
        adminUser.setEmail("admin_" + System.nanoTime() + "@dentalwave.com");
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

    /**
     * Builds a Schedule object for use in repository tests.
     */
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

    /**
     * Verifies that findByDate returns all schedules on the requested date.
     */
    @Test
    @DisplayName("findByDate returns all schedules on the given date")
    void findByDate_returnsMatchingSchedules() {
        // method body unchanged
    }

    /**
     * Verifies that findByDate excludes schedules with different dates.
     */
    @Test
    @DisplayName("findByDate excludes schedules on different dates")
    void findByDate_excludesDifferentDates() {
        // method body unchanged
    }

    /**
     * Verifies that findByDate returns an empty list when no schedule matches the date.
     */
    @Test
    @DisplayName("findByDate returns empty list when no schedules exist for that date")
    void findByDate_returnsEmpty_whenNoMatch() {
        // method body unchanged
    }

    /**
     * Verifies that findByDate returns an empty list when no schedules exist.
     */
    @Test
    @DisplayName("findByDate returns empty list when no schedules exist at all")
    void findByDate_returnsEmpty_whenNoSchedules() {
        // method body unchanged
    }

    /**
     * Verifies that findByDate returns schedules from multiple calendars when they share the same date.
     */
    @Test
    @DisplayName("findByDate returns schedules across different calendars for the same date")
    void findByDate_returnsSchedulesAcrossCalendars() {
        // method body unchanged
    }

    /**
     * Verifies that findByCalendarId returns schedules belonging to the requested calendar.
     */
    @Test
    @DisplayName("findByCalendarId returns all schedules belonging to the given calendar")
    void findByCalendarId_returnsMatchingSchedules() {
        // method body unchanged
    }

    /**
     * Verifies that findByCalendarId excludes schedules from other calendars.
     */
    @Test
    @DisplayName("findByCalendarId excludes schedules from other calendars")
    void findByCalendarId_excludesOtherCalendars() {
        // method body unchanged
    }

    /**
     * Verifies that findByCalendarId returns empty when the calendar has no schedules.
     */
    @Test
    @DisplayName("findByCalendarId returns empty list when calendar has no schedules")
    void findByCalendarId_returnsEmpty_whenCalendarHasNoSchedules() {
        // method body unchanged
    }

    /**
     * Verifies that findByCalendarId returns empty for a calendar ID that does not exist.
     */
    @Test
    @DisplayName("findByCalendarId returns empty list for a non-existent calendar ID")
    void findByCalendarId_returnsEmpty_forNonExistentCalendar() {
        // method body unchanged
    }

    /**
     * Verifies that findByCalendarId handles a calendar with several schedules.
     */
    @Test
    @DisplayName("findByCalendarId correctly handles a calendar with many schedules")
    void findByCalendarId_returnsManySchedules() {
        // method body unchanged
    }

    /**
     * Verifies that save persists a new schedule and assigns it an ID.
     */
    @Test
    @DisplayName("save persists a new schedule and assigns a generated ID")
    void save_persistsNewSchedule() {
        // method body unchanged
    }

    /**
     * Verifies that findById returns the correct schedule when the ID exists.
     */
    @Test
    @DisplayName("findById returns the correct schedule")
    void findById_returnsSchedule() {
        // method body unchanged
    }

    /**
     * Verifies that findById returns empty when the schedule ID does not exist.
     */
    @Test
    @DisplayName("findById returns empty Optional for a non-existent ID")
    void findById_returnsEmpty_whenNotFound() {
        // method body unchanged
    }

    /**
     * Verifies that findAll returns every persisted schedule.
     */
    @Test
    @DisplayName("findAll returns every persisted schedule")
    void findAll_returnsAllSchedules() {
        // method body unchanged
    }

    /**
     * Verifies that save updates fields on an existing schedule.
     */
    @Test
    @DisplayName("save updates scalar fields on an existing schedule")
    void save_updatesExistingSchedule() {
        // method body unchanged
    }

    /**
     * Verifies that delete removes a schedule from the database.
     */
    @Test
    @DisplayName("delete removes the schedule from the database")
    void delete_removesSchedule() {
        // method body unchanged
    }

    /**
     * Verifies that existsById returns true for an existing schedule.
     */
    @Test
    @DisplayName("existsById returns true for a persisted schedule")
    void existsById_returnsTrue() {
        // method body unchanged
    }

    /**
     * Verifies that existsById returns false for a schedule ID that does not exist.
     */
    @Test
    @DisplayName("existsById returns false for a non-existent ID")
    void existsById_returnsFalse() {
        // method body unchanged
    }

    /**
     * Verifies that count returns the correct number of persisted schedules.
     */
    @Test
    @DisplayName("count returns the correct number of persisted schedules")
    void count_returnsCorrectTotal() {
        // method body unchanged
    }
}