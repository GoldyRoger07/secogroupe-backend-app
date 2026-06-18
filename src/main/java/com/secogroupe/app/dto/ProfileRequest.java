package com.secogroupe.app.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class ProfileRequest {
    private String firstName;
    private String lastName;
    @Email private String email;
    private String phone;
    private String position;
    private String bio;
}
