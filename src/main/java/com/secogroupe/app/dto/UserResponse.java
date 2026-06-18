package com.secogroupe.app.dto;

import com.secogroupe.app.entity.UserStatus;

import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String roleName;
    private UserStatus status;
    private String createdAt;
}
