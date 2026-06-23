package com.secogroupe.app.entity;

import java.time.Instant;
import java.time.LocalDate;

import com.secogroupe.app.model.Sexe;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import lombok.Data;

@Entity
@Data
public class Employee {
    @Id
    @GeneratedValue
    private Long id;

    @Column(unique=true)
    private String idEmployee;

    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String position;
    private String department;
    private Double salary;
    private String photoUrl;

    @Enumerated(EnumType.STRING)
    private Sexe sexe;

    private String nif;

    private String adresse;

    private String etatCivil;

    private int nombreEnfant;

    private LocalDate dateEmbauche;

    private LocalDate dateNaissance;

    private String banque;
    
    private String numCompteBancaire;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmployeeStatus status = EmployeeStatus.ACTIVE;

    @Column(updatable = false)
    private Instant createdAt;

    @OneToOne
    private User user;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
