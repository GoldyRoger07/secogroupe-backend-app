package com.secogroupe.app.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoleRequest {

    @NotBlank
    private String name;

    private String description;

    private List<Long> permissionIds;
}
