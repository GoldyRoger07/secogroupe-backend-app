package com.secogroupe.app.dto;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SessionResponse {
    private Long id;
    private Long userId;
    private String username;
    private String userEmail;
    private String userRoleName;
    private String userStatus;
    private Instant createdAt;
    private Instant expiresAt;
}
