package com.secogroupe.app.dto;

import java.time.LocalDate;

import com.secogroupe.app.model.CongeStatus;

import lombok.Data;

@Data
public class CongeResponse {
    private Long id;
    private Long employeeId;
    private String employeeFullName;
    private String typeConge;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer numberOfDays;
    private String reason;
    private CongeStatus status;
    private Long approvedById;
    private String approvedByFullName;
    private String managerComment;
    private String decisionDate;
    private String createdAt;
    private String updatedAt;
}
