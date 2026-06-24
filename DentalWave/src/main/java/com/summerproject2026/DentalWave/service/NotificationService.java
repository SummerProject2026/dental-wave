package com.summerproject2026.DentalWave.service;

import com.summerproject2026.DentalWave.dto.NotificationDto;
import com.summerproject2026.DentalWave.enums.NotificationType;

import java.util.List;

/**
 * Service interface for managing notifications.
 * Handles both in-app and email notifications.
 */
public interface NotificationService {

    /**
     * Creates an in-app notification and sends email to HR
     * when an employee submits a time-off request.
     *
     * @param employeeName the full name of the employee
     * @param requestId the id of the time-off request
     * @param isEmergency whether the request is an emergency
     * @param recipientId the id of the HR user to notify
     */
    void notifyHrOfTimeOffRequest(String employeeName,
                                  Long requestId,
                                  boolean isEmergency,
                                  Long recipientId);

    /**
     * Creates and saves an in-app notification for a specific user.
     *
     * @param recipientId the id of the recipient user
     * @param message the notification message
     * @param type the type of notification
     * @param targetTab the tab where the badge appears REQUESTS or EMPLOYEES
     * @param referenceId the id of the related entity
     */
    void sendNotification(Long recipientId,
                          String message,
                          NotificationType type,
                          String targetTab,
                          Long referenceId);

    /**
     * Returns all notifications for a specific recipient.
     *
     * @param recipientId the id of the recipient user
     * @return list of NotificationDtos
     */
    List<NotificationDto> getNotificationsForUser(Long recipientId);

    /**
     * Returns all unread notifications for a specific recipient.
     *
     * @param recipientId the id of the recipient user
     * @return list of unread NotificationDtos
     */
    List<NotificationDto> getUnreadNotifications(Long recipientId);

    /**
     * Returns all unread notifications for a specific tab.
     * Used by the frontend to show badge count on
     * REQUESTS tab or EMPLOYEES tab separately.
     *
     * @param recipientId the id of the recipient user
     * @param targetTab the tab to filter by REQUESTS or EMPLOYEES
     * @return list of unread NotificationDtos for the tab
     */
    List<NotificationDto> getUnreadNotificationsByTab(Long recipientId, String targetTab);

    /**
     * Returns the count of unread notifications for HR badge display.
     *
     * @param recipientId the id of the recipient user
     * @return count of unread notifications
     */
    Long getUnreadCount(Long recipientId);

    /**
     * Marks a single notification as read.
     *
     * @param notificationId the id of the notification
     */
    void markAsRead(Long notificationId);

    /**
     * Marks all notifications as read for a specific recipient.
     *
     * @param recipientId the id of the recipient user
     */
    void markAllAsRead(Long recipientId);

    /**
     * Sends an email notification to HR.
     *
     * @param employeeName the name of the employee
     * @param requestId the id of the time-off request
     * @param isEmergency whether the request is an emergency
     * @param recipientId the id of the HR user
     */
    void sendEmailToHr(String employeeName,
                       Long requestId,
                       boolean isEmergency,
                       Long recipientId);
}
