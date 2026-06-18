package com.secogroupe.app.dto;

import java.time.Instant;

import lombok.Data;

@Data
public class LoginHistoryResponse {
    private Instant date;
    private String device;
    private String ip;
    private String location;
    private boolean success;
}
