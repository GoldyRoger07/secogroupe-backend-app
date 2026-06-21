package com.secogroupe.app.dto;

import com.secogroupe.app.entity.ApplicationStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApplicationUpdateRequest {

    @NotNull(message = "Le statut est obligatoire")
    private ApplicationStatus status;

    private String adminNotes;
}
