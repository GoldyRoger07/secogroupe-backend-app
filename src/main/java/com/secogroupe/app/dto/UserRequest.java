package com.secogroupe.app.dto;

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

    @NotBlank
    private String roleName;

    @NotNull
    private UserStatus status;
}
