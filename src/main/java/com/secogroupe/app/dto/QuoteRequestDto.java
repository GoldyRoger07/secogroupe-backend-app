package com.secogroupe.app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QuoteRequestDto {

    @NotBlank(message = "La catégorie de service est obligatoire")
    private String serviceCategory;

    @NotBlank(message = "Le nom de la propriété/entreprise est obligatoire")
    private String businessName;

    private String directDialNumber;

    @NotBlank(message = "Le prénom est obligatoire")
    private String firstName;

    @NotBlank(message = "Le nom de famille est obligatoire")
    private String lastName;

    private String city;

    private String state;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    private boolean newsletterOptIn;
}
