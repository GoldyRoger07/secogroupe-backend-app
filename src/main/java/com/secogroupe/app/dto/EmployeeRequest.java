package com.secogroupe.app.dto;

import java.time.LocalDate;

import com.secogroupe.app.entity.EmployeeStatus;
import com.secogroupe.app.model.Sexe;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class EmployeeRequest {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private String email;
    private String phone;

    @NotBlank
    private String position;

    private String department;

    private Double salary;

    @NotNull
    private EmployeeStatus status;

    private Sexe sexe;

    @Pattern(regexp = "\\d+", message = "Le NIF doit contenir uniquement des chiffres")
    private String nif;

    private String adresse;

    private String etatCivil;

    private Integer nombreEnfant;

    private LocalDate dateEmbauche;

    private LocalDate dateNaissance;

    private String banque;

    private String numCompteBancaire;

    /** Compte utilisateur à lier à cet employé (pour le pointage). Optionnel. */
    private Long userId;
}
