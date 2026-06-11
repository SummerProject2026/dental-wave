package com.summerproject2026.DentalWave.repository;

import com.summerproject2026.DentalWave.entity.ScheduleTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ScheduleTeam entity.
 */
@Repository
public interface ScheduleTeamRepository extends JpaRepository<ScheduleTeam, Long> {

    /** Find all teams belonging to a specific schedule */
    List<ScheduleTeam> findByScheduleId(Long scheduleId);

    /** Find a team by name within a specific schedule */
    Optional<ScheduleTeam> findByScheduleIdAndName(Long scheduleId, String name);

    /** Delete all teams belonging to a specific schedule */
    void deleteByScheduleId(Long scheduleId);
}