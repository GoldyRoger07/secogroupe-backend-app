package com.secogroupe.app.dto;

import java.util.List;

import com.secogroupe.app.entity.UserStatus;

import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private List<String> roleNames;
    /** Premier rôle (rétro-compatibilité d'affichage). */
    private String roleName;
    private UserStatus status;
    private String createdAt;
}
