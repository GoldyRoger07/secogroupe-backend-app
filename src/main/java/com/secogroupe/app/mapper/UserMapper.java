package com.secogroupe.app.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.secogroupe.app.dto.UserResponse;

import com.secogroupe.app.entity.Role;
import com.secogroupe.app.entity.User;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        UserResponse res = new UserResponse();
        res.setId(user.getId());
        res.setUsername(user.getUsername());
        res.setEmail(user.getEmail());
        res.setStatus(user.getStatus());
        res.setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            List<String> names = user.getRoles().stream().map(Role::getName).sorted().toList();
            res.setRoleNames(names);
            res.setRoleName(names.get(0));
        } else {
            res.setRoleNames(List.of());
        }
        return res;
    }
}
