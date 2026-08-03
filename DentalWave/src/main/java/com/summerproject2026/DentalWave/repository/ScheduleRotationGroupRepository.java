package com.summerproject2026.DentalWave.repository;
import com.summerproject2026.DentalWave.entity.ScheduleRotationGroup;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ScheduleRotationGroupRepository extends JpaRepository<ScheduleRotationGroup, Long> {
    boolean existsByNameIgnoreCase(String name);
}
