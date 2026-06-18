package com.secogroupe.app.dto;

import com.secogroupe.app.entity.EmployeeStatus;

import lombok.Data;

@Data
public class EmployeeResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String position;
    private String department;
    private Double salary;
    private String photoUrl;
    private EmployeeStatus status;
    private String createdAt;
}
