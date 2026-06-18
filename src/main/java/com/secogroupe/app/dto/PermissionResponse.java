package com.secogroupe.app.dto;

import lombok.Data;

@Data
public class PermissionResponse {
    private Long id;
    private String name;
    private String description;
    private String module;
    private String action;
    private String createdAt;
}
