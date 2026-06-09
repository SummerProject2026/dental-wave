package com.summerproject2026.DentalWave.repository;

import com.summerproject2026.DentalWave.entity.Calendar;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for CalendarRepository.
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CalendarRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CalendarRepository calendarRepository;

    private User adminUser;

    // -------------------------------------------------------------------------
    // Test fixtures
    // -------------------------------------------------------------------------

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setEmail("admin@dentalwave.com");
        entityManager.persist(adminUser);
        entityManager.flush();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Calendar buildCalendar(String month, LocalDate start, LocalDate end, boolean published) {
        Calendar calendar = new Calendar();
        calendar.setMonth(month);
        calendar.setStartCalendarDate(start);
        calendar.setEndCalendarDate(end);
        calendar.setPublished(published);
        calendar.setCreatedBy(adminUser);
        return calendar;
    }

    // -------------------------------------------------------------------------
    // findByMonth — happy paths
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findByMonth returns all calendars for the given month label")
    void findByMonth_returnsMatchingCalendars() {
        entityManager.persist(buildCalendar(
                "June 2025",
                LocalDate.of(2025, 6, 1),
                LocalDate.of(2025, 6, 30),
                false));
        entityManager.persist(buildCalendar(
                "June 2025",
                LocalDate.of(2025, 6, 1),
                LocalDate.of(2025, 6, 15),
                true));
        entityManager.flush();

        List<Calendar> result = calendarRepository.findByMonth("June 2025");

        assertThat(result).hasSize(2)
                .allMatch(c -> "June 2025".equals(c.getMonth()));
    }

    @Test
    @DisplayName("findByMonth excludes calendars with a different month label")
    void findByMonth_excludesNonMatchingMonth() {
        entityManager.persist(buildCalendar(
                "June 2025",
                LocalDate.of(2025, 6, 1),
                LocalDate.of(2025, 6, 30),
                false));
        entityManager.persist(buildCalendar(
                "July 2025",
                LocalDate.of(2025, 7, 1),
                LocalDate.of(2025, 7, 31),
                false));
        entityManager.flush();

        List<Calendar> result = calendarRepository.findByMonth("June 2025");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMonth()).isEqualTo("June 2025");
    }

    @Test
    @DisplayName("findByMonth returns empty list when no calendars match the month")
    void findByMonth_returnsEmptyList_whenNoMatch() {
        entityManager.persist(buildCalendar(
                "July 2025",
                LocalDate.of(2025, 7, 1),
                LocalDate.of(2025, 7, 31),
                false));
        entityManager.flush();

        List<Calendar> result = calendarRepository.findByMonth("June 2025");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByMonth returns empty list when no calendars exist at all")
    void findByMonth_returnsEmptyList_whenNoCalendars() {
        List<Calendar> result = calendarRepository.findByMonth("June 2025");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByMonth is case-sensitive — 'june 2025' does not match 'June 2025'")
    void findByMonth_isCaseSensitive() {
        entityManager.persist(buildCalendar(
                "June 2025",
                LocalDate.of(2025, 6, 1),
                LocalDate.of(2025, 6, 30),
                false));
        entityManager.flush();

        List<Calendar> result = calendarRepository.findByMonth("june 2025");

        // Standard JPQL equality is case-sensitive by default on most DBs
        assertThat(result).isEmpty();
    }

    // -------------------------------------------------------------------------
    // findByPublishedTrue — happy paths
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findByPublishedTrue returns only published calendars")
    void findByPublishedTrue_returnsOnlyPublished() {
        entityManager.persist(buildCalendar(
                "June 2025",
                LocalDate.of(2025, 6, 1),
                LocalDate.of(2025, 6, 30),
                true));
        entityManager.persist(buildCalendar(
                "July 2025",
                LocalDate.of(2025, 7, 1),
                LocalDate.of(2025, 7, 31),
                false));
        entityManager.flush();

        List<Calendar> result = calendarRepository.findByPublishedTrue();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPublished()).isTrue();
        assertThat(result.get(0).getMonth()).isEqualTo("June 2025");
    }

    @Test
    @DisplayName("findByPublishedTrue returns all calendars when all are published")
    void findByPublishedTrue_returnsAll_whenAllPublished() {
        entityManager.persist(buildCalendar(
                "June 2025",
                LocalDate.of(2025, 6, 1),
                LocalDate.of(2025, 6, 30),
                true));
        entityManager.persist(buildCalendar(
                "July 2025",
                LocalDate.of(2025, 7, 1),
                LocalDate.of(2025, 7, 31),
                true));
        entityManager.flush();

        List<Calendar> result = calendarRepository.findByPublishedTrue();

        assertThat(result).hasSize(2)
                .allMatch(c -> Boolean.TRUE.equals(c.getPublished()));
    }

    @Test
    @DisplayName("findByPublishedTrue returns empty list when no calendars are published")
    void findByPublishedTrue_returnsEmpty_whenNonePublished() {
        entityManager.persist(buildCalendar(
                "June 2025",
                LocalDate.of(2025, 6, 1),
                LocalDate.of(2025, 6, 30),
                false));
        entityManager.persist(buildCalendar(
                "July 2025",
                LocalDate.of(2025, 7, 1),
                LocalDate.of(2025, 7, 31),
                false));
        entityManager.flush();

        List<Calendar> result = calendarRepository.findByPublishedTrue();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByPublishedTrue returns empty list when no calendars exist")
    void findByPublishedTrue_returnsEmpty_whenNoCalendars() {
        List<Calendar> result = calendarRepository.findByPublishedTrue();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByPublishedTrue excludes a calendar that was unpublished after creation")
    void findByPublishedTrue_excludesUnpublishedAfterUpdate() {
        Calendar calendar = buildCalendar(
                "August 2025",
                LocalDate.of(2025, 8, 1),
                LocalDate.of(2025, 8, 31),
                true);
        entityManager.persist(calendar);
        entityManager.flush();

        // Unpublish
        calendar.setPublished(false);
        calendarRepository.save(calendar);
        entityManager.flush();
        entityManager.clear();

        List<Calendar> result = calendarRepository.findByPublishedTrue();

        assertThat(result).isEmpty();
    }

    // -------------------------------------------------------------------------
    // Standard JpaRepository operations
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("save persists a new calendar and assigns a generated ID")
    void save_persistsNewCalendar() {
        Calendar calendar = buildCalendar(
                "September 2025",
                LocalDate.of(2025, 9, 1),
                LocalDate.of(2025, 9, 30),
                false);

        Calendar saved = calendarRepository.save(calendar);

        assertThat(saved.getId()).isNotNull();
        assertThat(entityManager.find(Calendar.class, saved.getId())).isNotNull();
    }

    @Test
    @DisplayName("findById returns the correct calendar")
    void findById_returnsCalendar() {
        Calendar calendar = buildCalendar(
                "October 2025",
                LocalDate.of(2025, 10, 1),
                LocalDate.of(2025, 10, 31),
                false);
        entityManager.persist(calendar);
        entityManager.flush();

        Optional<Calendar> found = calendarRepository.findById(calendar.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getMonth()).isEqualTo("October 2025");
    }

    @Test
    @DisplayName("findById returns empty Optional for a non-existent ID")
    void findById_returnsEmpty_whenNotFound() {
        Optional<Calendar> found = calendarRepository.findById(9999L);

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("findAll returns every persisted calendar")
    void findAll_returnsAllCalendars() {
        entityManager.persist(buildCalendar(
                "June 2025",
                LocalDate.of(2025, 6, 1),
                LocalDate.of(2025, 6, 30),
                false));
        entityManager.persist(buildCalendar(
                "July 2025",
                LocalDate.of(2025, 7, 1),
                LocalDate.of(2025, 7, 31),
                true));
        entityManager.flush();

        List<Calendar> all = calendarRepository.findAll();

        assertThat(all).hasSize(2);
    }

    @Test
    @DisplayName("delete removes the calendar from the database")
    void delete_removesCalendar() {
        Calendar calendar = buildCalendar(
                "November 2025",
                LocalDate.of(2025, 11, 1),
                LocalDate.of(2025, 11, 30),
                false);
        entityManager.persist(calendar);
        entityManager.flush();
        Long id = calendar.getId();

        calendarRepository.delete(calendar);
        entityManager.flush();

        assertThat(entityManager.find(Calendar.class, id)).isNull();
    }

    @Test
    @DisplayName("save updates scalar fields of an existing calendar")
    void save_updatesExistingCalendar() {
        Calendar calendar = buildCalendar(
                "December 2025",
                LocalDate.of(2025, 12, 1),
                LocalDate.of(2025, 12, 31),
                false);
        entityManager.persist(calendar);
        entityManager.flush();

        calendar.setPublished(true);
        calendarRepository.save(calendar);
        entityManager.flush();
        entityManager.clear();

        Calendar updated = entityManager.find(Calendar.class, calendar.getId());
        assertThat(updated.getPublished()).isTrue();
    }

    @Test
    @DisplayName("existsById returns true for a persisted calendar")
    void existsById_returnsTrue() {
        Calendar calendar = buildCalendar(
                "January 2026",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31),
                false);
        entityManager.persist(calendar);
        entityManager.flush();

        assertThat(calendarRepository.existsById(calendar.getId())).isTrue();
    }

    @Test
    @DisplayName("existsById returns false for a non-existent ID")
    void existsById_returnsFalse() {
        assertThat(calendarRepository.existsById(9999L)).isFalse();
    }

    @Test
    @DisplayName("count reflects the exact number of persisted calendars")
    void count_returnsCorrectTotal() {
        entityManager.persist(buildCalendar(
                "February 2026",
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 28),
                false));
        entityManager.persist(buildCalendar(
                "March 2026",
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 31),
                true));
        entityManager.flush();

        assertThat(calendarRepository.count()).isEqualTo(2);
    }
}