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
}
