package com.summerproject2026.DentalWave.config;

public final class Roles {

    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_ASSISTANT = "ROLE_ASSISTANT";
    public static final String ROLE_HR = "ROLE_HR";
    public static final String ROLE_MANAGER = "ROLE_MANAGER";

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