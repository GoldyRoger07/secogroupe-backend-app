package com.secogroupe.app.dto;

import com.secogroupe.app.entity.QuoteStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuoteUpdateRequest {

    @NotNull(message = "Le statut est obligatoire")
    private QuoteStatus status;

    private String adminNotes;
}
