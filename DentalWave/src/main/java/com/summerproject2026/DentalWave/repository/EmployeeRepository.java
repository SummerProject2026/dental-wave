package com.dentalwave.repository;

import com.dentalwave.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA repository for Employee entities */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    // Add custom queries (e.g. findByRole) as the Employee entity grows
}
