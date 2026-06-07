package com.summerproject2026.DentalWave.repository;


import com.summerproject2026.DentalWave.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    // Find a role by its name e.g. ROLE_ASSISTANT, ROLE_HR, ROLE_MANAGER
    Optional<Role> findByName(String name);
}