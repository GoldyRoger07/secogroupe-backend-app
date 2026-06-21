package com.secogroupe.app.entity;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Entity
@Data
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "employee_id", "work_date" }))
public class Attendance {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDate workDate;

    @Column(nullable = false)
    private Instant checkInAt;

    private Instant checkOutAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status = AttendanceStatus.CHECKED_IN;

    @Enumerated(EnumType.STRING)
    private ArrivalStatus arrivalStatus;

    @Enumerated(EnumType.STRING)
    private DepartureStatus departureStatus;
}
