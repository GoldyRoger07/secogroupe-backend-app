package com.secogroupe.app.entity;

import java.time.Instant;
import java.time.LocalDate;

import com.secogroupe.app.model.CongeStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class Conge {
    
    @Id
    @GeneratedValue
    private Long id;
    
    @ManyToOne
    private Employee employee;

    private String typeConge;

    // Date de début
    private LocalDate startDate;

    // Date de fin
    private LocalDate endDate;

    // Nombre de jours
    private Integer numberOfDays;

    // Motif ou commentaire de l'employé
    @Column(length = 1000)
    private String reason;

    // Statut de la demande
    @Enumerated(EnumType.STRING)
    private CongeStatus status;

     // Personne ayant approuvé ou refusé
    @ManyToOne
    private Employee approvedBy;

    // Commentaire du responsable
    @Column(length = 1000)
    private String managerComment;

    // Date de décision
    private Instant decisionDate;

    // Date de création
    private Instant createdAt;

    // Date de modification
    private Instant updatedAt;




}
