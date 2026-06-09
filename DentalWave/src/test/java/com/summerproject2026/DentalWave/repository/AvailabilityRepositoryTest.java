package com.summerproject2026.DentalWave.repository;

import com.summerproject2026.DentalWave.entity.Availability;
import com.summerproject2026.DentalWave.entity.Employee;
import com.summerproject2026.DentalWave.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for AvailabilityRepository.
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AvailabilityRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AvailabilityRepository availabilityRepository;


    private Employee employeeA;
    private Employee employeeB;

    // -------------------------------------------------------------------------
    // Test fixtures
    // -------------------------------------------------------------------------

    @BeforeEach
    void setUp() {
        // Create minimal User stubs required by Employee FK
        User userA = new User();
        userA.setFirstName("Alice");
        userA.setLastName("Smith");
        userA.setUsername("alice");
        userA.setEmail("alice@dentalwave.com");
        entityManager.persist(userA);

        User userB = new User();
        userB.setFirstName("Bob");
        userB.setLastName("Jones");
        userB.setUsername("bob");
        userB.setEmail("bob@dentalwave.com");
        entityManager.persist(userB);

        employeeA = new Employee();
        employeeA.setUser(userA);
        employeeA.setPosition("Dental Hygienist");
        employeeA.setHireDate(java.time.LocalDate.of(2020, 1, 1));
        entityManager.persist(employeeA);

        employeeB = new Employee();
        employeeB.setUser(userB);
        employeeB.setPosition("Receptionist");
        employeeB.setHireDate(java.time.LocalDate.of(2020, 1, 1));
        entityManager.persist(employeeB);

        entityManager.flush();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Availability buildAvailability(Employee employee, DayOfWeek day,
                                           LocalTime start, LocalTime end,
                                           boolean available) {
        Availability avail = new Availability();
        avail.setEmployee(employee);
        avail.setDayOfWeek(day);
        avail.setStartTime(start);
        avail.setEndTime(end);
        avail.setAvailable(available);
        return avail;
    }

    // -------------------------------------------------------------------------
    // findByEmployeeId — happy paths
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findByEmployeeId returns all records for the given employee")
    void findByEmployeeId_returnsAllRecordsForEmployee() {
        Availability mon = buildAvailability(
                employeeA, DayOfWeek.MONDAY,
                LocalTime.of(8, 0), LocalTime.of(16, 0), true);
        Availability tue = buildAvailability(
                employeeA, DayOfWeek.TUESDAY,
                LocalTime.of(9, 0), LocalTime.of(17, 0), true);
        entityManager.persist(mon);
        entityManager.persist(tue);
        entityManager.flush();

        List<Availability> result = availabilityRepository.findByEmployeeId(employeeA.getId());

        assertThat(result).hasSize(2)
                .extracting(Availability::getDayOfWeek)
                .containsExactlyInAnyOrder(DayOfWeek.MONDAY, DayOfWeek.TUESDAY);
    }

    @Test
    @DisplayName("findByEmployeeId does NOT return records belonging to another employee")
    void findByEmployeeId_excludesOtherEmployeeRecords() {
        entityManager.persist(buildAvailability(
                employeeA, DayOfWeek.WEDNESDAY,
                LocalTime.of(8, 0), LocalTime.of(12, 0), true));
        entityManager.persist(buildAvailability(
                employeeB, DayOfWeek.THURSDAY,
                LocalTime.of(13, 0), LocalTime.of(17, 0), true));
        entityManager.flush();

        List<Availability> result = availabilityRepository.findByEmployeeId(employeeA.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDayOfWeek()).isEqualTo(DayOfWeek.WEDNESDAY);
        assertThat(result.get(0).getEmployee().getId()).isEqualTo(employeeA.getId());
    }

    @Test
    @DisplayName("findByEmployeeId returns empty list when employee has no availability records")
    void findByEmployeeId_returnsEmptyList_whenNoRecords() {
        List<Availability> result = availabilityRepository.findByEmployeeId(employeeA.getId());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByEmployeeId returns empty list for non-existent employee ID")
    void findByEmployeeId_returnsEmptyList_forNonExistentEmployee() {
        List<Availability> result = availabilityRepository.findByEmployeeId(9999L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByEmployeeId handles an employee with a full 7-day availability set")
    void findByEmployeeId_returnsAllSevenDays() {
        DayOfWeek[] days = DayOfWeek.values();
        for (DayOfWeek day : days) {
            entityManager.persist(buildAvailability(
                    employeeA, day,
                    LocalTime.of(8, 0), LocalTime.of(16, 0), true));
        }
        entityManager.flush();

        List<Availability> result = availabilityRepository.findByEmployeeId(employeeA.getId());

        assertThat(result).hasSize(7);
    }

    // -------------------------------------------------------------------------
    // Standard JpaRepository operations
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("save persists a new availability record and assigns a generated ID")
    void save_persistsNewRecord() {
        Availability avail = buildAvailability(
                employeeA, DayOfWeek.FRIDAY,
                LocalTime.of(8, 0), LocalTime.of(12, 0), true);

        Availability saved = availabilityRepository.save(avail);

        assertThat(saved.getId()).isNotNull();
        assertThat(entityManager.find(Availability.class, saved.getId())).isNotNull();
    }

    @Test
    @DisplayName("findById returns the correct record")
    void findById_returnsRecord() {
        Availability avail = buildAvailability(
                employeeA, DayOfWeek.SATURDAY,
                LocalTime.of(10, 0), LocalTime.of(14, 0), false);
        entityManager.persist(avail);
        entityManager.flush();

        Optional<Availability> found = availabilityRepository.findById(avail.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getDayOfWeek()).isEqualTo(DayOfWeek.SATURDAY);
        assertThat(found.get().getAvailable()).isFalse();
    }

    @Test
    @DisplayName("findById returns empty Optional for non-existent ID")
    void findById_returnsEmpty_whenNotFound() {
        Optional<Availability> found = availabilityRepository.findById(9999L);

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("save updates an existing record's time window")
    void save_updatesExistingRecord() {
        Availability avail = buildAvailability(
                employeeA, DayOfWeek.MONDAY,
                LocalTime.of(8, 0), LocalTime.of(16, 0), true);
        entityManager.persist(avail);
        entityManager.flush();

        avail.setStartTime(LocalTime.of(9, 0));
        avail.setEndTime(LocalTime.of(17, 0));
        availabilityRepository.save(avail);
        entityManager.flush();
        entityManager.clear();

        Availability updated = entityManager.find(Availability.class, avail.getId());
        assertThat(updated.getStartTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(updated.getEndTime()).isEqualTo(LocalTime.of(17, 0));
    }

    @Test
    @DisplayName("delete removes the record from the database")
    void delete_removesRecord() {
        Availability avail = buildAvailability(
                employeeA, DayOfWeek.SUNDAY,
                LocalTime.of(8, 0), LocalTime.of(12, 0), true);
        entityManager.persist(avail);
        entityManager.flush();
        Long id = avail.getId();

        availabilityRepository.delete(avail);
        entityManager.flush();

        assertThat(entityManager.find(Availability.class, id)).isNull();
    }

    @Test
    @DisplayName("findAll returns every persisted availability record")
    void findAll_returnsAllRecords() {
        entityManager.persist(buildAvailability(
                employeeA, DayOfWeek.MONDAY,
                LocalTime.of(8, 0), LocalTime.of(16, 0), true));
        entityManager.persist(buildAvailability(
                employeeB, DayOfWeek.TUESDAY,
                LocalTime.of(9, 0), LocalTime.of(17, 0), true));
        entityManager.flush();

        List<Availability> all = availabilityRepository.findAll();

        assertThat(all).hasSize(2);
    }

    @Test
    @DisplayName("existsById returns true for a persisted record")
    void existsById_returnsTrue_whenExists() {
        Availability avail = buildAvailability(
                employeeA, DayOfWeek.WEDNESDAY,
                LocalTime.of(8, 0), LocalTime.of(16, 0), true);
        entityManager.persist(avail);
        entityManager.flush();

        assertThat(availabilityRepository.existsById(avail.getId())).isTrue();
    }

    @Test
    @DisplayName("existsById returns false for a non-existent ID")
    void existsById_returnsFalse_whenNotFound() {
        assertThat(availabilityRepository.existsById(9999L)).isFalse();
    }

    @Test
    @DisplayName("count reflects the number of persisted records")
    void count_returnsCorrectTotal() {
        entityManager.persist(buildAvailability(
                employeeA, DayOfWeek.MONDAY,
                LocalTime.of(8, 0), LocalTime.of(16, 0), true));
        entityManager.persist(buildAvailability(
                employeeA, DayOfWeek.TUESDAY,
                LocalTime.of(8, 0), LocalTime.of(16, 0), true));
        entityManager.flush();

        assertThat(availabilityRepository.count()).isEqualTo(2);
    }
}