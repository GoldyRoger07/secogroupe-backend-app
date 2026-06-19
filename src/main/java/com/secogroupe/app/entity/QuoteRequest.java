package com.secogroupe.app.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class QuoteRequest {

    @Id
    @GeneratedValue
    private Long id;

    private String serviceCategory;
    private String businessName;
    private String directDialNumber;
    private String firstName;
    private String lastName;
    private String city;
    private String state;

    @Column(nullable = false)
    private String email;

    private boolean newsletterOptIn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuoteStatus status = QuoteStatus.NEW;

    @Column(columnDefinition = "TEXT")
    private String adminNotes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime submittedAt = LocalDateTime.now();
}
