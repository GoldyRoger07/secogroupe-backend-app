package com.secogroupe.app.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Data;

@Entity
@Data
public class UserPreferences {

    @Id
    @GeneratedValue
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    // Appearance
    private String theme = "light";
    private String density = "normal";
    private String fontSize = "14";
    private String sidebarColor = "blue";

    // Notifications
    private boolean emailEnabled = true;
    private boolean loginAlerts = true;
    private boolean weeklyReport = false;
    private boolean newRegistrations = true;
    private boolean securityAlerts = true;
    private boolean maintenanceAlerts = false;
    private boolean exportCompleted = true;

    // Security
    private boolean twoFactorAuth = false;
    private String sessionDuration = "8h";
    private boolean enforceStrongPasswords = true;
    private boolean passwordExpiry = false;
    private boolean singleSession = false;
}
