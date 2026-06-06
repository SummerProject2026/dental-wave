package com.summerproject2026.DentalWave.repository;

import com.summerproject2026.DentalWave.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    // Finds users by role name
    List<User> findByRoles_NameIgnoreCase(String roleName);

    // Searches for a user given a keyword, such as name, username, or email
    // Used ChatGPT to generate this part.
    @Query("""
    SELECT u FROM User u
    WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))
       OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))
       OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
       OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
    List<User> searchByKeyword(@Param("keyword") String keyword);
}