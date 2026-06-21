package com.secogroupe.app.dto;

import lombok.Data;

@Data
public class EndpointPermissionResponse {
    private Long id;
    private String httpMethod;
    private String pathPattern;
    private String permissionName;
    private String description;
    private boolean enabled;
    private String createdAt;
}
