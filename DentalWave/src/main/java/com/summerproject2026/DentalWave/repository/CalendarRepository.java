// ─────────────────────────────────────────────────────────────────────────────
// CalendarRepository.java
// ─────────────────────────────────────────────────────────────────────────────
package com.summerproject2026.DentalWave.repository;

import com.summerproject2026.DentalWave.entity.Calendar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for Calendar entities.
 * Custom query methods are derived from method names by Spring Data.
 */
@Repository
public interface CalendarRepository extends JpaRepository<Calendar, Long> {

    /** Returns all calendars with the given month label */
    List<Calendar> findByMonth(String month);

    /** Returns all calendars where published = true */
    List<Calendar> findByPublishedTrue();
}
