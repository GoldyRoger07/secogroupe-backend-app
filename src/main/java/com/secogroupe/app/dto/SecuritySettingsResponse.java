package com.secogroupe.app.dto;

import lombok.Data;

@Data
public class SecuritySettingsResponse {
    private boolean twoFactorAuth;
    private String sessionDuration;
    private boolean enforceStrongPasswords;
    private boolean passwordExpiry;
    private boolean singleSession;
}
