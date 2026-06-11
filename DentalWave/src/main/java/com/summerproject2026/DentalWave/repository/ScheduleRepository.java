package com.summerproject2026.DentalWave.repository;

import com.summerproject2026.DentalWave.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA repository for Schedule entities.
 */
@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    /** Returns all schedules that fall on the given date */
    List<Schedule> findByDate(LocalDate date);

    /** Returns all schedules belonging to a given calendar */
    List<Schedule> findByCalendarId(Long calendarId);

    @Query("SELECT DISTINCT s FROM Schedule s JOIN s.teams t JOIN t.employees e JOIN e.user u WHERE LOWER(CONCAT(u.firstName, ' ', u.lastName)) = LOWER(:employeeName) AND s.published = true")
    List<Schedule> findPublishedSchedulesByEmployeeName(@Param("employeeName") String employeeName);

    /**
     * Returns all published schedules assigned to a given employee.
     * Used by UC2 — Employee Views Personal Calendar.
     */
    @Query("SELECT DISTINCT s FROM Schedule s JOIN s.teams t JOIN t.employees e WHERE e.id = :employeeId AND s.published = true")
    List<Schedule> findPublishedSchedulesByEmployeeId(@Param("employeeId") Long employeeId);
}