package com.summerproject2026.DentalWave.repository;

import com.summerproject2026.DentalWave.entity.Office;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for {@link Office} entity.
 *
 * <p>Extends {@link JpaRepository} to inherit standard CRUD operations
 * and pagination support out of the box, including:
 * <ul>
 *   <li>{@code save()} — insert or update an office</li>
 *   <li>{@code findById()} — look up an office by its primary key</li>
 *   <li>{@code findAll()} — retrieve all offices</li>
 *   <li>{@code deleteById()} — remove an office by its primary key</li>
 * </ul>
 * </p>
 *
 * <p>Spring Data JPA automatically provides the implementation
 * at runtime — no implementation class is needed.</p>
 */
@Repository
public interface OfficeRepository extends JpaRepository<Office, Long> {

    /**
     * Finds an office by its name.
     *
     * <p>Useful for checking whether an office with a given name
     * already exists before creating a new one (duplicate prevention).</p>
     *
     * <p>Spring Data JPA automatically derives the query from the
     * method name: {@code SELECT * FROM offices WHERE name = ?}</p>
     *
     * @param name the name of the office to search for; must not be null
     * @return an {@link Optional} containing the matching {@link Office}
     *         if found, or an empty {@link Optional} if no match exists
     */
    Optional<Office> findByName(String name);
}