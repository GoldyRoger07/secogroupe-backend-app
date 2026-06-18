package com.secogroupe.app.entity;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.Data;

@Entity
@Data
public class LoginHistory {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private Instant date;
    private String device;
    private String ipAddress;
    private String location = "Inconnu";
    private boolean success;

    @PrePersist
    protected void onCreate() {
        if (date == null) date = Instant.now();
    }
}
