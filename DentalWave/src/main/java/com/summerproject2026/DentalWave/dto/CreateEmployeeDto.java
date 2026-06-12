package com.summerproject2026.DentalWave.dto;
import com.summerproject2026.DentalWave.enums.WorkStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CreateEmployeeDto {
    private RegisterDto user;
    private EmployeeDto employee;

    public CreateEmployeeDto() {}

    public RegisterDto getUser() {
        return user;
    }

    public void setUser(RegisterDto user) {
        this.user = user;
    }

    public EmployeeDto getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeDto employee) {
        this.employee = employee;
    }
}