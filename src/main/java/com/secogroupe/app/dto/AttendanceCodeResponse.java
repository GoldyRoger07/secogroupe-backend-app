package com.secogroupe.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AttendanceCodeResponse {
    private String code;
    private long periodSeconds;
    private long secondsRemaining;
}
