package com.secogroupe.app.dto;

public record SseEvent(
        String type,
        String title,
        String message,
        Long entityId
) {}
