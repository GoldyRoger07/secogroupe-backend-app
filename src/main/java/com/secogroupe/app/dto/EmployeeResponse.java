package com.secogroupe.app.dto;

import java.time.LocalDate;

import com.secogroupe.app.entity.EmployeeStatus;
import com.secogroupe.app.model.Sexe;

import lombok.Data;

@Data
public class EmployeeResponse {
    private Long id;
    private String idEmployee;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String position;
    private String department;
    private Double salary;
    private String photoUrl;
    private EmployeeStatus status;
    private String createdAt;
    private Long userId;
    private String userUsername;

    private Sexe sexe;
    private String nif;
    private String adresse;
    private String etatCivil;
    private Integer nombreEnfant;
    private LocalDate dateEmbauche;
    private LocalDate dateNaissance;
    private String banque;
    private String numCompteBancaire;
}
