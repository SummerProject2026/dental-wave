package com.summerproject2026.DentalWave.mapper;

import com.summerproject2026.DentalWave.dto.OfficeDto;
import com.summerproject2026.DentalWave.entity.Office;
import org.springframework.stereotype.Component;
/**
 * Mapper class responsible for converting between
 * {@link Office} entity and {@link OfficeDto} objects.
 *
 * <p>Registered as a Spring component so it can be
 * injected wherever mapping is needed (e.g. service layer).</p>
 */
@Component
public class OfficeMapper {

    /**
     * Converts an {@link Office} entity to an {@link OfficeDto}.
     *
     * @param office the Office entity to convert; must not be null
     * @return a populated {@link OfficeDto} reflecting the entity's data
     * @throws IllegalArgumentException if the provided office is null
     */
    public OfficeDto mapToOfficeDto(Office office) {
        if (office == null) {
            throw new IllegalArgumentException("Office entity must not be null");
        }

        return new OfficeDto(
                office.getId(),
                office.getName(),
                office.getAddress(),
                office.getPhoneNumber()
        );
    }

    /**
     * Converts an {@link OfficeDto} to an {@link Office} entity.
     *
     * <p>Typically used when creating or updating an Office
     * from incoming request data.</p>
     *
     * @param officeDto the OfficeDto to convert; must not be null
     * @return a populated {@link Office} entity reflecting the DTO's data
     * @throws IllegalArgumentException if the provided officeDto is null
     */
    public Office mapToOffice(OfficeDto officeDto) {
        if (officeDto == null) {
            throw new IllegalArgumentException("OfficeDto must not be null");
        }

        return new Office(
                officeDto.getId(),
                officeDto.getName(),
                officeDto.getAddress(),
                officeDto.getPhoneNumber()
        );
    }
}