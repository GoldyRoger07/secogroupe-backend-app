package com.secogroupe.app.dto;

import java.time.LocalDateTime;

import com.secogroupe.app.entity.QuoteStatus;

import lombok.Data;

@Data
public class QuoteResponse {
    private Long id;
    private String serviceCategory;
    private String businessName;
    private String directDialNumber;
    private String firstName;
    private String lastName;
    private String city;
    private String state;
    private String email;
    private boolean newsletterOptIn;
    private QuoteStatus status;
    private String adminNotes;
    private LocalDateTime submittedAt;
}
