package com.summerproject2026.DentalWave.repository;

import com.summerproject2026.DentalWave.entity.Employee;
import com.summerproject2026.DentalWave.enums.WorkStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for Employee entities.
 *
 * All query methods are derived from their names or declared with JPQL
 * so Spring Data generates the implementation automatically.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    /**
     * Finds an employee by their linked User account ID.
     * Returns Optional.empty() if no employee is associated with that user.
     */
    Optional<Employee> findByUserId(Long userId);

    /**
     * Returns all employees whose status matches the given WorkStatus.
     * e.g. findByStatus(WorkStatus.ACTIVE)
     */
    List<Employee> findByStatus(WorkStatus status);

    /**
     * Returns all employees with a specific position/job title.
     * Case-sensitive match; adjust to JPQL if you need case-insensitive.
     */
    List<Employee> findByPosition(String position);

    /**
     * Returns all employees assigned to a specific office.
     * JOINs on the employee_offices join table via the offices collection.
     */
    @Query("SELECT e FROM Employee e JOIN e.offices o WHERE o.id = :officeId")
    List<Employee> findByOfficeId(@Param("officeId") Long officeId);

    /**
     * Keyword search across first name, last name, email, and position.
     * Case-insensitive LIKE. Used by EmployeeController#searchEmployees.
     */
    @Query("SELECT e FROM Employee e " +
           "WHERE LOWER(e.user.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "   OR LOWER(e.user.lastName)  LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "   OR LOWER(e.user.email)     LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "   OR LOWER(e.position)       LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Employee> searchByKeyword(@Param("keyword") String keyword);
}
