package com.secogroupe.app.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ScanRequest {

    @NotBlank(message = "Le code est obligatoire")
    private String code;
}
