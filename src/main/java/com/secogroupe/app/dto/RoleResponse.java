package com.secogroupe.app.dto;

import java.util.List;

import lombok.Data;

@Data
public class RoleResponse {
    private Long id;
    private String name;
    private String description;
    private int permissionCount;
    private List<Long> permissionIds;
    private String createdAt;
}
