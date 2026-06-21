package com.secogroupe.app.dto;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AttendanceSettingsRequest {

    @NotNull(message = "L'heure d'arrivée est obligatoire")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime expectedCheckIn;

    @NotNull(message = "L'heure de départ est obligatoire")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime expectedCheckOut;

    @Min(value = 0, message = "La tolérance ne peut pas être négative")
    private int graceMinutes;
}
