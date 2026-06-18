package com.secogroupe.app.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PermissionRequest {

    @NotBlank
    private String name;

    private String description;

    @NotBlank
    private String module;

    @NotBlank
    private String action;
}
