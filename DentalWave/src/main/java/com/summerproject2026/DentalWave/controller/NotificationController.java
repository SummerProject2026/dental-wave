package com.summerproject2026.DentalWave.controller;

import com.summerproject2026.DentalWave.dto.NotificationDto;
import com.summerproject2026.DentalWave.entity.Notification;
import com.summerproject2026.DentalWave.repository.NotificationRepository;
import com.summerproject2026.DentalWave.repository.UserRepository;
import com.summerproject2026.DentalWave.service.NotificationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
 *
 * Any authenticated user can access their own notifications by userId,
 * since notifications are sent to HR (time-off requests, employee events)
 * as well as to individual employees (approval/denial decisions).
 */
@RestController
@RequestMapping("/api/notifications")
@AllArgsConstructor
public class NotificationController {

    /** Service that contains the notification business logic */
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * GET /api/notifications/user/{userId}
     * Returns all notifications for a specific user.
     *
     * @param userId the id of the recipient user
     * @return 200 OK with list of NotificationDtos
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationDto>> getNotificationsForUser(
            @PathVariable Long userId,
            Authentication authentication) {
        assertCanAccessUserNotifications(userId, authentication);
        return ResponseEntity.ok(notificationService.getNotificationsForUser(userId));
    }

    /**
     * GET /api/notifications/user/{userId}/unread
     * Returns all unread notifications for a specific user.
     *
     * @param userId the id of the recipient user
     * @return 200 OK with list of unread NotificationDtos
     */
    @GetMapping("/user/{userId}/unread")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationDto>> getUnreadNotifications(
            @PathVariable Long userId,
            Authentication authentication) {
        assertCanAccessUserNotifications(userId, authentication);
        return ResponseEntity.ok(notificationService.getUnreadNotifications(userId));
    }

    /**
     * GET /api/notifications/user/{userId}/unread/count
     * Returns the count of unread notifications for a specific user.
     * Used by the frontend to show the notification badge count.
     *
     * @param userId the id of the recipient user
     * @return 200 OK with the count of unread notifications
     */
    @GetMapping("/user/{userId}/unread/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> getUnreadCount(@PathVariable Long userId,
                                               Authentication authentication) {
        assertCanAccessUserNotifications(userId, authentication);
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
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationDto>> getNotificationsByTab(
            @PathVariable Long userId,
            @PathVariable String targetTab,
            Authentication authentication) {
        assertCanAccessUserNotifications(userId, authentication);
        return ResponseEntity.ok(
                notificationService.getUnreadNotificationsByTab(userId, targetTab));
    }

    /**
     * PATCH /api/notifications/{id}/read
     * Marks a single notification as read.
     *
     * @param id the id of the notification
     * @return 200 OK with confirmation message
     */
    @PatchMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> markAsRead(@PathVariable Long id,
                                             Authentication authentication) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new com.summerproject2026.DentalWave.exception.ResourceNotFoundException(
                        "Notification not found with id: " + id));
        assertCanAccessUserNotifications(notification.getRecipient().getId(), authentication);
        notificationService.markAsRead(id);
        return ResponseEntity.ok("Notification " + id + " marked as read.");
    }

    /**
     * PATCH /api/notifications/user/{userId}/read-all
     * Marks all notifications as read for a specific user.
     *
     * @param userId the id of the recipient user
     * @return 200 OK with confirmation message
     */
    @PatchMapping("/user/{userId}/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> markAllAsRead(@PathVariable Long userId,
                                                Authentication authentication) {
        assertCanAccessUserNotifications(userId, authentication);
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(
                "All notifications marked as read for user " + userId + ".");
    }

    private void assertCanAccessUserNotifications(Long userId, Authentication authentication) {
        if (hasAnyAuthority(authentication, "ROLE_HR", "ROLE_MANAGER", "ROLE_ADMIN")) {
            return;
        }

        String principal = authentication != null ? authentication.getName() : null;
        boolean ownsNotifications = principal != null && userRepository.findById(userId)
                .map(user -> principal.equals(user.getUsername()) || principal.equals(user.getEmail()))
                .orElse(false);

        if (!ownsNotifications) {
            throw new AccessDeniedException("Users may only access their own notifications.");
        }
    }

    private boolean hasAnyAuthority(Authentication authentication, String... authorities) {
        if (authentication == null) return false;
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> {
                    String value = grantedAuthority.getAuthority();
                    for (String authority : authorities) {
                        if (authority.equals(value)) return true;
                    }
                    return false;
                });
    }
}
