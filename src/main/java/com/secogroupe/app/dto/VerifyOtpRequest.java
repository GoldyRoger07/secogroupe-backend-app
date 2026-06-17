package com.secogroupe.app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VerifyOtpRequest {

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    @NotBlank(message = "Le code OTP est obligatoire")
    @Pattern(regexp = "\\d{6}", message = "Le code OTP doit contenir exactement 6 chiffres")
    private String otpCode;
}
