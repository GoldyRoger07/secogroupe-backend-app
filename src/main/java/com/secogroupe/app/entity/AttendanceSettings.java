package com.secogroupe.app.entity;

import java.time.Instant;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import lombok.Data;

/**
 * Horaires de référence (singleton) servant à évaluer la ponctualité des pointages.
 */
@Entity
@Data
public class AttendanceSettings {

    @Id
    @GeneratedValue
    private Long id;

    /** Heure normale d'arrivée. */
    @Column(nullable = false)
    private LocalTime expectedCheckIn = LocalTime.of(8, 0);

    /** Heure normale de départ. */
    @Column(nullable = false)
    private LocalTime expectedCheckOut = LocalTime.of(17, 0);

    /** Tolérance en minutes autour de l'heure de référence pour être considéré « à l'heure ». */
    @Column(nullable = false)
    private int graceMinutes = 5;

    private Instant updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
