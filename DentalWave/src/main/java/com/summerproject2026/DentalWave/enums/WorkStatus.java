package com.summerproject2026.DentalWave.enums;

/**
 * Represents the current employment/work status of an Employee.
 * Used for filtering and scheduling logic.
 */
public enum WorkStatus {

    /** Employee is actively working */
    ACTIVE,

    /** Employee is on approved leave (vacation, medical, etc.) */
    ON_LEAVE,

    /** Employee is on approved time-off */
    ON_TIME_OFF,

    /** Employee is part-time */
    PART_TIME,

    /** Employment has ended */
    TERMINATED,

    /** Employee is inactive / suspended */
    INACTIVE
}
