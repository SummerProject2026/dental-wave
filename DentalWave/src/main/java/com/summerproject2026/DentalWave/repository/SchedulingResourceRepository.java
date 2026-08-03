package com.summerproject2026.DentalWave.repository;
import com.summerproject2026.DentalWave.entity.SchedulingResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.List;
public interface SchedulingResourceRepository extends JpaRepository<SchedulingResource, Long> {
    @EntityGraph(attributePaths = {"defaultOffice", "offices"})
    List<SchedulingResource> findByTypeOrderByLastNameAsc(SchedulingResource.Type type);

    @EntityGraph(attributePaths = {"defaultOffice", "offices"})
    List<SchedulingResource> findByTypeAndActiveTrue(SchedulingResource.Type type);
}
