package com.secogroupe.app.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MetadataRequest {

    @NotBlank
    private String name;
}
