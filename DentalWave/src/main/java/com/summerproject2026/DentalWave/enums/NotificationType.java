
package com.summerproject2026.DentalWave.enums;

/**
 * Enum representing the different types of notifications
 * in the DentalWave system.
 *
 * Notifications are grouped by target tab so the frontend
 * knows which tab to show the notification badge on.
 *
 * REQUESTS tab — time-off related notifications
 * EMPLOYEES tab — employee status change notifications
 */
public enum NotificationType {


    // REQUESTS TAB NOTIFICATIONS
    // These appear as a badge on the Requests tab


    /** Sent to HR when an employee submits a time-off request */
    TIME_OFF_REQUEST,

    /** Sent to HR when an employee submits an emergency time-off request */
    EMERGENCY_REQUEST,

    /** Sent to employee when their time-off request is approved by HR */
    TIME_OFF_APPROVED,

    /** Sent to employee when their time-off request is denied by HR */
    TIME_OFF_DENIED,


    // EMPLOYEES TAB NOTIFICATIONS
    // These appear as a badge on the Employees tab

    /** Sent to HR when a new employee is hired */
    EMPLOYEE_HIRED,

    /** Sent to HR when an employee is terminated */
    EMPLOYEE_FIRED,

    /** Sent to HR when an employee is promoted */
    EMPLOYEE_PROMOTED,


    // CALENDAR TAB NOTIFICATIONS (UC12)
    // These appear as a badge on the Calendar tab for employees

    /**
     * Sent to an employee when a calendar covering them is published
     * for the first time — i.e. a brand-new schedule is available.
     */
    NEW_SCHEDULE,

    /**
     * Sent to an employee when a post-publish edit directly affects
     * their assignment (added to, removed from, or reassigned on a day).
     */
    SCHEDULE_UPDATE,


    // GENERAL NOTIFICATIONS

    /** Sent to manager when an approved time-off affects the schedule */
    SCHEDULE_UPDATE_REQUIRED,

    /** General system notification */
    SYSTEM
}
