package com.secogroupe.app.entity;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import lombok.Data;

@Entity
@Data
public class CompanySettings {

    @Id
    @GeneratedValue
    private Long id;

    private String companyName = "SecoGroupe";
    private String companyEmail = "contact@secogroupe.com";
    private String companyPhone;
    private String website;
    private String address;
    private String timezone = "Europe/Paris";
    private String dateFormat = "dd/MM/yyyy";
    private String language = "fr";

    private Instant updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
