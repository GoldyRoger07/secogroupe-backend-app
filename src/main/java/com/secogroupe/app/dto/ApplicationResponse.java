package com.secogroupe.app.dto;

import java.time.LocalDateTime;

import com.secogroupe.app.entity.ApplicationStatus;

import lombok.Data;

@Data
public class ApplicationResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String position;
    private ApplicationStatus status;
    private String adminNotes;
    private LocalDateTime submittedAt;
}
