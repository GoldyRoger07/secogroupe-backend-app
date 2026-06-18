package com.secogroupe.app.mapper;

import org.springframework.stereotype.Component;

import com.secogroupe.app.dto.UserResponse;

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
            res.setRoleName(user.getRoles().iterator().next().getName());
        }
        return res;
    }
}
