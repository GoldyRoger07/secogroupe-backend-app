package com.secogroupe.app.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EndpointPermissionRequest {

    @NotBlank(message = "La méthode HTTP est obligatoire")
    private String httpMethod;

    @NotBlank(message = "Le motif de chemin est obligatoire")
    private String pathPattern;

    @NotBlank(message = "La permission requise est obligatoire")
    private String permissionName;

    private String description;

    private boolean enabled = true;
}
