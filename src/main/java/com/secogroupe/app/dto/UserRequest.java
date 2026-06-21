package com.secogroupe.app.dto;

import java.util.List;

import com.secogroupe.app.entity.UserStatus;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserRequest {

    @NotBlank
    private String username;

    @NotBlank
    @Email
    private String email;

    private String password;

    /** Rôles assignés à l'utilisateur (RBAC : plusieurs rôles possibles). */
    private List<String> roleNames;

    /** @deprecated conservé pour rétro-compatibilité (import CSV, anciens clients). Utiliser {@link #roleNames}. */
    @Deprecated
    private String roleName;

    @NotNull
    private UserStatus status;
}
