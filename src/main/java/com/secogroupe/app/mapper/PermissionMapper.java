package com.secogroupe.app.mapper;

import org.springframework.stereotype.Component;

import com.secogroupe.app.dto.PermissionResponse;
import com.secogroupe.app.entity.Permission;

@Component
public class PermissionMapper {

    public PermissionResponse toResponse(Permission permission) {
        PermissionResponse res = new PermissionResponse();
        res.setId(permission.getId());
        res.setName(permission.getName());
        res.setDescription(permission.getDescription());
        res.setModule(permission.getModule());
        res.setAction(permission.getAction());
        res.setCreatedAt(permission.getCreatedAt() != null ? permission.getCreatedAt().toString() : null);
        return res;
    }
}
