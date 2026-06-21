package com.summerproject2026.DentalWave.dto;

import com.summerproject2026.DentalWave.enums.NotificationType;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Notification.
 * Used to transfer notification data to the frontend.
 *
 * The targetTab field tells the frontend which tab
 * to show the notification badge on:
 * REQUESTS — for time-off related notifications
 * EMPLOYEES — for employee status change notifications
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {

    /** The notification id */
    private Long id;

    /** The recipient user id */
    private Long recipientId;

    /** The recipient username */
    private String recipientUsername;

    /** The notification message content */
    private String message;

    /** Whether the notification has been read */
    private Boolean read;

    /** When the notification was created */
    private LocalDateTime createdAt;

    /** The type of notification */
    private NotificationType type;

    /**
     * The tab where the notification badge appears.
     * REQUESTS or EMPLOYEES
     */
    private String targetTab;

    /** The id of the related entity e.g. time-off request id or employee id */
    private Long referenceId;
}