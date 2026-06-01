package com.summerproject2026.assistant_scheduler.config;

public final class Roles {

    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    public enum UserRoles {

        /** Assistant employee */
        ROLE_ASSISTANT,

        /** Human Resources employee */
        ROLE_HR,

        /** Office manager responsible for creating schedules */
        ROLE_MANAGER,

        /** System administrator with full access */
        ROLE_ADMIN

    }
}