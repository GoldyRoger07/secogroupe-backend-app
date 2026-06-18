package com.secogroupe.app.dto;

import lombok.Data;

@Data
public class ProfileResponse {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String position;
    private String bio;
    private String photoUrl;
}
