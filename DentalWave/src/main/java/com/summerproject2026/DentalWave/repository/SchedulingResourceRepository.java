package com.summerproject2026.DentalWave.repository;
import com.summerproject2026.DentalWave.entity.SchedulingResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.Optional;
import java.util.List;
public interface SchedulingResourceRepository extends JpaRepository<SchedulingResource, Long> {
    @Override
    @EntityGraph(attributePaths = {"defaultOffice", "offices"})
    Optional<SchedulingResource> findById(Long id);

    @EntityGraph(attributePaths = {"defaultOffice", "offices"})
    List<SchedulingResource> findByTypeOrderByLastNameAsc(SchedulingResource.Type type);

    @EntityGraph(attributePaths = {"defaultOffice", "offices"})
    List<SchedulingResource> findByTypeAndActiveTrue(SchedulingResource.Type type);
}
