package com.summerproject2026.DentalWave.service.impl;

import com.summerproject2026.DentalWave.dto.NotificationDto;
import com.summerproject2026.DentalWave.entity.Notification;
import com.summerproject2026.DentalWave.entity.User;
import com.summerproject2026.DentalWave.enums.NotificationType;
import com.summerproject2026.DentalWave.exception.ResourceNotFoundException;
import com.summerproject2026.DentalWave.repository.NotificationRepository;
import com.summerproject2026.DentalWave.repository.UserRepository;
import com.summerproject2026.DentalWave.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of {@link NotificationService}.
 *
 * Responsibilities:
 * - Create in-app notifications for HR and employees
 * - Send email notifications
 * - Mark notifications as read
 * - Return notifications for a specific user
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    /** Repository for notification persistence */
    private final NotificationRepository notificationRepository;

    /** Repository for user lookups */
    private final UserRepository userRepository;

    /** Spring mail sender for email notifications */
    private final JavaMailSender mailSender;

    /**
     * Creates an in-app notification and sends email to HR
     * when an employee submits a time-off request.
     *
     * @param employeeName the full name of the employee
     * @param requestId the id of the time-off request
     * @param isEmergency whether the request is an emergency
     * @param recipientId the id of the HR user to notify
     */
    @Override
    @Transactional
    public void notifyHrOfTimeOffRequest(String employeeName,
                                         Long requestId,
                                         boolean isEmergency,
                                         Long recipientId) {
        // Determine notification type based on emergency flag
        NotificationType type = isEmergency
                ? NotificationType.EMERGENCY_REQUEST
                : NotificationType.TIME_OFF_REQUEST;

        // Build the notification message
        String message = isEmergency
                ? "EMERGENCY: " + employeeName + " submitted an emergency time-off request."
                : employeeName + " submitted a time-off request.";

        // Create and save the in-app notification
        sendNotification(recipientId, message, type, "REQUESTS", requestId);

        // Send email notification to HR
        sendEmailToHr(employeeName, requestId, isEmergency, recipientId);
    }

    /**
     * Creates and saves an in-app notification for a specific user.
     *
     * @param recipientId the id of the recipient user
     * @param message the notification message
     * @param type the type of notification
     * @param targetTab the tab where the badge appears REQUESTS or EMPLOYEES
     * @param referenceId the id of the related entity
     */
    @Override
    @Transactional
    public void sendNotification(Long recipientId,
                                 String message,
                                 NotificationType type,
                                 String targetTab,
                                 Long referenceId) {
        // Find the recipient user
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + recipientId));

        // Build the notification entity
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setMessage(message);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setType(type);
        notification.setTargetTab(targetTab);
        notification.setReferenceId(referenceId);

        // Save the notification
        notificationRepository.save(notification);

        log.info("Notification sent to user {} — {}", recipientId, message);
    }

    /**
     * Returns all notifications for a specific recipient.
     *
     * @param recipientId the id of the recipient user
     * @return list of NotificationDtos
     */
    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> getNotificationsForUser(Long recipientId) {
        // Verify user exists
        if (!userRepository.existsById(recipientId)) {
            throw new ResourceNotFoundException(
                    "User not found with id: " + recipientId);
        }

        // Fetch and map notifications
        return notificationRepository.findByRecipientId(recipientId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Returns all unread notifications for a specific recipient.
     *
     * @param recipientId the id of the recipient user
     * @return list of unread NotificationDtos
     */
    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> getUnreadNotifications(Long recipientId) {
        // Verify user exists
        if (!userRepository.existsById(recipientId)) {
            throw new ResourceNotFoundException(
                    "User not found with id: " + recipientId);
        }

        // Fetch and map unread notifications
        return notificationRepository.findByRecipientIdAndReadFalse(recipientId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Returns all unread notifications for a specific tab.
     *
     * @param recipientId the id of the recipient user
     * @param targetTab the tab to filter by REQUESTS or EMPLOYEES
     * @return list of unread NotificationDtos for the tab
     */
    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> getUnreadNotificationsByTab(Long recipientId,
                                                             String targetTab) {
        // Verify user exists
        if (!userRepository.existsById(recipientId)) {
            throw new ResourceNotFoundException(
                    "User not found with id: " + recipientId);
        }

        // Fetch and map unread notifications for the tab
        return notificationRepository
                .findByRecipientIdAndReadFalseAndTargetTab(recipientId, targetTab)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Returns the count of unread notifications for a specific recipient.
     *
     * @param recipientId the id of the recipient user
     * @return count of unread notifications
     */
    @Override
    @Transactional(readOnly = true)
    public Long getUnreadCount(Long recipientId) {
        // Verify user exists
        if (!userRepository.existsById(recipientId)) {
            throw new ResourceNotFoundException(
                    "User not found with id: " + recipientId);
        }

        // Return count of unread notifications
        return notificationRepository.countByRecipientIdAndReadFalse(recipientId);
    }

    /**
     * Marks a single notification as read.
     *
     * @param notificationId the id of the notification
     */
    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        // Find the notification
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification not found with id: " + notificationId));

        // Mark as read and save
        notification.setRead(true);
        notificationRepository.save(notification);

        log.info("Notification {} marked as read", notificationId);
    }

    /**
     * Marks all notifications as read for a specific recipient.
     *
     * @param recipientId the id of the recipient user
     */
    @Override
    @Transactional
    public void markAllAsRead(Long recipientId) {
        // Fetch all unread notifications for the recipient
        List<Notification> unread = notificationRepository
                .findByRecipientIdAndReadFalse(recipientId);

        // Mark all as read
        unread.forEach(n -> n.setRead(true));

        // Save all
        notificationRepository.saveAll(unread);

        log.info("All notifications marked as read for user {}", recipientId);
    }

    /**
     * Sends an email notification to HR.
     *
     * @param employeeName the name of the employee
     * @param requestId the id of the time-off request
     * @param isEmergency whether the request is an emergency
     * @param recipientId the id of the HR user
     */
    @Override
    public void sendEmailToHr(String employeeName,
                              Long requestId,
                              boolean isEmergency,
                              Long recipientId) {
        try {
            // Find the HR user to get their email
            User recipient = userRepository.findById(recipientId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "User not found with id: " + recipientId));

            // Build the email
            SimpleMailMessage email = new SimpleMailMessage();
            email.setTo(recipient.getEmail());
            email.setSubject(isEmergency
                    ? "EMERGENCY Time-Off Request from " + employeeName
                    : "New Time-Off Request from " + employeeName);
            email.setText(
                    "Dear HR,\n\n" +
                            (isEmergency ? "EMERGENCY REQUEST\n\n" : "") +
                            employeeName + " has submitted a time-off request.\n\n" +
                            "Request ID: " + requestId + "\n\n" +
                            "Please log in to review the request:\n" +
                            "http://localhost:3000/requests/" + requestId + "\n\n" +
                            "DentalWave System"
            );

            // Send the email
            mailSender.send(email);

            log.info("Email sent to HR {} for request {}",
                    recipient.getEmail(), requestId);

        } catch (Exception e) {
            // Log the error but don't fail the request submission
            log.error("Failed to send email notification for request {}: {}",
                    requestId, e.getMessage());
        }
    }

    /**
     * Maps a Notification entity to a NotificationDto.
     *
     * @param notification the entity to map
     * @return the mapped NotificationDto
     */
    private NotificationDto mapToDto(Notification notification) {
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
}