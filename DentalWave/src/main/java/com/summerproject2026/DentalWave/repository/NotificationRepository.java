package com.summerproject2026.DentalWave.repository;

import com.summerproject2026.DentalWave.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Notification persistence operations.
 * Provides methods to find, count, and filter notifications
 * by recipient, read status, and target tab.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Find all notifications for a specific recipient.
     * Used to display all notifications in the notification panel.
     *
     * @param recipientId the id of the recipient user
     * @return list of all notifications for the recipient
     */
    List<Notification> findByRecipientId(Long recipientId);

    /**
     * Find all unread notifications for a specific recipient.
     * Used to display unread notifications in the notification panel.
     *
     * @param recipientId the id of the recipient user
     * @return list of unread notifications for the recipient
     */
    List<Notification> findByRecipientIdAndReadFalse(Long recipientId);

    /**
     * Count unread notifications for a specific recipient.
     * Used by the frontend to show the notification badge count.
     *
     * @param recipientId the id of the recipient user
     * @return count of unread notifications
     */
    Long countByRecipientIdAndReadFalse(Long recipientId);

    /**
     * Find all unread notifications for a specific recipient and tab.
     * Used by the frontend to show badge on REQUESTS or EMPLOYEES tab.
     *
     * @param recipientId the id of the recipient user
     * @param targetTab the tab to filter by REQUESTS or EMPLOYEES
     * @return list of unread notifications for the recipient and tab
     */
    List<Notification> findByRecipientIdAndReadFalseAndTargetTab(
            Long recipientId, String targetTab);
}