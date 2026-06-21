package com.secogroupe.app.dto;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AttendanceSettingsResponse {

    @JsonFormat(pattern = "HH:mm")
    private LocalTime expectedCheckIn;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime expectedCheckOut;

    private int graceMinutes;
}
