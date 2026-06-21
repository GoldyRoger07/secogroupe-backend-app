package com.secogroupe.app.dto;

import java.time.Instant;
import java.time.LocalDate;

import com.secogroupe.app.entity.ArrivalStatus;
import com.secogroupe.app.entity.AttendanceStatus;
import com.secogroupe.app.entity.DepartureStatus;

import lombok.Data;

@Data
public class AttendanceResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String position;
    private String department;
    private LocalDate workDate;
    private Instant checkInAt;
    private Instant checkOutAt;
    private AttendanceStatus status;
    private ArrivalStatus arrivalStatus;
    private DepartureStatus departureStatus;
}
