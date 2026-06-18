package com.secogroupe.app.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import lombok.Data;

@Entity
@Data
public class Permission {

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String name;

    private String description;
    private String module;
    private String action;

    @Column(updatable = false)
    private Instant createdAt;

    public Permission() {}

    public Permission(String name) {
        this.name = name;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
