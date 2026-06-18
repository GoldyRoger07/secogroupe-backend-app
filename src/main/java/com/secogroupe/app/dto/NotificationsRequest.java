package com.secogroupe.app.dto;

import lombok.Data;

@Data
public class NotificationsRequest {
    private boolean emailEnabled;
    private boolean loginAlerts;
    private boolean weeklyReport;
    private boolean newRegistrations;
    private boolean securityAlerts;
    private boolean maintenanceAlerts;
    private boolean exportCompleted;
}
