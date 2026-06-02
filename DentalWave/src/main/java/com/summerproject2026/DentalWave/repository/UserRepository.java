package com.summerproject2026.DentalWave.repository;

import com.summerproject2026.DentalWave.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find a user by their username for login authentication
    Optional<User> findByUsername(String username);

    // Find a user by their email address
    Optional<User> findByEmail(String email);

    // Check if a username already exists
    Boolean existsByUsername(String username);

    // Check if an email already exists
    Boolean existsByEmail(String email);
}