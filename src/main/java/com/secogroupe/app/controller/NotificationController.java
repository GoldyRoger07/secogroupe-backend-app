package com.secogroupe.app.controller;

import java.security.Principal;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.secogroupe.app.service.SseEmitterService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final SseEmitterService sseEmitterService;

    /**
     * Flux SSE authentifié. Angular doit utiliser event-source-polyfill
     * pour passer le header Authorization: Bearer <token>.
     *
     * Reconnexion automatique toutes les 3s si la connexion tombe (comportement natif EventSource).
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(Principal principal) {
        return sseEmitterService.subscribe(principal.getName());
    }
}
