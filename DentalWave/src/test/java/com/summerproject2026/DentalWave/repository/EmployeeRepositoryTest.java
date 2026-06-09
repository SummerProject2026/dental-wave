package com.summerproject2026.DentalWave.repository;

import com.summerproject2026.DentalWave.entity.Employee;
import com.summerproject2026.DentalWave.entity.Office;
import com.summerproject2026.DentalWave.entity.User;
import com.summerproject2026.DentalWave.enums.WorkStatus;
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
 * Integration tests for EmployeeRepository.
 *
 * The tests cover:
 *  - findByUserId
 *  - findByStatus
 *  - findByPosition
 *  - findByOfficeId  (JPQL JOIN query)
 *  - searchByKeyword (JPQL LIKE query across multiple fields)
 *  - Standard JpaRepository operations (save, findById, findAll, delete, count)
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EmployeeRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EmployeeRepository employeeRepository;

    // Shared fixtures
    private User userAlice;
    private User userBob;
    private User userCarla;
    private Office officeA;
    private Office officeB;
    private Employee employeeAlice;
    private Employee employeeBob;
    private Employee employeeCarla;

    // -------------------------------------------------------------------------
    // Test fixtures
    // -------------------------------------------------------------------------

    @BeforeEach
    void setUp() {
        // Users
        userAlice = buildUser("Alice", "Smith", "alice@dentalwave.com");
        userBob   = buildUser("Bob",   "Jones", "bob@dentalwave.com");
        userCarla = buildUser("Carla", "White", "carla@clinic.com");
        entityManager.persist(userAlice);
        entityManager.persist(userBob);
        entityManager.persist(userCarla);

        // Offices
        officeA = new Office();
        officeA.setName("Raleigh");
        officeB = new Office();
        officeB.setName("Garner");
        entityManager.persist(officeA);
        entityManager.persist(officeB);

        // Employees
        employeeAlice = buildEmployee(userAlice, "Assistant", WorkStatus.ACTIVE,
                LocalDate.of(2020, 1, 15));
        employeeAlice.getOffices().add(officeA);

        employeeBob = buildEmployee(userBob, "Assistant", WorkStatus.INACTIVE,
                LocalDate.of(2019, 6, 1));
        employeeBob.getOffices().add(officeB);

        employeeCarla = buildEmployee(userCarla, "Assistant", WorkStatus.ACTIVE,
                LocalDate.of(2021, 3, 10));
        employeeCarla.getOffices().add(officeA);
        employeeCarla.getOffices().add(officeB);

        entityManager.persist(employeeAlice);
        entityManager.persist(employeeBob);
        entityManager.persist(employeeCarla);
        entityManager.flush();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private User buildUser(String firstName, String lastName, String email) {
        User u = new User();
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setEmail(email);
        return u;
    }

    private Employee buildEmployee(User user, String position,
                                   WorkStatus status, LocalDate hireDate) {
        Employee e = new Employee();
        e.setUser(user);
        e.setPosition(position);
        e.setStatus(status);
        e.setHireDate(hireDate);
        return e;
    }

    // -------------------------------------------------------------------------
    // findByUserId
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findByUserId returns the employee linked to the given user")
    void findByUserId_returnsEmployee() {
        Optional<Employee> result = employeeRepository.findByUserId(userAlice.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(employeeAlice.getId());
    }

    @Test
    @DisplayName("findByUserId returns empty Optional when no employee is linked")
    void findByUserId_returnsEmpty_whenNoEmployee() {
        User unlinkedUser = buildUser("Dave", "Black", "dave@clinic.com");
        entityManager.persist(unlinkedUser);
        entityManager.flush();

        Optional<Employee> result = employeeRepository.findByUserId(unlinkedUser.getId());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByUserId returns empty Optional for a non-existent user ID")
    void findByUserId_returnsEmpty_forNonExistentUser() {
        Optional<Employee> result = employeeRepository.findByUserId(9999L);

        assertThat(result).isEmpty();
    }

    // -------------------------------------------------------------------------
    // findByStatus
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findByStatus returns only employees with the given WorkStatus")
    void findByStatus_returnsMatchingEmployees() {
        List<Employee> actives = employeeRepository.findByStatus(WorkStatus.ACTIVE);

        assertThat(actives).hasSize(2)
                .extracting(emp -> emp.getUser().getFirstName())
                .containsExactlyInAnyOrder("Alice", "Carla");
    }

    @Test
    @DisplayName("findByStatus returns single employee with INACTIVE status")
    void findByStatus_returnsInactiveEmployee() {
        List<Employee> inactives = employeeRepository.findByStatus(WorkStatus.INACTIVE);

        assertThat(inactives).hasSize(1);
        assertThat(inactives.get(0).getUser().getFirstName()).isEqualTo("Bob");
    }

    @Test
    @DisplayName("findByStatus returns empty list when no employees have the given status")
    void findByStatus_returnsEmpty_whenNoMatch() {
        List<Employee> onLeave = employeeRepository.findByStatus(WorkStatus.ON_LEAVE);

        assertThat(onLeave).isEmpty();
    }

    // -------------------------------------------------------------------------
    // findByPosition
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findByPosition returns employee(s) with the exact position title")
    void findByPosition_returnsMatchingEmployees() {
        List<Employee> assistants = employeeRepository.findByPosition("Assitant");

        assertThat(assistants).hasSize(1);
        assertThat(assistants.get(0).getUser().getFirstName()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("findByPosition returns empty list when position does not match any employee")
    void findByPosition_returnsEmpty_whenNoMatch() {
        List<Employee> nurses = employeeRepository.findByPosition("Nurse");

        assertThat(nurses).isEmpty();
    }

    @Test
    @DisplayName("findByPosition is case-sensitive — 'assistant' does not match 'Assistant'")
    void findByPosition_isCaseSensitive() {
        List<Employee> result = employeeRepository.findByPosition("assistant");
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByPosition returns multiple employees sharing the same position")
    void findByPosition_returnsMultiple_whenDuplicatePositions() {
        User userDan = buildUser("Dan", "Grey", "dan@clinic.com");
        entityManager.persist(userDan);
        Employee employeeDan = buildEmployee(userDan, "Assistant", WorkStatus.ACTIVE,
                LocalDate.of(2022, 5, 1));
        entityManager.persist(employeeDan);
        entityManager.flush();

        List<Employee> assistants = employeeRepository.findByPosition("Assistant");

        assertThat(assistants).hasSize(2)
                .extracting(emp -> emp.getUser().getFirstName())
                .containsExactlyInAnyOrder("Carla", "Dan");
    }

    // -------------------------------------------------------------------------
    // findByOfficeId (JPQL)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findByOfficeId returns all employees assigned to the given office")
    void findByOfficeId_returnsMatchingEmployees() {
        List<Employee> officeAEmployees = employeeRepository.findByOfficeId(officeA.getId());

        // Alice and Carla are both in officeA
        assertThat(officeAEmployees).hasSize(2)
                .extracting(emp -> emp.getUser().getFirstName())
                .containsExactlyInAnyOrder("Alice", "Carla");
    }

    @Test
    @DisplayName("findByOfficeId returns a single employee assigned only to officeB")
    void findByOfficeId_returnsSingleEmployee() {
        // Bob is only in officeB; Carla is in both offices
        List<Employee> officeBEmployees = employeeRepository.findByOfficeId(officeB.getId());

        assertThat(officeBEmployees).hasSize(2)
                .extracting(emp -> emp.getUser().getFirstName())
                .containsExactlyInAnyOrder("Bob", "Carla");
    }

    @Test
    @DisplayName("findByOfficeId returns empty list for an office with no employees")
    void findByOfficeId_returnsEmpty_whenNoEmployees() {
        Office emptyOffice = new Office();
        emptyOffice.setName("Empty Office");
        entityManager.persist(emptyOffice);
        entityManager.flush();

        List<Employee> result = employeeRepository.findByOfficeId(emptyOffice.getId());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByOfficeId returns empty list for a non-existent office ID")
    void findByOfficeId_returnsEmpty_forNonExistentOffice() {
        List<Employee> result = employeeRepository.findByOfficeId(9999L);

        assertThat(result).isEmpty();
    }

    // -------------------------------------------------------------------------
    // searchByKeyword (JPQL LIKE across firstName, lastName, email, position)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("searchByKeyword matches on first name (case-insensitive)")
    void searchByKeyword_matchesFirstName() {
        List<Employee> result = employeeRepository.searchByKeyword("alice");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUser().getFirstName()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("searchByKeyword matches on last name (case-insensitive)")
    void searchByKeyword_matchesLastName() {
        List<Employee> result = employeeRepository.searchByKeyword("jones");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUser().getLastName()).isEqualTo("Jones");
    }

    @Test
    @DisplayName("searchByKeyword matches on email domain (partial LIKE)")
    void searchByKeyword_matchesEmailPartial() {
        // 'dentalwave.com' appears in alice and bob emails
        List<Employee> result = employeeRepository.searchByKeyword("dentalwave.com");

        assertThat(result).hasSize(2)
                .extracting(emp -> emp.getUser().getFirstName())
                .containsExactlyInAnyOrder("Alice", "Bob");
    }

    @Test
    @DisplayName("searchByKeyword matches on position title (case-insensitive)")
    void searchByKeyword_matchesPosition() {
        List<Employee> result = employeeRepository.searchByKeyword("assistant");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPosition()).isEqualTo("Assistant");
    }

    @Test
    @DisplayName("searchByKeyword returns multiple employees for a common partial keyword")
    void searchByKeyword_returnsMultipleMatches() {
        // 'a' appears in Alice, Carla (firstName), Smith (lastName "Smi-th" no), etc.
        // Use a targeted keyword present in multiple records
        List<Employee> result = employeeRepository.searchByKeyword("Assistant");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUser().getFirstName()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("searchByKeyword returns empty list when keyword matches nothing")
    void searchByKeyword_returnsEmpty_whenNoMatch() {
        List<Employee> result = employeeRepository.searchByKeyword("xyzzy");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("searchByKeyword matches uppercase keyword against lower-case data")
    void searchByKeyword_isCaseInsensitive_uppercase() {
        List<Employee> result = employeeRepository.searchByKeyword("BOB");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUser().getFirstName()).isEqualTo("Bob");
    }

    @Test
    @DisplayName("searchByKeyword with empty string returns all employees")
    void searchByKeyword_emptyString_returnsAll() {
        List<Employee> result = employeeRepository.searchByKeyword("");

        // '%' + '' + '%' matches every string
        assertThat(result).hasSize(3);
    }

    // -------------------------------------------------------------------------
    // Standard JpaRepository operations
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("save persists a new employee and assigns a generated ID")
    void save_persistsNewEmployee() {
        User newUser = buildUser("Eve", "Turner", "eve@clinic.com");
        entityManager.persist(newUser);

        Employee newEmp = buildEmployee(newUser, "Nurse", WorkStatus.ACTIVE,
                LocalDate.of(2023, 1, 1));
        Employee saved = employeeRepository.save(newEmp);

        assertThat(saved.getId()).isNotNull();
        assertThat(entityManager.find(Employee.class, saved.getId())).isNotNull();
    }

    @Test
    @DisplayName("findById returns the correct employee")
    void findById_returnsEmployee() {
        Optional<Employee> found = employeeRepository.findById(employeeAlice.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getPosition()).isEqualTo("Assistant");
    }

    @Test
    @DisplayName("findById returns empty Optional for non-existent ID")
    void findById_returnsEmpty_whenNotFound() {
        Optional<Employee> found = employeeRepository.findById(9999L);

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("findAll returns all persisted employees")
    void findAll_returnsAllEmployees() {
        List<Employee> all = employeeRepository.findAll();

        assertThat(all).hasSize(3);
    }

    @Test
    @DisplayName("delete removes the employee record")
    void delete_removesEmployee() {
        Long id = employeeBob.getId();
        employeeRepository.delete(employeeBob);
        entityManager.flush();

        assertThat(entityManager.find(Employee.class, id)).isNull();
    }

    @Test
    @DisplayName("count returns the correct number of persisted employees")
    void count_returnsCorrectTotal() {
        assertThat(employeeRepository.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("existsById returns true for a persisted employee")
    void existsById_returnsTrue() {
        assertThat(employeeRepository.existsById(employeeAlice.getId())).isTrue();
    }

    @Test
    @DisplayName("existsById returns false for a non-existent ID")
    void existsById_returnsFalse() {
        assertThat(employeeRepository.existsById(9999L)).isFalse();
    }
}