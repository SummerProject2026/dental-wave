package com.summerproject2026.DentalWave.repository;

import com.summerproject2026.DentalWave.entity.Office;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Office entities.
 */
@Repository
public interface OfficeRepository extends JpaRepository<Office, Long> {

}