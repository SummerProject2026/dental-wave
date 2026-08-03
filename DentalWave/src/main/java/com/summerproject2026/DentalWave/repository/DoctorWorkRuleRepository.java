package com.summerproject2026.DentalWave.repository;
import com.summerproject2026.DentalWave.entity.DoctorWorkRule;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.DayOfWeek;
import java.util.List;
public interface DoctorWorkRuleRepository extends JpaRepository<DoctorWorkRule, Long> {
    @EntityGraph(attributePaths = {"doctor", "office", "rotationGroup"})
    List<DoctorWorkRule> findByDoctorIdOrderByDayOfWeekAsc(Long doctorId);
    @EntityGraph(attributePaths = {"doctor", "office", "rotationGroup"})
    List<DoctorWorkRule> findByDoctorIdAndDayOfWeekAndActiveTrue(Long doctorId, DayOfWeek day);
    boolean existsByRotationGroupId(Long rotationGroupId);
}
