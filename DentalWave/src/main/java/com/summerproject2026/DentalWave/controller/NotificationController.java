package com.summerproject2026.DentalWave.controller;

import com.summerproject2026.DentalWave.dto.NotificationDto;
import com.summerproject2026.DentalWave.service.NotificationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller exposing notification endpoints.
 * Base path: /api/notifications
 *
 * Responsibilities:
 * - Get all notifications for a user
 * - Get unread notifications for a user
 * - Get unread notification count for badge display
 * - Get unread notifications by tab REQUESTS or EMPLOYEES
 * - Mark a notification as read
 * - Mark all notifications as read
 */
@RestController
@RequestMapping("/api/notifications")
@AllArgsConstructor
public class NotificationController {

    /** Service that contains the notification business logic */
    private final NotificationService notificationService;

    /**
     * GET /api/notifications/user/{userId}
     * Returns all notifications for a specific user.
     * Used by HR to see all notifications in the notification panel.
     *
     * @param userId the id of the recipient user
     * @return 200 OK with list of NotificationDtos
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_HR')")
    public ResponseEntity<List<NotificationDto>> getNotificationsForUser(
            @PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getNotificationsForUser(userId));
    }

    /**
     * GET /api/notifications/user/{userId}/unread
     * Returns all unread notifications for a specific user.
     * Used by HR to see only unread notifications.
     *
     * @param userId the id of the recipient user
     * @return 200 OK with list of unread NotificationDtos
     */
    @GetMapping("/user/{userId}/unread")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_HR')")
    public ResponseEntity<List<NotificationDto>> getUnreadNotifications(
            @PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getUnreadNotifications(userId));
    }

    /**
     * GET /api/notifications/user/{userId}/unread/count
     * Returns the count of unread notifications for a specific user.
     * Used by the frontend to show the notification badge count
     * on the Requests and Employees tabs.
     *
     * @param userId the id of the recipient user
     * @return 200 OK with the count of unread notifications
     */
    @GetMapping("/user/{userId}/unread/count")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_HR')")
    public ResponseEntity<Long> getUnreadCount(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }

    /**
     * GET /api/notifications/user/{userId}/tab/{targetTab}
     * Returns all unread notifications for a specific tab.
     * Used by the frontend to show badge count on
     * REQUESTS tab or EMPLOYEES tab separately.
     *
     * @param userId the id of the recipient user
     * @param targetTab the tab to filter by REQUESTS or EMPLOYEES
     * @return 200 OK with list of unread NotificationDtos for the tab
     */
    @GetMapping("/user/{userId}/tab/{targetTab}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_HR')")
    public ResponseEntity<List<NotificationDto>> getNotificationsByTab(
            @PathVariable Long userId,
            @PathVariable String targetTab) {
        return ResponseEntity.ok(
                notificationService.getUnreadNotificationsByTab(userId, targetTab));
    }

    /**
     * PATCH /api/notifications/{id}/read
     * Marks a single notification as read.
     * Called when HR clicks on a notification.
     *
     * @param id the id of the notification
     * @return 200 OK with confirmation message
     */
    @PatchMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_HR')")
    public ResponseEntity<String> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok("Notification " + id + " marked as read.");
    }

    /**
     * PATCH /api/notifications/user/{userId}/read-all
     * Marks all notifications as read for a specific user.
     * Called when HR opens the notifications panel.
     *
     * @param userId the id of the recipient user
     * @return 200 OK with confirmation message
     */
    @PatchMapping("/user/{userId}/read-all")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_HR')")
    public ResponseEntity<String> markAllAsRead(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(
                "All notifications marked as read for user " + userId + ".");
    }
}