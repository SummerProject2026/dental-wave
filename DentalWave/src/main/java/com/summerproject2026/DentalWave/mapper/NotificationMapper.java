
package com.summerproject2026.DentalWave.mapper;

import com.summerproject2026.DentalWave.dto.NotificationDto;
import com.summerproject2026.DentalWave.entity.Notification;
import org.springframework.stereotype.Component;

/**
 * Mapper class for converting between Notification entities
 * and NotificationDto objects.
 */
@Component
public class NotificationMapper {

    /**
     * Maps a Notification entity to a NotificationDto.
     *
     * @param notification the entity to map
     * @return the mapped NotificationDto
     */
    public static NotificationDto toDto(Notification notification) {
        // Create new DTO
        NotificationDto dto = new NotificationDto();

        // Map all fields from entity to DTO
        dto.setId(notification.getId());
        dto.setRecipientId(notification.getRecipient().getId());
        dto.setRecipientUsername(notification.getRecipient().getUsername());
        dto.setMessage(notification.getMessage());
        dto.setRead(notification.getRead());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setType(notification.getType());
        dto.setTargetTab(notification.getTargetTab());
        dto.setReferenceId(notification.getReferenceId());

        return dto;
    }

    /**
     * Maps a NotificationDto to a Notification entity.
     * Note: recipient is not set here as it requires
     * a database lookup in the service layer.
     *
     * @param dto the DTO to map
     * @return the mapped Notification entity
     */
    public static Notification toEntity(NotificationDto dto) {
        // Create new entity
        Notification notification = new Notification();

        // Map all fields from DTO to entity
        notification.setId(dto.getId());
        notification.setMessage(dto.getMessage());
        notification.setRead(dto.getRead());
        notification.setCreatedAt(dto.getCreatedAt());
        notification.setType(dto.getType());
        notification.setTargetTab(dto.getTargetTab());
        notification.setReferenceId(dto.getReferenceId());

        return notification;
    }
}
