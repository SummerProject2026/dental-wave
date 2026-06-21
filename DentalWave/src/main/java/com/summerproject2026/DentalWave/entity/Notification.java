package com.summerproject2026.DentalWave.entity;

import com.summerproject2026.DentalWave.enums.NotificationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing an in-app notification.
 *
 * Notifications are created when events occur in the system such as:
 * - Employee submits a time-off request (REQUESTS tab badge)
 * - Employee submits an emergency request (REQUESTS tab badge)
 * - New employee is hired (EMPLOYEES tab badge)
 * - Employee is terminated (EMPLOYEES tab badge)
 * - Employee is promoted (EMPLOYEES tab badge)
 *
 * The targetTab field tells the frontend which tab
 * to show the notification badge on.
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    /** Unique identifier for the notification */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user who receives the notification.
     * Usually HR for time-off requests and employee events.
     */
    @ManyToOne
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    /**
     * The notification message content.
     * e.g. "Jane Smith submitted a time-off request"
     * e.g. "EMERGENCY: John Doe submitted an emergency request"
     * e.g. "New employee Alice Smith has been hired"
     */
    @Column(nullable = false)
    private String message;

    /**
     * Whether the notification has been read by the recipient.
     * Used to show/hide the badge on the frontend.
     * Defaults to false when created.
     */
    @Column(nullable = false)
    private Boolean read = false;

    /**
     * When the notification was created.
     * Set server-side when the notification is created.
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * The type of notification.
     * Used by the frontend to determine how to display
     * and where to navigate when clicked.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    /**
     * The tab where the notification badge appears.
     * REQUESTS — for time-off related notifications
     * EMPLOYEES — for employee status change notifications
     */
    @Column(nullable = false)
    private String targetTab;

    /**
     * The id of the related entity.
     * For time-off requests this is the time-off request id.
     * For employee events this is the employee id.
     * Used by the frontend to navigate to the correct detail page.
     */
    private Long referenceId;
}