package com.dentalwave.repository;

import com.dentalwave.model.Availability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for Availability entities.
 */
@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {

    /** Returns all availability records for a given employee */
    List<Availability> findByEmployeeId(Long employeeId);
}
