package com.secogroupe.app.dto;

import com.secogroupe.app.entity.EmployeeStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmployeeRequest {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private String email;
    private String phone;

    @NotBlank
    private String position;

    private String department;

    private Double salary;

    @NotNull
    private EmployeeStatus status;
}
