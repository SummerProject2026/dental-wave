package com.summerproject2026.DentalWave.repository;
import com.summerproject2026.DentalWave.entity.ReusableTeam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReusableTeamRepository extends JpaRepository<ReusableTeam, Long> {
    boolean existsByNameIgnoreCase(String name);
    Optional<ReusableTeam> findByNameIgnoreCase(String name);
    boolean existsByDoctorId(Long doctorId);
}
