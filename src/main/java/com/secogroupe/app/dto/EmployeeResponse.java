package com.secogroupe.app.dto;

import lombok.Data;

@Data
public class EmployeeResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String position;
    private Double salary;

    // getters/setters
}
