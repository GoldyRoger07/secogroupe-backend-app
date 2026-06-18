package com.secogroupe.app.mapper;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.secogroupe.app.dto.RoleResponse;
import com.secogroupe.app.entity.Permission;
import com.secogroupe.app.entity.Role;

@Component
public class RoleMapper {

    public RoleResponse toResponse(Role role) {
        RoleResponse res = new RoleResponse();
        res.setId(role.getId());
        res.setName(role.getName());
        res.setDescription(role.getDescription());
        res.setCreatedAt(role.getCreatedAt() != null ? role.getCreatedAt().toString() : null);
        if (role.getPermissions() != null) {
            res.setPermissionCount(role.getPermissions().size());
            res.setPermissionIds(role.getPermissions().stream()
                    .map(Permission::getId)
                    .collect(Collectors.toList()));
        } else {
            res.setPermissionCount(0);
            res.setPermissionIds(java.util.List.of());
        }
        return res;
    }
}
