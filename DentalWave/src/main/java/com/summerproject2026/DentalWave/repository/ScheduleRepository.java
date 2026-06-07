package com.dentalwave.repository;

import com.dentalwave.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
