package com.secogroupe.app.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.secogroupe.app.dto.SseEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseEmitterService {

    private static final long EMITTER_TIMEOUT = 5 * 60 * 1000L;

    private final ObjectMapper objectMapper;
    private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String username) {
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT);
        emitters.computeIfAbsent(username, k -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> remove(username, emitter));
        emitter.onTimeout(() -> remove(username, emitter));
        emitter.onError(e -> remove(username, emitter));
        return emitter;
    }

    public void send(String username, SseEvent event) {
        List<SseEmitter> list = emitters.get(username);
        if (list != null) doSend(list, event);
    }

    public void broadcast(SseEvent event) {
        emitters.values().forEach(list -> doSend(list, event));
    }

    // Heartbeat toutes les 25s pour maintenir les connexions SSE actives à travers les proxies
    @Scheduled(fixedDelay = 25_000)
    public void heartbeat() {
        SseEmitter.SseEventBuilder ping = SseEmitter.event().name("ping").data("keep-alive");
        emitters.values().forEach(list -> {
            List<SseEmitter> dead = new CopyOnWriteArrayList<>();
            for (SseEmitter emitter : list) {
                try {
                    emitter.send(ping);
                } catch (IOException e) {
                    dead.add(emitter);
                }
            }
            list.removeAll(dead);
        });
    }

    private void doSend(List<SseEmitter> list, SseEvent event) {
        List<SseEmitter> dead = new CopyOnWriteArrayList<>();
        for (SseEmitter emitter : list) {
            try {
                String json = objectMapper.writeValueAsString(event);
                emitter.send(SseEmitter.event().name(event.type()).data(json));
            } catch (IOException e) {
                dead.add(emitter);
            } catch (Exception e) {
                log.error("Erreur SSE pour un émetteur: {}", e.getMessage());
                dead.add(emitter);
            }
        }
        list.removeAll(dead);
    }

    private void remove(String username, SseEmitter emitter) {
        List<SseEmitter> list = emitters.get(username);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) emitters.remove(username);
        }
    }
}
