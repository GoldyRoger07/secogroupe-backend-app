package com.secogroupe.app.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

/**
 * Règle d'autorisation dynamique : associe un motif d'endpoint (méthode + chemin)
 * à une permission requise. Évaluée à l'exécution, elle permet de protéger des
 * endpoints avec des permissions créées dynamiquement, sans recompilation.
 */
@Entity
@Data
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "http_method", "path_pattern" }))
public class EndpointPermission {

    @Id
    @GeneratedValue
    private Long id;

    /** Méthode HTTP : GET, POST, PUT, DELETE, PATCH ou * (toutes). */
    @Column(name = "http_method", nullable = false)
    private String httpMethod = "*";

    /** Motif de chemin Ant (ex. /api/v1/reports/**). */
    @Column(name = "path_pattern", nullable = false)
    private String pathPattern;

    /** Nom de la permission requise (autorité attendue dans le JWT). */
    @Column(nullable = false)
    private String permissionName;

    private String description;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
