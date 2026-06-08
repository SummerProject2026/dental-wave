package com.summerproject2026.DentalWave.repository;

import com.summerproject2026.DentalWave.entity.TimeOffRequest;
import com.summerproject2026.DentalWave.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for {@link TimeOffRequest} entity.
 *
 * <p>Extends {@link JpaRepository} to inherit standard CRUD operations
 * and pagination support out of the box, including:
 * <ul>
 *   <li>{@code save()} — insert or update a time-off request</li>
 *   <li>{@code findById()} — look up a request by its primary key</li>
 *   <li>{@code findAll()} — retrieve all time-off requests</li>
 *   <li>{@code deleteById()} — remove a request by its primary key</li>
 * </ul>
 * </p>
 *
 * <p>Spring Data JPA automatically provides the implementation
 * at runtime — no implementation class is needed.</p>
 */
@Repository
public interface TimeOffRequestRepository extends JpaRepository<TimeOffRequest, Long> {

    /**
     * Finds all time-off requests submitted by a specific employee.
     *
     * <p>Used to display a history of requests for a given employee,
     * such as on an employee profile or manager review dashboard.</p>
     *
     * <p>Spring Data JPA automatically derives the query from the
     * method name: {@code SELECT * FROM time_off_requests WHERE employee_id = ?}</p>
     *
     * @param employeeId the ID of the employee whose requests to retrieve;
     *                   must not be null
     * @return a {@link List} of {@link TimeOffRequest} entities belonging
     *         to the specified employee; returns an empty list if none found
     */
    List<TimeOffRequest> findByEmployeeId(Long employeeId);

    /**
     * Finds all time-off requests with a specific status.
     *
     * <p>Useful for filtering requests by their current state, such as
     * retrieving all {@code PENDING} requests for a manager approval queue,
     * or all {@code APPROVED} requests for scheduling purposes.</p>
     *
     * <p>Spring Data JPA automatically derives the query from the
     * method name: {@code SELECT * FROM time_off_requests WHERE status = ?}</p>
     *
     * @param status the {@link RequestStatus} to filter by (PENDING, APPROVED, DENIED);
     *               must not be null
     * @return a {@link List} of {@link TimeOffRequest} entities matching
     *         the specified status; returns an empty list if none found
     */
    List<TimeOffRequest> findByStatus(RequestStatus status);
}