package com.summerproject2026.DentalWave.repository;
import com.summerproject2026.DentalWave.entity.DoctorWorkRule;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.DayOfWeek;
import java.util.List;
public interface DoctorWorkRuleRepository extends JpaRepository<DoctorWorkRule, Long> {
    @EntityGraph(attributePaths = {
            "doctor", "doctor.defaultOffice", "doctor.offices", "office", "rotationGroup"
    })
    List<DoctorWorkRule> findByDoctorIdOrderByDayOfWeekAsc(Long doctorId);
    @EntityGraph(attributePaths = {
            "doctor", "doctor.defaultOffice", "doctor.offices", "office", "rotationGroup"
    })
    List<DoctorWorkRule> findByDoctorIdAndDayOfWeekAndActiveTrue(Long doctorId, DayOfWeek day);
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from DoctorWorkRule rule where rule.doctor.id = :doctorId")
    int deleteByDoctorId(@Param("doctorId") Long doctorId);
    boolean existsByRotationGroupId(Long rotationGroupId);
}
