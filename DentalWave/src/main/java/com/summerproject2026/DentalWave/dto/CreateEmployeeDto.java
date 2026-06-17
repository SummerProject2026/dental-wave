package com.summerproject2026.DentalWave.dto;

/**
 * DTO used when HR creates a new employee.
 *
 * This DTO combines:
 *  - User account information (RegisterDto)
 *  - Employee profile information (EmployeeDto)
 *  - Security role assignment
 *
 * The User portion is used to create the employee's login account,
 * while the Employee portion contains HR and scheduling-related data.
 *
 * Example:
 * {
 *     "user": { ... },
 *     "employee": { ... },
 *     "role": "ROLE_ASSISTANT"
 * }
 */
public class CreateEmployeeDto {

    /**
     * User account information used to create the login.
     */
    private RegisterDto user;

    /**
     * Employee profile information.
     */
    private EmployeeDto employee;

    /**
     * Security role assigned to the new user.
     *
     * Valid values:
     *  - ROLE_ASSISTANT
     *  - ROLE_HR
     *  - ROLE_MANAGER
     *  - ROLE_ADMIN
     */
    private String role;

    /**
     * Default no-argument constructor.
     */
    public CreateEmployeeDto() {
    }

    /**
     * Returns the user account information.
     *
     * @return RegisterDto containing login account details
     */
    public RegisterDto getUser() {
        return user;
    }

    /**
     * Sets the user account information.
     *
     * @param user RegisterDto containing login account details
     */
    public void setUser(RegisterDto user) {
        this.user = user;
    }

    /**
     * Returns the employee profile information.
     *
     * @return EmployeeDto containing employee-specific details
     */
    public EmployeeDto getEmployee() {
        return employee;
    }

    /**
     * Sets the employee profile information.
     *
     * @param employee EmployeeDto containing employee-specific details
     */
    public void setEmployee(EmployeeDto employee) {
        this.employee = employee;
    }

    /**
     * Returns the security role assigned to the user.
     *
     * @return role name
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the security role assigned to the user.
     *
     * @param role role name (e.g. ROLE_ASSISTANT)
     */
    public void setRole(String role) {
        this.role = role;
    }
}