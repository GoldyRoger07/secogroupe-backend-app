package com.secogroupe.app.dto;

import java.time.Instant;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ScanResponse {
    /** CHECKED_IN | CHECKED_OUT | ALREADY_COMPLETE */
    private String result;
    private String message;
    private String employeeName;
    private LocalDate workDate;
    private Instant time;
    /** Ponctualité de l'arrivée (EARLY|ON_TIME|LATE) — présent lors d'un check-in. */
    private String arrivalStatus;
    /** Ponctualité du départ (EARLY|ON_TIME|OVERTIME) — présent lors d'un check-out. */
    private String departureStatus;
}
