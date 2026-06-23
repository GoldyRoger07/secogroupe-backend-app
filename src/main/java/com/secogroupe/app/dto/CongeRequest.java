package com.secogroupe.app.dto;

import java.time.LocalDate;

import com.secogroupe.app.model.CongeStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CongeRequest {

    @NotNull
    private Long employeeId;

    @NotBlank
    private String typeConge;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    private Integer numberOfDays;

    private String reason;

    private CongeStatus status;

    private String managerComment;

    private Long approvedById;
}
